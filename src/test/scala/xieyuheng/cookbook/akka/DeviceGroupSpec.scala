package xieyuheng.cookbook.akka

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

  "be able to list active devices" in {
    val probe = TestProbe()
    val group = system.actorOf(DeviceGroup.props("group"))

    group.tell(DeviceManager.RequestTrackDevice("group", "device1"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)

    group.tell(DeviceManager.RequestTrackDevice("group", "device2"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)

    group.tell(DeviceGroup.RequestDeviceIds(requestId = 0), probe.ref)
    probe.expectMsg(DeviceGroup.ReplyDeviceIds(requestId = 0, Set("device1", "device2")))
  }

  "be able to list active devices after one shuts down" in {
    val probe = TestProbe()
    val group = system.actorOf(DeviceGroup.props("group"))

    group.tell(DeviceManager.RequestTrackDevice("group", "device1"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)

    val toShutDown = probe.lastSender

    group.tell(DeviceManager.RequestTrackDevice("group", "device2"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)

    group.tell(DeviceGroup.RequestDeviceIds(requestId = 0), probe.ref)
    probe.expectMsg(DeviceGroup.ReplyDeviceIds(requestId = 0, Set("device1", "device2")))

    probe.watch(toShutDown)
    toShutDown ! akka.actor.PoisonPill
    probe.expectTerminated(toShutDown)

    // using awaitAssert to retry because it might take longer for the group
    // to see the Terminated, that order is undefined
    probe.awaitAssert {
      group.tell(DeviceGroup.RequestDeviceIds(requestId = 1), probe.ref)
      probe.expectMsg(DeviceGroup.ReplyDeviceIds(requestId = 1, Set("device2")))
    }
  }

  "be able to collect temperatures from all active devices" in {
    val probe = TestProbe()
    val group = system.actorOf(DeviceGroup.props("group"))

    group.tell(DeviceManager.RequestTrackDevice("group", "device1"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val device1 = probe.lastSender

    group.tell(DeviceManager.RequestTrackDevice("group", "device2"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val device2 = probe.lastSender

    group.tell(DeviceManager.RequestTrackDevice("group", "device3"), probe.ref)
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val device3 = probe.lastSender

    // Check that the device actors are working
    device1.tell(Device.RecordTemperature(requestId = 0, 1.0), probe.ref)
    probe.expectMsg(Device.TemperatureRecorded(requestId = 0))
    device2.tell(Device.RecordTemperature(requestId = 1, 2.0), probe.ref)
    probe.expectMsg(Device.TemperatureRecorded(requestId = 1))
    // No temperature for device3

    group.tell(DeviceGroup.RequestAllTemperatures(requestId = 0), probe.ref)
    probe.expectMsg(
      DeviceGroup.RespondAllTemperatures(
        requestId = 0,
        temperatures = Map(
          "device1" -> DeviceGroup.Temperature(1.0),
          "device2" -> DeviceGroup.Temperature(2.0),
          "device3" -> DeviceGroup.TemperatureNotAvailable)))
  }
}
