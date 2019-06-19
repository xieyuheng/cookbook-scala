package com.example

import akka.actor.{ Actor, ActorRef, ActorLogging, Props, Terminated }
import scala.concurrent.duration._

/*
 * A verb can also be implemented as actor.
 * An actor can represents a process or a task rather than an object,
 *   or we can say we are viewing a verb as a noun,
 *   or we can view these kind of actors as works.

 * We will create an actor that represents a single query
 * and that performs the tasks needed to complete the query on behalf of the group actor.
 */

object DeviceGroupQueryNatural {
  case object CollectionTimeout

  def props(
    deviceIdMap: Map[ActorRef, String],
    requestId: Long,
    requester: ActorRef,
    timeout: FiniteDuration,
  ) = Props(
    new DeviceGroupQueryNatural(
      deviceIdMap,
      requestId,
      requester,
      timeout))
}

class DeviceGroupQueryNatural(
  deviceIdMap: Map[ActorRef, String],
  requestId: Long,
  requester: ActorRef,
  timeout: FiniteDuration,
) extends Actor with ActorLogging {
  import DeviceGroupQueryNatural._
  import context.dispatcher

  val queryTimeoutTimer = context.system.scheduler.scheduleOnce(timeout, self, CollectionTimeout)

  override def preStart() = {
    deviceIdMap.keysIterator.foreach { device =>
      context.watch(device)
      device ! Device.ReadTemperature(0)
    }
  }

  override def postStop() = {
    queryTimeoutTimer.cancel()
  }

  var repliesSoFar: Map[String, DeviceGroup.TemperatureReading] = Map.empty
  var stillWaiting: Set[ActorRef] = deviceIdMap.keySet

  def receive = {
    case Device.RespondTemperature (0, option) =>
      val device = sender()
      val reading = option match {
        case Some(value) => DeviceGroup.Temperature(value)
        case None => DeviceGroup.TemperatureNotAvailable
      }
      receivedResponse(device, reading)

    case Terminated(device) =>
      receivedResponse(device, DeviceGroup.DeviceNotAvailable)

    case CollectionTimeout =>
      val timedOutReplies = stillWaiting.map { device =>
        val deviceId = deviceIdMap(device)
        deviceId -> DeviceGroup.DeviceTimedOut
      }
      requester ! DeviceGroup.RespondAllTemperatures(requestId, repliesSoFar ++ timedOutReplies)
      context.stop(self)
  }

  def receivedResponse(
    device: ActorRef,
    reading: DeviceGroup.TemperatureReading,
  ): Unit = {
    context.unwatch(device)

    val deviceId = deviceIdMap(device)

    stillWaiting = stillWaiting - device
    repliesSoFar = repliesSoFar + (deviceId -> reading)

    if (stillWaiting.isEmpty) {
      requester ! DeviceGroup.RespondAllTemperatures(requestId, repliesSoFar)
      context.stop(self)
    }
  }
}
