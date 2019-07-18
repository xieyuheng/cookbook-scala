package xieyuheng.cookbook.akka

import akka.actor.{
  Actor,
  ActorSystem,
  ActorRef,
  ActorLogging,
  Props,
  Terminated
}
import scala.io.StdIn

object Indeter {
  def props = Props(new Indeter)

  case object Start
  case object Go
  case object Stop
}

class Indeter extends Actor with ActorLogging {
  import Indeter._

  var counter = 0

  def receive = {
    /*
     * We do not have indeterminacy here, because in akka,
     * message ordering is maintained per sender, receiver pair.
     */
    case Start =>
      self ! Go
      self ! Go
      self ! Go
      self ! Stop

    case Go =>
      counter += 1

    case Stop =>
      println(s"counter: $counter")
  }
}

object Indeterminacy {
  def main(args: Array[String]) {
    val system = ActorSystem("Indeterminacy")

    val indeter = system.actorOf(Indeter.props, "indeter")

    indeter ! Indeter.Start

    println(">>> press enter to exit <<<")
    try StdIn.readLine()
    finally system.terminate()
  }
}
