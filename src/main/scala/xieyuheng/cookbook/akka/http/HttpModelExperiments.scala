package xieyuheng.cookbook.akka

import akka.http.scaladsl.model._
import HttpMethods._
import MediaTypes._
import HttpCharsets._
import HttpProtocols._

object HttpModelExperiments extends App {

  val userData = akka.util.ByteString("abc")
  val authorization = headers.Authorization(headers.BasicHttpCredentials("user", "pass"))
  val req = HttpRequest(
    PUT,
    uri = "/user",
    entity = HttpEntity(`text/plain` withCharset `UTF-8`, userData),
    headers = List(authorization),
    protocol = `HTTP/1.0`)
  println(req)

  {
    import akka.http.scaladsl.model.headers.`Raw-Request-URI`
    val req = HttpRequest(uri = "/ignored", headers = List(`Raw-Request-URI`("/a/b%2Bc")))
    println(req)
  }

  {
    import StatusCodes._
    // simple OK response without data created using the integer status code
    println(HttpResponse(200))
    // 404 response created using the named StatusCode constant
    println(HttpResponse(NotFound))
    // 404 response with a body explaining the error
    println(HttpResponse(404, entity = "Unfortunately, the resource couldn't be found."))
    // A redirecting response containing an extra header
    val locationHeader = headers.Location("http://example.com/other")
    println(HttpResponse(Found, headers = List(locationHeader)))
  }
}
