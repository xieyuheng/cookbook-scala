package xieyuheng.cookbook.akka.http

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import scala.io.StdIn

object QuickStartApp extends App {
  implicit val system = ActorSystem("QuickStart")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val route = pathPrefix("user") {
    path("hello") {
      get {
        complete(HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          "<h1>Say hello to akka-http</h1>"))
      }
    } ~ path("hi") {
      get {
        complete(HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          "hi"))
      }
    }
  }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/")
  // println(s"Press RETURN to stop...")
  // StdIn.readLine() // let it run until user presses return
  // bindingFuture
  //   // .flatMap(_.unbind()) // trigger unbinding from the port
  //   .flatMap(x => {
  //     println(x)
  //     x.unbind()
  //   }) // trigger unbinding from the port
  //   .onComplete(_ => system.terminate()) // and shutdown when done

}
