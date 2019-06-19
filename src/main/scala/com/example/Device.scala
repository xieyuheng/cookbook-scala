package com.example

import akka.actor.{ Actor, ActorLogging, Props }

object Device {
  def props(groupId: String, deviceId: String): Props = Props(new Device(groupId, deviceId))

  case class ReadTemperature(requestId: Long)
  case class RespondTemperature(requestId: Long, value: Option[Double])
}

class Device(groupId: String, deviceId: String) extends Actor with ActorLogging {
  import Device._

  var lastTemperatureReading: Option[Double] = None

  override def preStart() = log.info("Device actor {}-{} started", groupId, deviceId)
  override def postStop() = log.info("Device actor {}-{} stopped", groupId, deviceId)

  def receive = {
    case ReadTemperature(id) =>
      sender() ! RespondTemperature(id, lastTemperatureReading)
  }
}
