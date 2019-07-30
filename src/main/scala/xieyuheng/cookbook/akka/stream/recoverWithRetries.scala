package xieyuheng.cookbook.akka.stream

import akka.{ Done, NotUsed }
import akka.actor.ActorSystem
import akka.util.ByteString
import akka.stream.{ ActorMaterializer, IOResult, Attributes }
import akka.stream.scaladsl.{ Source, Sink, Flow, Keep, RunnableGraph, FileIO }
import akka.event.{ Logging, LogSource, LoggingAdapter }
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

object recoverWithRetries extends App {
  implicit val system = ActorSystem("recoverWithRetries")
  implicit val mat = ActorMaterializer()
  implicit val ctx = system.dispatcher
  implicit val logSource = new LogSource[AnyRef] {
    def genString(o: AnyRef) = o.getClass.getName
  }
  implicit val log = Logging(system, this)

  val source = Source(0 to 10)
    .map { n =>
      if (n < 5) {
        n.toString
      } else {
        throw new RuntimeException("Boom!")
      }
    }

  val planB = Source(List("five", "six", "seven", "eight"))

  val flow = Flow[String]
    .recoverWithRetries (attempts = 1, {
      case _: Exception => planB
    })

  val sink = Sink.foreach(println)

  source
    .via(flow)
    .runWith(sink)
    .onComplete { case (result) =>
      println(result)
      system.terminate()
    }
}
