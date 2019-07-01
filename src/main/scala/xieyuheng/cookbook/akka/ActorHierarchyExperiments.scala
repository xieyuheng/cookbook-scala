package xieyuheng.cookbook.akka

import akka.actor.{ Actor, ActorSystem, Props }
import scala.io.StdIn

object PrintMyActorRefActor {
  def props = Props(new PrintMyActorRefActor)
}

class PrintMyActorRefActor extends Actor {
  def receive = {
    case "printit" =>
      val second = context.actorOf(PrintMyActorRefActor.props, "second-actor")
      println(s"Second: $second")
  }
}

object ActorHierarchyExperiments extends App {
  val system = ActorSystem("root")

  val first = system.actorOf(PrintMyActorRefActor.props, "first-actor")
  println(s"First: $first")
  first ! "printit"

  println(">>> press enter to exit <<<")
  try StdIn.readLine()
  finally system.terminate()
}
