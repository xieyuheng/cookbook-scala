package xieyuheng.cookbook.akka.stream

import akka.{ Done, NotUsed }
import akka.actor.ActorSystem
import akka.util.ByteString
import akka.stream.{ ActorMaterializer, IOResult }
import akka.stream.scaladsl.{ Source, Sink, Flow, Keep, RunnableGraph, FileIO }
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{ Failure, Success }
import java.nio.file.Paths

object factorials extends App {
  implicit val system = ActorSystem("factorials")
  implicit val mat = ActorMaterializer()
  implicit val ctx = system.dispatcher

  val source: Source[Int, NotUsed] =
    Source(1 to 100)
  val factorials: Source[BigInt, NotUsed] =
    source.scan(BigInt(1)) { case (acc, next) => acc * next }

  // factorials
  //   .map(num => ByteString(s"$num\n"))
  //   .runWith(FileIO.toPath(Paths.get("factorials.txt")))
  //   .onComplete { case (result) =>
  //     println(result)
  //     system.terminate()
  //   }

  // Reusable Pieces
  // def lineSink(filename: String): Sink[String, Future[IOResult]] =
  //   Flow[String]
  //     .map(s => ByteString(s + "\n"))
  //     .toMat(FileIO.toPath(Paths.get(filename)))(Keep.right)

  // factorials
  //   .map(_.toString)
  // // .toMat(lineSink("factorials.txt"))(Keep.right)
  // // .run()
  //   .runWith(lineSink("factorials.txt"))
  //   .onComplete { case (result) =>
  //     println(result)
  //     system.terminate()
  //   }

  // factorials
  //   .runForeach(println)
  //   .onComplete { case (result) =>
  //     println(result)
  //     system.terminate()
  //   }

  // we are in fact dealing with streams that can flow at a certain speed:
  //   we use the throttle operator to slow down the stream to 1 element per second.
  factorials
    .zipWith(Source(0 to 100)) { case (num, idx) => s"$idx! = $num" }
    .throttle(1, 1.second)
    .runForeach(println)
    .onComplete { case (result) =>
      println(result)
      system.terminate()
    }

}
