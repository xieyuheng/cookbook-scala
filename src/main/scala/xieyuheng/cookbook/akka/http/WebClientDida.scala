package xieyuheng.cookbook.akka

import scala.concurrent.{ExecutionContext, Future, blocking}
import scala.util.{Failure, Success}
import akka.util.{ByteString}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import HttpMethods._
import MediaTypes._
import HttpCharsets._
import HttpProtocols._

object WebClientDida extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext: ExecutionContext =
    // ExecutionContext.global
    system.dispatcher

  val response: Future[HttpResponse] =
    Http().singleRequest(
      HttpRequest(
        POST,
        uri =
          "http://api.didatravel.com/api/staticdata/GetCountryList?$format=json",
        entity = HttpEntity(
          `application/json`,
          """{ "Header": { "LicenseKey": "DidaApiTestID", "ClientID": "TestKey" } }"""
        )
        // protocol = `HTTP/1.1`
      )
    )

  response.onComplete {
    case Success(res) => println(res.entity)
    case Failure(_) => sys.error("something wrong")
  }

  response.foreach { res =>
    println(res.entity)
  }
}
