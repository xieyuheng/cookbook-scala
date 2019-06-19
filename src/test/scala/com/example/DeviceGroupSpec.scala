package com.example

import org.scalatest.{ BeforeAndAfterAll, WordSpecLike, Matchers }
import akka.actor.ActorSystem
import akka.testkit.{ TestKit, TestProbe }
import scala.concurrent.duration._
import scala.language.postfixOps

class DeviceGroupSpec(_system: ActorSystem)
    extends TestKit(_system)
    with Matchers
    with WordSpecLike
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("DeviceGroupSpec"))

  override def afterAll = {
    shutdown(system)
  }

  "be able to register a device actor" in {
    val probe = TestProbe()
    val group = system.actorOf(DeviceGroup.props("group"))

    group.tell(DeviceManager.RequestTrackDevice("group", "device1"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val device1 = probe.lastSender

    group.tell(DeviceManager.RequestTrackDevice("group", "device2"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val device2 = probe.lastSender

    device1 should !==(device2)

    // Check that the device actors are working
    device1.tell(Device.RecordTemperature(requestId = 0, 1.0), probe.ref)
    probe.expectMsg(Device.TemperatureRecorded(requestId = 0))
    device2.tell(Device.RecordTemperature(requestId = 1, 2.0), probe.ref)
    probe.expectMsg(Device.TemperatureRecorded(requestId = 1))
  }

  "ignore requests for wrong groupId" in {
    val probe = TestProbe()
    val group = system.actorOf(DeviceGroup.props("group"))

    group.tell(DeviceManager.RequestTrackDevice("wrongGroup", "device1"), probe.ref)
    probe.expectNoMessage(500.milliseconds)
  }

  "return same actor for same deviceId" in {
    val probe = TestProbe()
    val group = system.actorOf(DeviceGroup.props("group"))

    group.tell(DeviceManager.RequestTrackDevice("group", "device1"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val device1 = probe.lastSender

    group.tell(DeviceManager.RequestTrackDevice("group", "device1"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val device2 = probe.lastSender

    device1 should ===(device2)
  }
}
