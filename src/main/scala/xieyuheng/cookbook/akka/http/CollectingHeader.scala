package xieyuheng.cookbook.akka

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.`Set-Cookie`
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.Future

object CollectingHeader extends App {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val responseFuture: Future[HttpResponse] =
    Http().singleRequest(HttpRequest(uri = "https://akka.io"))

  responseFuture.map {
    case response @ HttpResponse(StatusCodes.OK, _, _, _) =>
      val setCookies = response.headers[`Set-Cookie`]
      println(s"Cookies set by a server: $setCookies")
      response.discardEntityBytes()
    case _ => sys.error("something wrong")
  }
}
