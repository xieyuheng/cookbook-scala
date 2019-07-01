package xieyuheng.cookbook.akka

import akka.actor.{ Actor, ActorSystem, Props }
import scala.io.StdIn

object StartStopActor1 {
  def props = Props(new StartStopActor1)
}

class StartStopActor1 extends Actor {
  override def preStart() = {
    println("first started")
    context.actorOf(StartStopActor2.props, "second")
  }

  override def postStop() = println("first stopped")

  def receive = {
    case "stop" => context.stop(self)
  }
}

object StartStopActor2 {
  def props = Props(new StartStopActor2)
}

class StartStopActor2 extends Actor {
  override def preStart() = println("second started")
  override def postStop() = println("second stopped")

  def receive = Actor.emptyBehavior
}

object ActorLifecycleExperiments extends App {
  val system = ActorSystem("root")

  val first = system.actorOf(StartStopActor1.props, "first")
  first ! "stop"

  println(">>> press enter to exit <<<")
  try StdIn.readLine()
  finally system.terminate()
}
