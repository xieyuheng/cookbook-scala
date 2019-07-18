package xieyuheng.cookbook.akka

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.event.Logging

object Greeter {
  def props(implicit logger: ActorRef) = Props(new Greeter)

  case class SayHi(yo: String)
  case object SayBye
}

class Greeter(implicit logger: ActorRef) extends Actor {
  def receive = {
    case Greeter.SayHi(yo) =>
      logger ! Logger.Log(s"Hi! ${yo}")
    case Greeter.SayBye =>
      logger ! Logger.Log(s"Bye ^-^/")
  }
}

object Logger {
  def props = Props(new Logger)

  case class Log(x: Any)
}

class Logger extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case Logger.Log(x) => log.info(s"${x}")
  }
}

object GreeterApp extends App {
  val system = ActorSystem("GreeterApp")
  implicit val logger = system.actorOf(Logger.props, "logger")
  val greeter = system.actorOf(Greeter.props, "greeter")

  greeter ! Greeter.SayHi("Xie Yuheng")
  greeter ! Greeter.SayBye
}
