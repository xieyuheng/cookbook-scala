package xieyuheng.cookbook.akka

import akka.actor.{Actor, ActorSystem, Props}
import scala.io.StdIn

object SupervisingActor {
  def props = Props(new SupervisingActor)
}

class SupervisingActor extends Actor {
  val child = context.actorOf(SupervisedActor.props, "supervised-actor")

  override def receive = {
    case "failChild" => child ! "fail"
  }
}

object SupervisedActor {
  def props = Props(new SupervisedActor)
}

class SupervisedActor extends Actor {
  override def preStart() = println("supervised actor started")
  override def postStop() = println("supervised actor stopped")

  def receive = {
    case "fail" =>
      println("supervised actor fails now")
      throw new Exception("I failed!")
  }
}

object ActorSuperviseExperiments extends App {
  val system = ActorSystem("root")

  val supervisingActor =
    system.actorOf(SupervisingActor.props, "supervising-actor")
  supervisingActor ! "failChild"

  println(">>> press enter to exit <<<")
  try StdIn.readLine()
  finally system.terminate()
}
