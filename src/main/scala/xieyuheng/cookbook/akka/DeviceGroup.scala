package xieyuheng.cookbook.akka

import akka.actor.{ Actor, ActorRef, ActorLogging, Props }
import scala.concurrent.duration._

/*
 * A group of devices in one home.
 */

object DeviceGroup {
  def props(groupId: String) = Props(new DeviceGroup(groupId))

  case class RequestDeviceIds(requestId: Long)
  case class ReplyDeviceIds(requestId: Long, deviceIds: Set[String])

  case class RequestAllTemperatures(requestId: Long)
  case class RespondAllTemperatures(requestId: Long, temperatures: Map[String, TemperatureReading])

  trait TemperatureReading
  case class Temperature(value: Double) extends TemperatureReading
  case object TemperatureNotAvailable extends TemperatureReading
  case object DeviceNotAvailable extends TemperatureReading
  case object DeviceTimedOut extends TemperatureReading
}

class DeviceGroup(groupId: String) extends Actor with ActorLogging {
  import DeviceGroup._

  var deviceMap = Map.empty[String, ActorRef]
  var deviceIdMap = Map.empty[ActorRef, String]

  override def preStart() = log.info("DeviceGroup {} started", groupId)

  override def postStop() = log.info("DeviceGroup {} stopped", groupId)

  def receive = {
    /*
     * Maybe we should explicitly send sensor's ActorRef down to device,
     * instead of forwarding sender.
     */
    case req @ DeviceManager.RequestTrackDevice(`groupId`, _) =>
      deviceMap.get(req.deviceId) match {
        case Some(device) =>
          device.forward(req)
        case None =>
          log.info("Creating device actor for {}", req.deviceId)
          val device = context.actorOf(
            Device.props(groupId, req.deviceId),
            s"device-${req.deviceId}")
          context.watch(device)
          deviceMap += req.deviceId -> device
          deviceIdMap += device -> req.deviceId
          device.forward(req)
      }

    case DeviceManager.RequestTrackDevice(groupId, deviceId) =>
      log.warning(
        "Ignoring TrackDevice request for {}. This actor is responsible for {}.",
        groupId,
        this.groupId)

    case akka.actor.Terminated(device) =>
      val deviceId = deviceIdMap(device)
      log.info("Device actor for {} has been terminated", deviceId)
      deviceIdMap -= device
      deviceMap -= deviceId

    case RequestDeviceIds(requestId) =>
      val deviceIds = deviceMap.keySet
      sender() ! ReplyDeviceIds(requestId, deviceIds)

    case RequestAllTemperatures(requestId) =>
      // context.actorOf(
      //   DeviceGroupQuery.props(
      //     deviceIdMap = deviceIdMap,
      //     requestId = requestId,
      //     requester = sender(),
      //     timeout = 3.seconds))
      context.actorOf(
        DeviceGroupQueryNatural.props(
          deviceIdMap = deviceIdMap,
          requestId = requestId,
          requester = sender(),
          timeout = 3.seconds))
  }
}
