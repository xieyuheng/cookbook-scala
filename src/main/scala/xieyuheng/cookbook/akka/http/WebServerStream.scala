package xieyuheng.cookbook.akka.http

import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.util.ByteString
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpEntity, ContentTypes }
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.util.Random
import scala.io.StdIn

object WebServerStream extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  // streams are re-usable so we can define it here
  // and use it for every request
  val numbers =
    Source.fromIterator(() => Iterator.continually(Random.nextInt()))

  val route: Route = {
    path("random") {
      get {
        complete(HttpEntity(
          ContentTypes.`text/plain(UTF-8)`,
          // transform each number to a chunk of bytes
          numbers.map(n => ByteString(s"$n\n"))))
      }
    }
  }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done
}
