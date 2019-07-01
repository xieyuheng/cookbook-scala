package xieyuheng.cookbook.akka

import akka.actor.{ Actor, ActorRef, ActorSystem, PoisonPill, Props }
import language.postfixOps
import scala.concurrent.duration._

case object Ping
case object Pong

class Pinger extends Actor {
  var countDown = 10

  def receive = {
    case Pong =>
      println(s"${self.path} received pong")
      println(s"$countDown")

      if (countDown > 0) {
        countDown -= 1
        sender() ! Ping
      } else {
        sender() ! PoisonPill
        self ! PoisonPill
      }
  }
}

class Ponger(pinger: ActorRef) extends Actor {
  def receive = {
    case Ping =>
      println(s"${self.path} received ping")
      pinger ! Pong
  }
}

object PingPong extends App {
  val system = ActorSystem("pingpong")

  val pinger = system.actorOf(Props[Pinger], "pinger")

  // val ponger = system.actorOf(Props(classOf[Ponger], pinger), "ponger")
  val ponger = system.actorOf(Props(new Ponger(pinger)), "ponger")

  import system.dispatcher
  system.scheduler.scheduleOnce(500 millis) {
    ponger ! Ping
  }
}
