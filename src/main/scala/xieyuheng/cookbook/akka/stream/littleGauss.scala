package xieyuheng.cookbook.akka.stream

import akka.{ Done, NotUsed }
import akka.actor.ActorSystem
import akka.util.ByteString
import akka.stream.{ ActorMaterializer, IOResult }
import akka.stream.scaladsl.{ Source, Sink, Flow, Keep, RunnableGraph, FileIO }
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

object littleGauss extends App {
  implicit val system = ActorSystem("littleGauss")
  implicit val mat = ActorMaterializer()
  implicit val ctx = system.dispatcher

  val source: Source[Int, NotUsed] = Source(1 to 100)
  val flow: Flow[Int, Int, NotUsed] = Flow[Int].map(_ + 1)
  val sink: Sink[Int, Future[Int]] = Sink.fold(0)(_ + _)
  val runnable: RunnableGraph[Future[Int]] =
    source.async
      .via(flow)
      .toMat(sink)(Keep.right)

  runnable
    .run()
    .onComplete { case (result) =>
      println(result)
      system.terminate()
    }
}
