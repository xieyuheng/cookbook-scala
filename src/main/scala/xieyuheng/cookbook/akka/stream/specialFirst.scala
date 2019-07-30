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

object specialFirst extends App {
  implicit val system = ActorSystem("specialFirst")
  implicit val mat = ActorMaterializer()
  implicit val ctx = system.dispatcher

  Source(List("title", "1", "2", "3", "4", "5"))
    .prefixAndTail(1).flatMapConcat { case (head, tail) =>
      val x = head(0)
      tail.via(Flow[String].map(i => (x, i)))
    }
    .runForeach(println)
    .onComplete { case (result) =>
      println(result)
      system.terminate()
    }

}
