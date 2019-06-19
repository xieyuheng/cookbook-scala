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

object ActorHierarchyExperiments extends App {
  val system = ActorSystem("root")

  // val first = system.actorOf(StartStopActor1.props, "first")
  // first ! "stop"

  val supervisingActor = system.actorOf(SupervisingActor.props, "supervising-actor")
  supervisingActor ! "failChild"

  val first = system.actorOf(PrintMyActorRefActor.props, "first-actor")
  println(s"First: $first")
  first ! "printit"

  println(">>> press enter to exit <<<")
  try StdIn.readLine()
  finally system.terminate()
}
