package com.example

import akka.actor.{ Actor, ActorRef, ActorLogging, Props }

/*
 * A group of devices in one home.
 */

object DeviceGroup {
  def props(groupId: String) = Props(new DeviceGroup(groupId))
}

class DeviceGroup(groupId: String) extends Actor with ActorLogging {
  var deviceMap = Map.empty[String, ActorRef]

  override def preStart() = log.info("DeviceGroup {} started", groupId)

  override def postStop() = log.info("DeviceGroup {} stopped", groupId)

  def receive = {
    /*
     * Maybe we should explicitly send sensor's ActorRef down to device,
     * instead of forwarding sender.
     */
    case trackMsg @ DeviceManager.RequestTrackDevice(`groupId`, _) =>
      deviceMap.get(trackMsg.deviceId) match {
        case Some(device) =>
          device.forward(trackMsg)
        case None =>
          log.info("Creating device actor for {}", trackMsg.deviceId)
          val device = context.actorOf(
            Device.props(groupId, trackMsg.deviceId),
            s"device-${trackMsg.deviceId}")
          deviceMap += trackMsg.deviceId -> device
          device.forward(trackMsg)
      }

    case DeviceManager.RequestTrackDevice(groupId, deviceId) =>
      log.warning(
        "Ignoring TrackDevice request for {}. This actor is responsible for {}.",
        groupId,
        this.groupId)
  }
}
