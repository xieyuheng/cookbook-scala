package xieyuheng.cookbook.akka.stream

import akka.stream.{ActorMaterializer}
import akka.stream.scaladsl.{Source, Sink, Flow, Keep, RunnableGraph}
import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.util.ByteString
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import java.nio.file.Paths

object StreamQuickstart extends App {
  val source: Source[Int, NotUsed] = Source(1 to 100)
  val flow: Flow[Int, Int, NotUsed] = Flow[Int].map(2 * _)
  val sink: Sink[Int, Future[Int]] = Sink.fold(0)(_ + _)
  val runnable: RunnableGraph[Future[Int]] =
    source.async
      .via(flow)
      .toMat(sink)(Keep.right)

  implicit val system = ActorSystem("StreamQuickstart")
  implicit val materializer = ActorMaterializer()
  implicit val exeCtx = system.dispatcher

  val sum: Future[Int] = runnable.run()

  sum.onComplete {
    case Success(int) => {
      println(int)
      system.terminate()
    }
    case Failure(err) => {
      println(err)
      system.terminate()
    }
  }
}
