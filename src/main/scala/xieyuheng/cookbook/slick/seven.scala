package xieyuheng.cookbook.slick

import slick.jdbc.MySQLProfile.api._
import scala.concurrent.{Future, Await, blocking}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

case class Country(countryCode: String, countryName: String)

class CountryTable(tag: Tag) extends Table[Country](tag, "countries") {
  def countryCode = column[String]("country_code", O.PrimaryKey)
  def countryName = column[String]("country_name")

  def * = (countryCode, countryName).mapTo[Country]
}

object Seven extends App {
  lazy val countries = TableQuery[CountryTable]

  val db = Database.forConfig("CookbookSlick")

  db.run(countries.schema.create).onComplete { println }
}
