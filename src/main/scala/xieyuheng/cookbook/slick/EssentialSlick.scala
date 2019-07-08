package xieyuheng.cookbook.slick

import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ Future, Await }
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object EssentialSlick extends App {

  case class Message(
    sender: String,
    content: String,
    id: Long = 0L)

  class MessageTable(
    tag: Tag
  ) extends Table[Message](tag, "message") {
    def id      = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def sender  = column[String]("sender")
    def content = column[String]("content")

    def * = (sender, content, id).mapTo[Message]
  }

  def freshTestData = Seq(
    Message("Dave", "Hello, HAL. Do you read me, HAL?"),
    Message("HAL",  "Affirmative, Dave. I read you."),
    Message("Dave", "Open the pod bay doors, HAL."),
    Message("HAL",  "I'm sorry, Dave. I'm afraid I can't do that."))

  def exec[T](program: DBIO[T]): T = {
    val res: Future[T] = db.run(program)
    Await.result(res, 100 seconds)
  }

  lazy val messages = TableQuery[MessageTable]

  val db = Database.forConfig("CookbookSlick")

  // exec(messages.schema.create)

  print("STATEMENTS: ")
  println(messages.result.statements.mkString)

  exec(messages += Message("Xie",  "haha!"))

  exec(messages ++= freshTestData)

//   exec(
//     messages
//       .result
//   ) foreach { println }
//   exec(
//     messages
//       .filter(_.sender === "HAL")
//       .result
//   ) foreach { println }
//   exec(
//     messages
//       .filter(_.sender === "Dave")
//       .result
//   ) foreach { println }

//   exec(messages += Message("Dave","What if I say 'Pretty please'?"))

  val query = for {
    message <- messages if message.sender === "Xie"
  } yield message

  exec(query.sortBy(m => (m.sender, m.content)).take(3).result).foreach { println }

  exec(Query("1").result)
}
