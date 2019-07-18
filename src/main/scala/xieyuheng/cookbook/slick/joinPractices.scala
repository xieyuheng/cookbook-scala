package xieyuheng.cookbook.slick

import slick.jdbc.MySQLProfile.api._
import slick.model.{ForeignKeyAction}
import scala.concurrent.{Future, Await, blocking}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

case class Department(DepartmentId: Long, DepartmentName: String)

class DepartmentTable(tag: Tag) extends Table[Department](tag, "Department") {
  def DepartmentId = column[Long]("DepartmentId", O.PrimaryKey)
  def DepartmentName = column[String]("DepartmentName")

  def * = (DepartmentId, DepartmentName).mapTo[Department]
}

case class Employee(
    EmployeeId: Long,
    LastName: String,
    Country: String,
    DepartmentId: Option[Long]
)

class EmployeeTable(tag: Tag) extends Table[Employee](tag, "Employee") {
  def EmployeeId = column[Long]("EmployeeId", O.PrimaryKey)
  def LastName = column[String]("LastName")
  def Country = column[String]("Country")
  def DepartmentId = column[Long]("DepartmentId")

  def * =
    (
      EmployeeId,
      LastName,
      Country,
      DepartmentId.?
    ).mapTo[Employee]

  def DepartmentFk =
    foreignKey("DepartmentFk", DepartmentId, TableQuery[DepartmentTable])(
      _.DepartmentId,
      onDelete = ForeignKeyAction.Cascade
    )
}

object joinPracticesData {
  def departments =
    Seq(
      Department(31, "Sales"),
      Department(33, "Engineering"),
      Department(34, "Clerical"),
      Department(35, "Marketing")
    )

  def employees =
    Seq(
      Employee(123, "Rafferty", "Australia", Some(31)),
      Employee(124, "Jones", "Australia", Some(33)),
      Employee(145, "Heisenberg", "Australia", Some(33)),
      Employee(201, "Robinson", "United States", Some(34)),
      Employee(305, "Smith", "Germany", Some(34)),
      Employee(306, "Williams", "Germany", None)
    )
}

object JoinPracticesApp extends App {
  def initDepartmentTable =
    for {
      tryCreate <- TableQuery[DepartmentTable].schema.create.asTry
      deleted <- TableQuery[DepartmentTable].delete
      inserted <- TableQuery[DepartmentTable] ++= joinPracticesData.departments
    } yield (
      "DepartmentTable",
      Map(
        "tryCreate" -> tryCreate,
        "deleted" -> deleted,
        "inserted" -> inserted
      )
    )

  def initEmployeeTable =
    for {
      tryCreate <- TableQuery[EmployeeTable].schema.create.asTry
      deleted <- TableQuery[EmployeeTable].delete
      inserted <- TableQuery[EmployeeTable] ++= joinPracticesData.employees
    } yield (
      "EmployeeTable",
      Map(
        "tryCreate" -> tryCreate,
        "deleted" -> deleted,
        "inserted" -> inserted))

  def crossJoin =
    TableQuery[EmployeeTable]
      .join(TableQuery[DepartmentTable])

  def innerJoin =
    TableQuery[EmployeeTable]
      .join(TableQuery[DepartmentTable])
      .on { _.DepartmentId === _.DepartmentId }
      .map { case (e, d) => (e.LastName, e.DepartmentId, d.DepartmentName) }

  def leftJoin =
    TableQuery[EmployeeTable]
      .joinLeft(TableQuery[DepartmentTable])
      .on { _.DepartmentId === _.DepartmentId }

  def rightJoin =
    TableQuery[EmployeeTable]
      .joinRight(TableQuery[DepartmentTable])
      .on { _.DepartmentId === _.DepartmentId }

  def selfJoin =
    TableQuery[EmployeeTable]
      .join(TableQuery[EmployeeTable])
      .on { _.Country === _.Country }
      .filter { case (a, b) => a.EmployeeId < b.EmployeeId }
      .sortBy { case (a, b) => (a.EmployeeId.desc, b.EmployeeId.desc) }

  def monadicJoin =
    for {
      e <- TableQuery[EmployeeTable] if e.Country === "Australia"
      d <- e.DepartmentFk
    } yield (e, d)

  val db = Database.forConfig("CookbookSlick")

  db.run(
      for {
        i1 <- initDepartmentTable
        i2 <- initEmployeeTable
        cross <- crossJoin.result
        inner <- innerJoin.result
        left <- leftJoin.result
        right <- rightJoin.result
        self <- selfJoin.result
        monadic <- monadicJoin.result
      } yield (i1, i2, cross, inner, left, right, self, monadic)
    )
    .onComplete {
      case Success((i1, i2, cross, inner, left, right, self, monadic)) => {
        println((i1, i2))
        cross.foreach(println)
        inner.foreach(println)
        left.foreach { case (e, d) => println(e.LastName, e.DepartmentId, d) }
        right.foreach { case (e, d) => println(e, d.DepartmentName) }
        self.foreach {
          case (a, b) =>
            println(
              a.EmployeeId,
              a.LastName,
              b.EmployeeId,
              b.LastName,
              a.Country)
        }
        monadic.foreach { case (e, d) => println(e, d.DepartmentName) }
      }
      case Failure(error) => println(error)
    }
}
