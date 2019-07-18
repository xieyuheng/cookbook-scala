package xieyuheng.cookbook.slick

import slick.jdbc.MySQLProfile.api._
import scala.concurrent.{ Future, Await, blocking }
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }

case class Message(
  sender: String,
  content: String,
  id: Long = 0L)

class MessageTable(
  tag: Tag
) extends Table[Message](tag, "Message") {
  def id      = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def sender  = column[String]("sender")
  def content = column[String]("content")

  def * = (sender, content, id).mapTo[Message]
}

object essentialSlickData {
  def movieScript = Seq(
    Message("Dave", "Hello, HAL. Do you read me, HAL?"),
    Message("HAL",  "Affirmative, Dave. I read you."),
    Message("Dave", "Open the pod bay doors, HAL."),
    Message("HAL",  "I'm sorry, Dave. I'm afraid I can't do that."))
}

object EssentialSlickApp extends App {
  lazy val messages = TableQuery[MessageTable]

  val db = Database.forConfig("CookbookSlick")

  db.run(messages.schema.create.asTry).onComplete { println }
  db.run(messages += Message("Xie", "haha!")).onComplete { println }

  db.run(messages ++= essentialSlickData.movieScript).onComplete { println }

  db.run(
    messages
      .result
  ).onComplete { println }

  db.run(
    messages
      .filter(_.sender === "HAL")
      .result
  ).onComplete { println }

  db.run(
    messages
      .filter(_.sender === "Dave")
      .result
  ).onComplete { println }

  db.run(
    messages += Message("Dave","What if I say 'Pretty please'?")
  ).onComplete { println }

  val query = for {
    message <- messages if message.sender === "Xie"
  } yield message

  db.run(
    query.sortBy(m => (m.sender, m.content)).take(3).result
  ).onComplete { println }

  db.run(Query("1").result).onComplete { println }
}
