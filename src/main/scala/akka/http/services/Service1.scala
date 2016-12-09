package akka.http.services

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import scala.io.StdIn

/**
  * Created by ALEJANDRO on 22/11/2016.
  */
object Service1 {


  def main(args: Array[String]): Unit ={

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()

    implicit val executionContext = system.dispatcher

    val route  =
      path("hello"){
        get{
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`,"<h1>Say hello to akka-http</h1>"))
        }

      }

    val bindingFuture = Http().bindAndHandle(route,"localhost",8080)

    println(s"Server online...")
    StdIn.readLine()
    bindingFuture.flatMap( _.unbind()).onComplete(_ => system.terminate())


  }


}
