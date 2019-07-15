package xieyuheng.cookbook.design

import scala.util.DynamicVariable

class StackableVariable[A](init: A) {
  private var values: List[A] = List(init)

  def value: A = values.head

  def withValue[B](newValue: A)(op: => B): B = {
    values = newValue :: values
    try op finally values = values.tail
  }
}

class Signal[A](exp: => A) {
  import Signal._

  private var currentExp: () => A = _
  private var currentValue: A = _
  private var observers: Set[Signal[_]] = Set()

  update(exp)

  protected def update(exp: => A): Unit = {
    currentExp = () => exp
    computeValue()
  }

  protected def computeValue(): Unit = {
    val newValue = caller.withValue(this)(currentExp())
    if (newValue != currentValue) {
      currentValue = newValue
      val tmp = observers
      observers = Set()
      tmp.foreach(_.computeValue())
    }
  }

  def apply() = {
    observers += caller.value
    assert(!caller.value.observers.contains(this),
      "cyclic signal definition")
    currentValue
  }
}

object Signal {
  // private val caller = new StackableVariable[Signal[_]](NoSignal)
  private val caller = new DynamicVariable[Signal[_]](NoSignal)
  def apply[A](exp: => A) = new Signal(exp)
}

// sentinel object
object NoSignal extends Signal[Nothing](???) {
  override def computeValue() = ()
}

class Var[A](exp: => A) extends Signal[A](exp) {
  override def update(exp: => A): Unit = super.update(exp)
}

object Var {
  def apply[A](exp: => A) = new Var(exp)
}

object SignalExample extends App {
  class BankAccount {
    val balance: Var[Int] = Var(0)

    def deposit(amount: Int): Unit =
      if (amount > 0) {
        val b = balance()
        balance() = b + amount
      }

    def withdraw(amount: Int): Unit =
      if (0 < amount && amount <= balance()) {
        val b = balance()
        balance() = b - amount
      } else throw new Error("insufficient funds")
  }

  def consolidated(accounts: List[BankAccount]): Signal[Int] = {
    Signal(accounts.map(_.balance()).foldLeft(0)(_ + _))
  }

  val a = new BankAccount()
  val b = new BankAccount()
  val c = consolidated(List(a, b))

  println(c())
  a.deposit(20)
  println(c())
  b.deposit(30)
  println(c())

//   val x = Var(3)
//   val y = Var(5)
//   val z = Signal(x() + y())

//   println(z())
//   x.update(4)
//   println(z())
}
