package com.example

import org.scalatest.{ BeforeAndAfterAll, WordSpecLike, Matchers }
import akka.actor.ActorSystem
import akka.testkit.{ TestKit, TestProbe }
import scala.concurrent.duration._
import scala.language.postfixOps

class DeviceSpec(_system: ActorSystem)
    extends TestKit(_system)
    with Matchers
    with WordSpecLike
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("DeviceSpec"))

  override def afterAll = {
    shutdown(system)
  }

  "reply with empty reading if no temperature is known" in {
    val probe = TestProbe()
    val device = system.actorOf(Device.props("group", "device"))

    {
      // use second arg of `.tell` to provide explicit sender
      // device ! Device.ReadTemperature(requestId = 42)
      device.tell(Device.ReadTemperature(requestId = 42), probe.ref)
      val res = probe.expectMsgType[Device.RespondTemperature]
      res.requestId should ===(42L)
      res.value should ===(None)
    }
  }

  "reply with latest temperature reading" in {
    val probe = TestProbe()
    val device = system.actorOf(Device.props("group", "device"))

    {
      device.tell(Device.RecordTemperature(requestId = 1, 24.0), probe.ref)
      // probe.expectMsg(Device.TemperatureRecorded(requestId = 1))
      val res = probe.expectMsgType[Device.TemperatureRecorded]
      res.requestId should ===(1L)
    }

    {
      device.tell(Device.ReadTemperature(requestId = 2), probe.ref)
      val res = probe.expectMsgType[Device.RespondTemperature]
      res.requestId should ===(2L)
      res.value should ===(Some(24.0))
    }

    {
      device.tell(Device.RecordTemperature(requestId = 3, 55.0), probe.ref)
      probe.expectMsg(Device.TemperatureRecorded(requestId = 3))
    }

    {
      device.tell(Device.ReadTemperature(requestId = 4), probe.ref)
      val res = probe.expectMsgType[Device.RespondTemperature]
      res.requestId should ===(4L)
      res.value should ===(Some(55.0))
    }
  }

  "reply to registration requests" in {
    val probe = TestProbe()
    val device = system.actorOf(Device.props("group", "device"))

    device.tell(DeviceManager.RequestTrackDevice("group", "device"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    probe.lastSender should ===(device)
  }

  "ignore wrong registration requests" in {
    val probe = TestProbe()
    val device = system.actorOf(Device.props("group", "device"))

    device.tell(DeviceManager.RequestTrackDevice("wrongGroup", "device"), probe.ref)
    probe.expectNoMessage(500.milliseconds)

    device.tell(DeviceManager.RequestTrackDevice("group", "Wrongdevice"), probe.ref)
    probe.expectNoMessage(500.milliseconds)
  }
}
