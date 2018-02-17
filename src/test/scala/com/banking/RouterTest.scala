package com.banking

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, OneInstancePerTest, WordSpec}

import scala.concurrent.Future

class RouterTest extends WordSpec with Matchers with ScalatestRouteTest with OneInstancePerTest with MockFactory {

  val repository = stub[AccountRepository]
  val router = new Router(repository)

  "The service" should {

    "/balance" can {

      "return account balance" in {
        (repository.get _).when(1).returns(Future.successful(Some(Account(3000))))

        Get("/balance/1") ~> router.route ~> check {
          responseAs[String] shouldEqual "3000"
        }
      }

      "return `Not Found` when account doesn't exist" in {
        (repository.get _).when(42).returns(Future.successful(None))

        Get("/balance/42") ~> router.route ~> check {
          status shouldEqual StatusCodes.NotFound
        }
      }

    }

    "/transfer" can {

      "transfer money successfully" in {
        (repository.transfer _).when(1, 2, 500).returns(Future.successful(1))

        Post("/transfer", HttpEntity(ContentTypes.`application/json`, """{ "from": 1, "to": 2, "amount": 500 }""")) ~> router.route ~> check {
          responseAs[String] shouldEqual "transfer competed"
        }
      }

      "return error when an error occurred" in {
        (repository.transfer _).when(1, 2, 500).returns(Future.failed(new RuntimeException("Insufficient funds")))

        // curl -H "Content-Type: application/json" -X POST -d '{ "from": 1, "to": 2, "amount": 500 }' http://localhost:8080/transfer
        Post("/transfer", HttpEntity(ContentTypes.`application/json`, """{ "from": 1, "to": 2, "amount": 500 }""")) ~> router.route ~> check {
          responseAs[String] shouldEqual "Insufficient funds"
        }
      }

    }
  }
}
