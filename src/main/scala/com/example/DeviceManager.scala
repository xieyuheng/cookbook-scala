package com.example

import akka.actor.{ Actor, ActorLogging, Props }

object DeviceManager {
  def props(): Props = Props(new DeviceManager)

  final case class RequestTrackDevice(groupId: String, deviceId: String)
  case object DeviceRegistered
}

class DeviceManager extends Actor with ActorLogging {

  def receive = Actor.emptyBehavior
}
