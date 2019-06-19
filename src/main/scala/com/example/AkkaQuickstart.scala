package com.example

import akka.actor.{
  Actor,
  ActorLogging,
  ActorRef,
  ActorSystem,
  Props,
}

// use companion object to define messages of an actor

object Greeter {
  // use a props method in the companion object construct the actor
  def props(
    message: String,
    printer: ActorRef
  ): Props = Props(new Greeter(message, printer))

  case class WhoToGreet(who: String)
  case object Greet
}

class Greeter(message: String, printer: ActorRef) extends Actor {
  import Greeter._

  var greeting = ""

  def receive = {
    case WhoToGreet(who) =>
      greeting = message + ", " + who
    case Greet =>
      printer ! Printer.Greeting(greeting)
  }
}

object Printer {
  def props: Props = Props[Printer]
  case class Greeting(greeting: String)
}

class Printer extends Actor with ActorLogging {
  import Printer._

  def receive = {
    case Greeting(greeting) =>
      log.info("Greeting received (from " + sender() + "): " + greeting)
  }
}

object AkkaQuickstart extends App {
  import Greeter._

  val system = ActorSystem("helloAkka")

  val printer = system.actorOf(Printer.props, "printer")

  val howdyGreeter = system.actorOf(Greeter.props("Howdy", printer), "howdyGreeter")
  val helloGreeter = system.actorOf(Greeter.props("Hello", printer), "helloGreeter")
  val goodDayGreeter = system.actorOf(Greeter.props("Good day", printer), "goodDayGreeter")

  howdyGreeter ! WhoToGreet("Akka")
  howdyGreeter ! Greet

  howdyGreeter ! WhoToGreet("Lightbend")
  howdyGreeter ! Greet

  helloGreeter ! WhoToGreet("Scala")
  helloGreeter ! Greet

  goodDayGreeter ! WhoToGreet("Play")
  goodDayGreeter ! Greet
  goodDayGreeter ! Greet
  goodDayGreeter ! Greet
}
