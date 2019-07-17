package xieyuheng.cookbook.slick

import slick.jdbc.MySQLProfile.api._
import slick.model.{ ForeignKeyAction }
import scala.concurrent.{ Future, Await, blocking }
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }

case class Department(
  DepartmentId: Long,
  DepartmentName: String)

class DepartmentTable(tag: Tag)
    extends Table[Department](tag, "Department") {
  def DepartmentId = column[Long]("DepartmentId", O.PrimaryKey)
  def DepartmentName = column[String]("DepartmentName")

  def * = (
    DepartmentId,
    DepartmentName,
  ).mapTo[Department]
}

case class Employee(
  EmployeeId: Long,
  LastName: String,
  Country: String,
  DepartmentId: Option[Long])

class EmployeeTable(tag: Tag)
    extends Table[Employee](tag, "Employee") {
  def EmployeeId = column[Long]("EmployeeId", O.PrimaryKey)
  def LastName = column[String]("LastName")
  def Country = column[String]("Country")
  def DepartmentId = column[Long]("DepartmentId")

  def * = (
    EmployeeId,
    LastName,
    Country,
    DepartmentId.?,
  ).mapTo[Employee]

  def DepartmentFk = foreignKey(
    "DepartmentFk",
    DepartmentId,
    TableQuery[DepartmentTable])(_.DepartmentId,
      onDelete=ForeignKeyAction.Cascade)
}

object JoinPracticesData {
  def departments = Seq(
    Department(31, "Sales"),
    Department(33, "Engineering"),
    Department(34, "Clerical"),
    Department(35, "Marketing"))

  def employees = Seq(
    Employee(123, ",Rafferty", "Australia", Some(31)),
    Employee(124, "Jones", "Australia", Some(33)),
    Employee(145, "Heisenberg", "Australia", Some(33)),
    Employee(201, "Robinson", "United States", Some(34)),
    Employee(305, "Smith", "Germany", Some(34)),
    Employee(306, "Williams", "Germany", None))
}

object JoinPracticesApp extends App {
  val db = Database.forConfig("CookbookSlick")

  def initDepartmentTable = for {
    tryCreate <- TableQuery[DepartmentTable].schema.create.asTry
    deleted <- TableQuery[DepartmentTable].delete
    inserted <- TableQuery[DepartmentTable] ++= JoinPracticesData.departments
  } yield ("DepartmentTable", Map(
    "tryCreate" -> tryCreate,
    "deleted" -> deleted,
    "inserted" -> inserted))

  def initEmployeeTable = for {
    tryCreate <- TableQuery[EmployeeTable].schema.create.asTry
    deleted <- TableQuery[EmployeeTable].delete
    inserted <- TableQuery[EmployeeTable] ++= JoinPracticesData.employees
  } yield ("EmployeeTable", Map(
    "tryCreate" -> tryCreate,
    "deleted" -> deleted,
    "inserted" -> inserted))

  db.run(
    for {
      i1 <- initDepartmentTable
      i2 <- initEmployeeTable
    } yield (i1, i2)
  ).onComplete { println }
}
