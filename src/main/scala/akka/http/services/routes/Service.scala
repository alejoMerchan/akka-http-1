package akka.http.services.routes

import akka.actor.{Props, ActorLogging, Actor, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.services.routes.Service.{ResponseMessage, RequestMessage}

import akka.http.services.security
import akka.http.services.security.Authentication
import akka.routing.FromConfig
import akka.stream.ActorMaterializer

import akka.http.scaladsl.model.{StatusCodes}
import akka.http.scaladsl.server.{Route, Directives}

import scala.concurrent.duration._
import akka.util.Timeout
import akka.pattern.ask

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._

import scala.io.StdIn

/**
 * Created by abelmeos on 2017/05/09.
 */


object Service extends App with Directives {

  import akka.actor.{ActorLogging, Actor}

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(10 seconds)

  case class RequestMessage(message: String)

  case class ResponseMessage(message: String)

  implicit val requestMessageFormat = jsonFormat1(RequestMessage)
  implicit val responseMessageFormat = jsonFormat1(ResponseMessage)

  val pingPongActor = system.actorOf(Props[PingPongActor], "ping-pong-actor")

  // pool router ping pong actor
  val routerPingPongActor = system.actorOf(Props[PingPongActor].withRouter(FromConfig),"poolRouter")

  // pool router reizable actor
  val rougerReizablePingPongActor = system.actorOf(Props[PingPongActor].withRouter(FromConfig),"poolRouterReizable")



  val route: Route = path("ping-pong-service") {

    post {
      entity(as[RequestMessage]) {
        requestMessage =>

          onSuccess(rougerReizablePingPongActor ? requestMessage) {
          case response: ResponseMessage =>
            complete(StatusCodes.OK, response)

          case _ => complete(StatusCodes.InternalServerError)
        }

      }
    }


  }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println(s"Server online...")
  StdIn.readLine()
  bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())

}

class PingPongActor extends Actor with ActorLogging {

  def receive: Receive = {
    case pingMessage: RequestMessage =>
      println("referencia del actor: " + self.actorRef)
      Thread.sleep(5000)
      sender() ! new ResponseMessage("pong")
  }

}
