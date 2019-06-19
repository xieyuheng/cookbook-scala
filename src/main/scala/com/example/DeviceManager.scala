package com.example

import akka.actor.{ Actor, ActorRef, ActorLogging, Props }

object DeviceManager {
  def props(): Props = Props(new DeviceManager)

  case class RequestTrackDevice(groupId: String, deviceId: String)
  case object DeviceRegistered
}


class DeviceManager extends Actor with ActorLogging {
  import DeviceManager._

  var groupMap = Map.empty[String, ActorRef]
  var groupIdMap = Map.empty[ActorRef, String]

  override def preStart() = log.info("DeviceManager started")

  override def postStop() = log.info("DeviceManager stopped")

  def receive = {
    case req @ RequestTrackDevice(groupId, _) =>
      groupMap.get(groupId) match {
        case Some(group) =>
          group.forward(req)
        case None =>
          log.info("Creating device group actor for {}", groupId)
          val group = context.actorOf(DeviceGroup.props(groupId), "group-" + groupId)
          context.watch(group)
          group.forward(req)
          groupMap += groupId -> group
          groupIdMap += group -> groupId
      }

    case akka.actor.Terminated(group) =>
      val groupId = groupIdMap(group)
      log.info("Device group actor for {} has been terminated", groupId)
      groupIdMap -= group
      groupMap -= groupId
  }
}
