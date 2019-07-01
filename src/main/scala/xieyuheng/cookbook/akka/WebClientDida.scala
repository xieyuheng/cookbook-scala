package xieyuheng.cookbook.akka

import scala.concurrent.{ ExecutionContext, Future, blocking }
import scala.util.{ Failure, Success }
import akka.util.{ ByteString }
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import HttpMethods._
import MediaTypes._
import HttpCharsets._
import HttpProtocols._

object WebClientDida {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext: ExecutionContext =
      // ExecutionContext.global
      system.dispatcher

    val response: Future[HttpResponse] =
      Http().singleRequest(HttpRequest(
        POST,
        uri = "http://api.didatravel.com/api/staticdata/GetCountryList?$format=json",
        entity = HttpEntity(
          `application/json`,
          """{ "Header": { "LicenseKey": "DidaApiTestID", "ClientID": "TestKey" } }"""),
        // protocol = `HTTP/1.1`
      ))

    response.onComplete {
      case Success(res) => println(res.entity)
      case Failure(_) => sys.error("something wrong")
    }

    response.foreach { res => println(res.entity) }
  }
}

object MarshallerExample extends App {
  import scala.concurrent.Await
  import scala.concurrent.duration._
  import akka.http.scaladsl.marshalling.Marshal
  import akka.http.scaladsl.model._

  implicit val system = ActorSystem()
  import system.dispatcher // ExecutionContext

  val string = "Yeah"
  val entityFuture = Marshal(string).to[MessageEntity]
  val entity = Await.result(entityFuture, 1.second) // don't block in non-test code!
  println(entity)
  // entity.contentType shouldEqual ContentTypes.`text/plain(UTF-8)`

  val errorMsg = "Easy, pal!"
  val responseFuture = Marshal(420 -> errorMsg).to[HttpResponse]
  val response = Await.result(responseFuture, 1.second) // don't block in non-test code!
  println(response)
  // response.status shouldEqual StatusCodes.EnhanceYourCalm
  // response.entity.contentType shouldEqual ContentTypes.`text/plain(UTF-8)`

  val request = HttpRequest(headers = List(headers.Accept(MediaTypes.`application/json`)))
  val responseText = "Plaintext"
  val respFuture = Marshal(responseText).toResponseFor(request) // with content negotiation!
  println(respFuture)
  // a[Marshal.UnacceptableResponseContentTypeException] should be thrownBy {
  //   Await.result(respFuture, 1.second) // client requested JSON, we only have text/plain!
  // }
}
