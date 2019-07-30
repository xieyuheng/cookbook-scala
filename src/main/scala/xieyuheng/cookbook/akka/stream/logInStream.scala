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

object logInStream extends App {
  implicit val system = ActorSystem("logInStream")
  implicit val mat = ActorMaterializer()
  implicit val ctx = system.dispatcher
  implicit val logSource = new LogSource[AnyRef] {
    def genString(o: AnyRef) = o.getClass.getName
  }
  implicit val log = Logging(system, this)

  val source = Source(-5 to 5)

  val flow = Flow[Int]
    .map(100 / _) //throwing ArithmeticException: / by zero
    .log(name = "logInStream", (element) => s">>> ${element} <<<")
    .addAttributes(
      Attributes.logLevels(
        onElement = Attributes.LogLevels.Info,
        onFailure = Attributes.LogLevels.Error,
        onFinish = Attributes.LogLevels.Info))

  val sink = Sink.foreach(println)

  source
    .via(flow)
    .runWith(sink)
    .onComplete { case (result) =>
      println(result)
      system.terminate()
    }
}
