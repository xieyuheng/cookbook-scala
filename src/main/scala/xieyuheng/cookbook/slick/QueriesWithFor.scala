package xieyuheng.cookbook.slick

object QueriesWithFor extends App {
  case class Department(
    DepartmentId: Long,
    DepartmentName: String,
    Employees: List[Employee])

  case class Employee(EmployeeId: Long, LastName: String, Country: String)

  def departments =
    Set(
      Department(31, "Sales", List(Employee(123, "Rafferty", "Australia"))),
      Department(33, "Engineering", List(
        Employee(124, "Jones", "Australia"),
        Employee(145, "Heisenberg", "Australia"))),
      Department(34, "Clerical", List(
        Employee(201, "Robinson", "United States"),
        Employee(305, "Smith", "Germany"))),
      Department(35, "Marketing", List()))

  val result = for {
    d <- departments
    e <- d.Employees if e.Country == "Australia"
  } yield (e, d)

  result.foreach { case (e, d) => println(e, d.DepartmentName) }
}
