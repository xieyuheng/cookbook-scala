package xieyuheng.cookbook.akka

import org.scalatest.{ BeforeAndAfterAll, WordSpecLike, Matchers }
import akka.actor.ActorSystem
import akka.testkit.{ TestKit, TestProbe }
import scala.concurrent.duration._
import scala.language.postfixOps

class AkkaQuickstartSpec(_system: ActorSystem)
    extends TestKit(_system)
    with Matchers
    with WordSpecLike
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("AkkaQuickstartSpec"))

  override def afterAll = {
    shutdown(system)
  }

  "A Greeter" should {
    "say Hi {name} to logger" in {
      val logger = TestProbe()
      val greeter = system.actorOf(Greeter.props(logger.ref))
      greeter ! Greeter.SayHi("Xie Yuheng")
      logger.expectMsg(500.milliseconds, Logger.Log("Hi! Xie Yuheng"))
    }

    "also say Bye to logger" in {
      val logger = TestProbe()
      val greeter = system.actorOf(Greeter.props(logger.ref))
      greeter ! Greeter.SayBye
      logger.expectMsg(500.milliseconds, Logger.Log("Bye ^-^/"))
    }
  }
}
