package xieyuheng.cookbook.akka

import akka.actor.{Actor, ActorLogging, Props}

object Device {
  def props(groupId: String, deviceId: String) =
    Props(new Device(groupId, deviceId))

  case class RecordTemperature(requestId: Long, value: Double)
  case class TemperatureRecorded(requestId: Long)

  case class ReadTemperature(requestId: Long)
  case class RespondTemperature(requestId: Long, value: Option[Double])
}

class Device(
  groupId: String,
  deviceId: String
) extends Actor
    with ActorLogging {
  import Device._

  var lastTemperatureReading: Option[Double] = None

  override def preStart() =
    log.info("Device actor {}-{} started", groupId, deviceId)

  override def postStop() =
    log.info("Device actor {}-{} stopped", groupId, deviceId)

  def receive = {
    case ReadTemperature(id) =>
      sender() ! RespondTemperature(id, lastTemperatureReading)

    case RecordTemperature(id, value) =>
      log.info("Recorded temperature reading {} with {}", value, id)
      lastTemperatureReading = Some(value)
      sender() ! TemperatureRecorded(id)

    case DeviceManager.RequestTrackDevice(`groupId`, `deviceId`) =>
      sender() ! DeviceManager.DeviceRegistered

    case DeviceManager.RequestTrackDevice(groupId, deviceId) =>
      log.warning(
        "Ignoring TrackDevice request for {}-{}.This actor is responsible for {}-{}.",
        groupId,
        deviceId,
        this.groupId,
        this.deviceId
      )
  }
}
