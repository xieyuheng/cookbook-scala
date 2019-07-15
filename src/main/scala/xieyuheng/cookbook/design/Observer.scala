package xieyuheng.cookbook.design

trait Publisher {
  private var subscribers: Set[Subscriber] = Set()

  def subscribe(subscriber: Subscriber): Unit =
    subscribers += subscriber

  def unsubscribe(subscriber: Subscriber): Unit =
    subscribers -= subscriber

  def publish(): Unit =
    subscribers.foreach(_.handler(this))

}

trait Subscriber {
  def handler(pub: Publisher)
}

object ObserverExample extends App {
  class BankAccount extends Publisher {
    private var balance: Int = 0

    def currentBalance: Int = balance

    def deposit(amount: Int): Unit =
      if (amount > 0) {
        balance += amount
        publish()
      }

    def withdraw(amount: Int): Unit =
      if (0 < amount && amount <= balance) {
        balance -= amount
        publish()
      } else throw new Error("insufficient funds")
  }

  class Consolidator(observed: List[BankAccount]) extends Subscriber {
    observed.foreach(_.subscribe(this))

    private var total: Int = _
    update()

    private def update(): Unit =
      total = observed.map(_.currentBalance).sum

    def handler(pub: Publisher): Unit = update()

    def totalBalance: Int = total
  }

  val a = new BankAccount()
  val b = new BankAccount()
  val c = new Consolidator(List(a, b))

  println(c.totalBalance)
  a.deposit(20)
  println(c.totalBalance)
  b.deposit(30)
  println(c.totalBalance)
}
