package akka.http.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{StatusCodes, HttpResponse, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout

import scala.concurrent.Future

import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._

import scala.util.{Failure, Success}
import scala.concurrent.duration._

/**
 * Created by abelmeos on 2017/05/09.
 */
object GeneralClient extends App{

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(10 seconds)

  case class Ping(message:String)

  implicit val  pingMarshaller = jsonFormat1(Ping)

  val httpClient = Http().outgoingConnection("localhost",8080)

  def sendAndReceiveAs(httpRequest: HttpRequest):Future[String] =
    sendAndReceive(httpRequest,response => Unmarshal(response.entity).to[String])

  def sendAndReceive(request:HttpRequest, f:HttpResponse => Future[String]):Future[String] =
    Source.single(request).via(httpClient).mapAsync(1) {
      response =>
        if(response.status.isSuccess() || response.status == StatusCodes.NotFound) f(response)
        else Future.failed(new Exception(s"Request filed. Response was: $response"))
    }.runWith(Sink.head)

  def callClient() = {

    val resultFuture = sendAndReceiveAs(Post("/ping-pong-service",Ping("ping")))

    resultFuture.onComplete{
      case Success(result) => println(result)
      case Failure(f) => println(f.getMessage)
    }
  }

  def pruebaConcurrencia() = {

    for(a <- 1 to 9){

      println("lllamado: " +  a )
      callClient()

    }
  }


  pruebaConcurrencia()

}
