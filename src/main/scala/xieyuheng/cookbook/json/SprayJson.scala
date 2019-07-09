package xieyuheng.cookbook.json

import spray.json._
import DefaultJsonProtocol._ // if you don't supply your own Protocol (see below)

object SprayJsonApp extends App {
  val source = """{ "some": "JSON source" }"""
  val jsonAst = source.parseJson // or JsonParser(source)

  println(source.parseJson == source.parseJson)
}
