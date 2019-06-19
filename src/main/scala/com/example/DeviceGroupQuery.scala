package com.example

import akka.actor.{ Actor, ActorRef, ActorLogging, Props, Terminated }
import scala.concurrent.duration._

/*
 * A verb can also be implemented as actor.
 * An actor can represent a process or a task rather than an object,
 *   or we can say we are viewing a verb as a noun,
 *   or we can view these kind of actors as workers.

 * We will create an actor that represents a single query
 * and that performs the tasks needed to complete the query on behalf of the group actor.
 */

object DeviceGroupQuery {
  case object CollectionTimeout

  def props(
    deviceIdMap: Map[ActorRef, String],
    requestId: Long,
    requester: ActorRef,
    timeout: FiniteDuration,
  ) = Props(
    new DeviceGroupQuery(
      deviceIdMap,
      requestId,
      requester,
      timeout))
}

class DeviceGroupQuery(
  deviceIdMap: Map[ActorRef, String],
  requestId: Long,
  requester: ActorRef,
  timeout: FiniteDuration,
) extends Actor with ActorLogging {
  import DeviceGroupQuery._
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

  def receive: Receive =
    waitingForReplies(Map.empty, deviceIdMap.keySet)

  def waitingForReplies(
    repliesSoFar: Map[String, DeviceGroup.TemperatureReading],
    stillWaiting: Set[ActorRef],
  ): Receive = {
    case Device.RespondTemperature (0, option) =>
      val device = sender()
      val reading = option match {
        case Some(value) => DeviceGroup.Temperature(value)
        case None => DeviceGroup.TemperatureNotAvailable
      }
      receivedResponse(
        device,
        reading,
        repliesSoFar,
        stillWaiting)

    case Terminated(device) =>
      receivedResponse(
        device,
        DeviceGroup.DeviceNotAvailable,
        repliesSoFar,
        stillWaiting)

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
    repliesSoFar: Map[String, DeviceGroup.TemperatureReading],
    stillWaiting: Set[ActorRef],
  ): Unit = {
    context.unwatch(device)

    val deviceId = deviceIdMap(device)
    val newStillWaiting = stillWaiting - device
    val newRepliesSoFar = repliesSoFar + (deviceId -> reading)

    if (newStillWaiting.isEmpty) {
      requester ! DeviceGroup.RespondAllTemperatures(requestId, newRepliesSoFar)
      context.stop(self)
    } else {
      context.become(waitingForReplies(newRepliesSoFar, newStillWaiting))
    }
  }
}
