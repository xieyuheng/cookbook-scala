package xieyuheng.cookbook.akka

import org.scalatest.{BeforeAndAfterAll, WordSpecLike, Matchers}
import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import scala.concurrent.duration._
import scala.language.postfixOps

class DeviceManagerSpec(_system: ActorSystem)
    extends TestKit(_system)
    with Matchers
    with WordSpecLike
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("DeviceManagerSpec"))

  override def afterAll =
    shutdown(system)

  "be able to register a group actor" in {
    val probe = TestProbe()
    val manager = system.actorOf(DeviceManager.props())

    manager.tell(
      DeviceManager.RequestTrackDevice("group", "device1"),
      probe.ref
    )
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val device1 = probe.lastSender

    manager.tell(
      DeviceManager.RequestTrackDevice("group", "device2"),
      probe.ref
    )
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val device2 = probe.lastSender

    device1 should !==(device2)

    // Check that the device actors are working
    device1.tell(Device.RecordTemperature(requestId = 0, 1.0), probe.ref)
    probe.expectMsg(Device.TemperatureRecorded(requestId = 0))
    device2.tell(Device.RecordTemperature(requestId = 1, 2.0), probe.ref)
    probe.expectMsg(Device.TemperatureRecorded(requestId = 1))
  }

  "return same actor for same groupId" in {
    val probe = TestProbe()
    val manager = system.actorOf(DeviceManager.props())

    manager.tell(
      DeviceManager.RequestTrackDevice("group", "device1"),
      probe.ref
    )
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val device1 = probe.lastSender

    manager.tell(
      DeviceManager.RequestTrackDevice("group", "device1"),
      probe.ref
    )
    probe.expectMsg(DeviceManager.DeviceRegistered)
    val device2 = probe.lastSender

    device1 should ===(device2)
  }
}
