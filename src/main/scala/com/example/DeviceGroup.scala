package com.example

import akka.actor.{ Actor, ActorRef, ActorLogging, Props }

/*
 * A group of devices in one home.
 */

object DeviceGroup {
  def props(groupId: String) = Props(new DeviceGroup(groupId))

  case class RequestDeviceIds(requestId: Long)
  case class ReplyDeviceIds(requestId: Long, deviceIds: Set[String])
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
  }
}
