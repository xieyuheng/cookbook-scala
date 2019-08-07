package xieyuheng.cookbook.akka.http

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import scala.concurrent.{ ExecutionContext, Future }
import scala.io.StdIn

/**
  * https://richardimaoka.github.io/blog/akka-http-quickstart
  */

case class User(
  familyName: String,
  givenName: String,
  age: Int)

object User {
  import spray.json.DefaultJsonProtocol._
  import spray.json.RootJsonFormat

  implicit val userJsonFormat: RootJsonFormat[User] =
    jsonFormat3(User.apply)
}

object QuickStartApp extends App {
  implicit val system = ActorSystem("QuickStart")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val route: Route = pathPrefix("user") {
    path("xie") {
      get {
        complete(User("xie", "yuheng", 400))
      }
    } ~
    path("yu") {
      get {
        complete(User("yu", "hengxie", 300))
      }
    } ~
    path("heng") {
      get {
        complete(User("heng", "yuxie", 200))
      }
    }
  }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
  println(s"Server online at http://localhost:8080/")
  println(s"Press RETURN to stop...")
  StdIn.readLine() // let it run until user presses return

  bindingFuture
    .flatMap(binding => {
      // trigger unbinding from the port
      println(s"unbind: ${binding}")
      binding.unbind()
    })
    .onComplete(result => {
      println(s"result: ${result}")
      system.terminate()
    })
}
