package xieyuheng.cookbook.akka.stream

import akka.{ Done, NotUsed }
import akka.actor.ActorSystem
import akka.stream.{ ActorMaterializer, ClosedShape, OverflowStrategy }
import akka.stream.scaladsl._
import scala.util.{ Failure, Success }
import scala.concurrent._
import scala.concurrent.duration._

final case class Author(handle: String)

final case class Hashtag(name: String)

final case class Tweet(author: Author, timestamp: Long, body: String) {
  def hashtags: Set[Hashtag] =
    body
      .split(" ")
      .collect {
        case t if t.startsWith("#") =>
          Hashtag(t.replaceAll("[^#\\w]", "")) }
      .toSet
}

object reactiveTweets extends App {
  val tweets: Source[Tweet, NotUsed] = Source(List(
    Tweet(Author("rolandkuhn"), System.currentTimeMillis, "#akka rocks!"),
    Tweet(Author("patriknw"), System.currentTimeMillis, "#akka !"),
    Tweet(Author("bantonsson"), System.currentTimeMillis, "#akka !"),
    Tweet(Author("drewhk"), System.currentTimeMillis, "#akka !"),
    Tweet(Author("ktosopl"), System.currentTimeMillis, "#akka on the rocks!"),
    Tweet(Author("mmartynas"), System.currentTimeMillis, "wow #akka !"),
    Tweet(Author("akkateam"), System.currentTimeMillis, "#akka rocks!"),
    Tweet(Author("bananaman"), System.currentTimeMillis, "#bananas rock!"),
    Tweet(Author("appleman"), System.currentTimeMillis, "#apples rock!"),
    Tweet(Author("drama"), System.currentTimeMillis, "we compared #apples to #oranges!")))

  val authors: Source[Author, NotUsed] =
    tweets
      .filter(_.hashtags.contains(Hashtag("#akka")))
      .map(_.author)

  implicit val system = ActorSystem("reactiveTweets")
  implicit val materializer = ActorMaterializer()
  implicit val ctx = system.dispatcher

  //   tweets
  //     .map(_.hashtags) // Get all sets of hashtags ...
  //     .reduce(_ ++ _) // ... and reduce them to a single set, removing duplicates across all tweets
  //     .mapConcat(identity) // Flatten the set of hashtags to a stream of hashtags
  //     .map(_.name.toUpperCase)
  //     .runWith(Sink.foreach(println))
  //     .onComplete { case (result) =>
  //       println(result)
  //       system.terminate()
  //     }

  //   authors
  //     .runWith(Sink.foreach(println))
  //     .onComplete { case (result) =>
  //       println(result)
  //       system.terminate()
  //     }

  //   {
  //     val writeAuthors: Sink[Author, Future[Done]] = Sink.foreach(println)
  //     val writeHashtags: Sink[Hashtag, Future[Done]] = Sink.foreach(println)

  //     val graph = GraphDSL.create() { implicit builder =>
  //       // use an implicit graph builder
  //       //   to mutably construct the graph using the ~> "edge operator"

  //       // how is this API implemented ?
  //       //   we could use similar API when constructing higher dim objects.

  //       import GraphDSL.Implicits._

  //       val broadcast = builder.add(Broadcast[Tweet](2))
  //       tweets ~> broadcast.in
  //       broadcast.out(0) ~> Flow[Tweet].map(_.author) ~> writeAuthors
  //       broadcast.out(1) ~> Flow[Tweet].mapConcat(_.hashtags.toList) ~> writeHashtags
  //       ClosedShape
  //     }

  //     RunnableGraph.fromGraph(graph).run()
  //   }

  // explicit buffer handling
  //   tweets
  //     .buffer(10, OverflowStrategy.dropHead)
  //     .runWith(Sink.ignore)

  // Materialized values
  {
    val count: Flow[Tweet, Int, NotUsed] = Flow[Tweet].map(_ => 1)
    val sumSink: Sink[Int, Future[Int]] = Sink.fold[Int, Int](0)(_ + _)

    tweets
      .via(count)
      .toMat(sumSink)(Keep.right)
      .run()
      .onComplete {
        case Success(c) =>
          println(s"Total tweets processed: $c")
          system.terminate()
        case Failure(error) =>
          println(error)
          system.terminate()
      }
  }

}
