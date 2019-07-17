package xieyuheng.cookbook.slick

import slick.jdbc.MySQLProfile.api._
import slick.model.{ ForeignKeyAction }
import scala.concurrent.{ Future, Await, blocking }
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }
