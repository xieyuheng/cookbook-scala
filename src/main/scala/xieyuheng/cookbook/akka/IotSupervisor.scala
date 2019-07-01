package xieyuheng.cookbook.akka

import akka.actor.{ Actor, ActorLogging, Props }

object IotSupervisor {
  def props() = Props(new IotSupervisor)
}

class IotSupervisor extends Actor with ActorLogging {
  override def preStart() = log.info("IoT Application started")
  override def postStop() = log.info("IoT Application stopped")

  def receive = Actor.emptyBehavior
}
