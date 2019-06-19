package com.example

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

  "A Greeter Actor" should {
    "pass on a greeting message when instructed to" in {
      val probe = TestProbe()
      val helloGreetingMessage = "hello"
      val helloGreeter = system.actorOf(Greeter.props(helloGreetingMessage, probe.ref))
      val name = "Akka"
      helloGreeter ! Greeter.WhoToGreet(name)
      helloGreeter ! Greeter.Greet
      probe.expectMsg(
        500.milliseconds,
        Printer.Greeting(helloGreetingMessage + ", " + name))
    }
  }
}
