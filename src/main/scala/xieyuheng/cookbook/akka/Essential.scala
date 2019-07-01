package xieyuheng.cookbook.akka

object person {
  val firstName = "Dave"
  val lastName = "Gurnell"
}

object alien {
  def greet(p: person.type) =
    "Greetings, " + p.firstName + " " + p.lastName
}

object Main extends App {
  alien.greet(person)
}
