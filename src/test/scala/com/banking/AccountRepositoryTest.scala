package com.banking

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures._

import scala.concurrent.Await
import scala.concurrent.duration._

class AccountRepositoryTest extends WordSpec with BeforeAndAfterAll {

  val repository = new AccountRepository()

  override def beforeAll = {
    repository.initialize
  }

  "AccountRepositoryTest" can {

    "get" should {

      "return Account by specified id" in {
        whenReady(repository.get(3)) { case Some(account) =>
          assert(account.balance == 3600)
        }
      }

      "fail when Account with specified id isn't found" in {
        whenReady(repository.get(42)) { result =>
          assert(result.isEmpty)
        }
      }
    }

    "transfer" should {

      "send money between accounts" in {
        whenReady(repository.transfer(1, 2, 100)) { _ =>
          assert(Await.result(repository.get(1), 5.second).get.balance == 1100)
          assert(Await.result(repository.get(2), 5.second).get.balance == 2500)
        }
      }

      "fail when sender doesn't exist" in {
        whenReady(repository.transfer(42, 2, 20).failed) { e =>
          assert(e.getMessage == "Account 42 not found")
        }
      }

      "fail when receiver doesn't exist" in {
        whenReady(repository.transfer(2, 42, 20).failed) { e =>
          assert(e.getMessage == "Account 42 not found")
        }
      }

      "fail when sender hasn't enough money" in {
        whenReady(repository.transfer(1, 2, 10000).failed) { e =>
          assert(e.getMessage == "Insufficient funds")
        }
      }
    }

  }
}
