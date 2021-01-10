package dev.touchdown.gyremock

import java.io.IOException

import akka.actor.ActorSystem
import akka.grpc.ServiceDescription
import akka.grpc.scaladsl.{ServerReflection, ServiceHandler}
import akka.http.scaladsl.{Http, HttpConnectionContext}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import com.typesafe.scalalogging.StrictLogging

import scala.collection.immutable
import scala.concurrent.Future

class AkkaGrpcServer(services: immutable.Seq[(ServiceDescription, PartialFunction[HttpRequest, Future[HttpResponse]])])(implicit sys: ActorSystem) extends StrictLogging {

  private val handlers = services.map(_._2) :+ ServerReflection.partial(services.map(_._1).toList)
  private val serviceHandlers: HttpRequest => Future[HttpResponse] = ServiceHandler.concatOrNotFound(handlers:_*)

  @throws[IOException]
  def start(port: Int): Future[Http.ServerBinding] = {
    // Bind service handler servers to localhost:8080/8081
    Http().bindAndHandleAsync(
      serviceHandlers,
      interface = "0.0.0.0",
      port = port,
      connectionContext = HttpConnectionContext())
  }
}