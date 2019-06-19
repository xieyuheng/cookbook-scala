package com.example

import akka.actor.{ Actor, ActorSystem, Props }
import scala.io.StdIn

object PrintMyActorRefActor {
  def props: Props =
    Props(new PrintMyActorRefActor)
}

class PrintMyActorRefActor extends Actor {
  def receive = {
    case "printit" =>
      val second = context.actorOf(Props.empty, "second-actor")
      println(s"Second: $second")
  }
}

object ActorHierarchyExperiments extends App {
  val system = ActorSystem("root")

  val first = system.actorOf(PrintMyActorRefActor.props, "first-actor")
  println(s"First: $first")
  first ! "printit"

  println(">>> Press ENTER to exit <<<")
  try StdIn.readLine()
  finally system.terminate()
}
