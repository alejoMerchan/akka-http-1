package akka.http.services

import akka.actor.{Props, ActorLogging, Actor, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.services.Service3.{ResponseMessage, RequestMessage}
import akka.http.services.security
import akka.http.services.security.Authentication
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
 * Created by abelmeos on 2017/01/02.
 */

object Service3 extends App with Directives with Authentication{

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  implicit val timeout:Timeout = Timeout( 60 seconds )

  case class RequestMessage(message:String)
  case class ResponseMessage(message:String)

  implicit val requestMessageFormat = jsonFormat1(RequestMessage)
  implicit val responseMessageFormat = jsonFormat1(ResponseMessage)

  val pingPongActor = system.actorOf(Props[PingPongActor],"ping-pong-actor")

  val route:Route = path("ping-pong-service") {
    authorize(hasPermission("123")){
      post{
        entity(as[RequestMessage]){
          requestMessage => onSuccess(pingPongActor ? new RequestMessage("ping") ){
            case response:ResponseMessage =>
              complete(StatusCodes.OK, response)

            case _ => complete(StatusCodes.InternalServerError)
          }

        }
      }

    }

  }

  val bindingFuture = Http().bindAndHandle(route,"localhost",8080)

  println(s"Server online...")
  StdIn.readLine()
  bindingFuture.flatMap( _.unbind()).onComplete(_ => system.terminate())

}

class PingPongActor extends Actor with ActorLogging {

  def receive:Receive = {
      case pingMessage:RequestMessage =>
        log.info(pingMessage.message)
        sender() ! new ResponseMessage("pong")
  }

}
