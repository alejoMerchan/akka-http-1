package akka.http.services.security

import akka.http.scaladsl.server.directives.Credentials

/**
 * Created by abelmeos on 2017/02/01.
 */
trait Authentication {

  val tokenValid = "654321"

  def hasPermission(token:String): Boolean =  token == tokenValid


}
