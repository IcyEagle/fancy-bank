package com.banking

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import spray.json.DefaultJsonProtocol._

import scala.util.{Failure, Success}

class Router(val repository: AccountRepository) {

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  case class TransferRequest(from: Long, to: Long, amount: Int)

  implicit val transferFormat = jsonFormat3(TransferRequest)

  val route = get {
    pathPrefix("balance" / LongNumber) { id =>
      val request = repository.get(id)

      onSuccess(request) {
        case Some(account) => complete(account.balance.toString)
        case None => complete(StatusCodes.NotFound)
      }
    }
  } ~
    post {
      path("transfer") {
        entity(as[TransferRequest]) { order =>
          val request = repository.transfer(order.from, order.to, order.amount)

          onComplete(request) {
            case Success(_) => complete("transfer competed")
            case Failure(error) => complete((StatusCodes.InternalServerError, error.getMessage))
          }
        }
      }
    }
}
