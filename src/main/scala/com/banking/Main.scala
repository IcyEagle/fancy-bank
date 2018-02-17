package com.banking

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.StdIn

object Main extends App {
  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val repository = new AccountRepository
  val router = new Router(repository)

  Await.ready(repository.initialize, Duration.Inf)

  val bindingFuture = Http().bindAndHandle(router.route, "localhost", 8080)

  println("Server started")

  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}