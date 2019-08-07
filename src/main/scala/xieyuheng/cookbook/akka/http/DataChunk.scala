package xieyuheng.cookbook.akka.http

import akka.NotUsed
import akka.util.ByteString
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Source, Flow }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.common.{ EntityStreamingSupport, JsonEntityStreamingSupport }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.io.StdIn

/**
  * https://richardimaoka.github.io/blog/akka-http-response-streaming
  */

case class DataChunk(id: Int, content: String)

object DataChunk {
  import spray.json.DefaultJsonProtocol._
  import spray.json.RootJsonFormat

  implicit val dataChunkJsonFormat: RootJsonFormat[DataChunk] =
    jsonFormat2(DataChunk.apply)

  def source: Source[DataChunk, NotUsed] = {
    Source(List(
      DataChunk(1, "the first"),
      DataChunk(2, "the second"),
      DataChunk(3, "the thrid"),
      DataChunk(4, "the fourth"),
      DataChunk(5, "the fifth"),
      DataChunk(6, "the sixth"))
    ).throttle(1, 1.second)
  }
}

object DataChunkApp extends App {
  implicit val system = ActorSystem("DataChunkApp")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  implicit val jsonStreamingSupport: JsonEntityStreamingSupport =
    EntityStreamingSupport
      .json()
      .withFramingRenderer(
        // new-line delimited JSON streaming
        Flow[ByteString].map(byteString => byteString ++ ByteString("\n")))

  val route: Route = get {
    complete(DataChunk.source)
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
