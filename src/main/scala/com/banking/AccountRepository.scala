package com.banking

import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class Account(balance: Int, id: Long = 0)

class AccountRepository {

  private val config: DatabaseConfig[H2Profile] = DatabaseConfig.forConfig("h2db")

  import config.profile.api._

  private class AccountTable(tag: Tag) extends Table[Account](tag, "accounts") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def balance = column[Int]("balance")

    def * = (balance, id) <> (Account.tupled, Account.unapply)
  }

  private object accounts extends TableQuery(new AccountTable(_)) {

    val createTable = this.schema.create

    val addFixtures = this ++= Seq(
      Account(1200, 1),
      Account(2400, 2),
      Account(3600, 3)
    )

    def getOption(id: Long) = this.filter(_.id === id).result.headOption

    def get(id: Long) = this.getOption(id).collect {
      case Some(account) => account
      case None => throw new RuntimeException(s"Account $id not found")
    }
  }

  def initialize: Future[Option[Int]] = config.db.run(accounts.createTable andThen accounts.addFixtures)

  def get(id: Long): Future[Option[Account]] = config.db.run(accounts.getOption(id))

  def transfer(fromId: Long, toId: Long, amount: Int): Future[Int] = {
    val action = (for {
      sender <- accounts.get(fromId)
      receiver <- accounts.get(toId)
    } yield (sender, receiver)).flatMap {
      case (sender, receiver) =>
        if (sender.balance >= amount) {
          changeBalance(sender.id, sender.balance - amount)
            .andThen(changeBalance(receiver.id, receiver.balance + amount))
        } else {
          throw new RuntimeException("Insufficient funds")
        }
    }

    config.db.run(action.transactionally)
  }

  private def changeBalance(id: Long, balance: Int) = accounts.filter(_.id === id).map(_.balance).update(balance)
}
