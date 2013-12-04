package net.nablux.twtdb
package snippet

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.util.Helpers._
import net.nablux.twtdb.lib._

class LoginForm extends Loggable {

  def oauthButton = {
    UserAccessToken.get match {
      case Full(token) => {
        // there is already an access token, no need to login
        ":submit" #> ("already logged in")
      }
      case _ => {
        // we don't have an access token yet, so we render the button
        ":submit" #> SHtml.onSubmitUnit(() =>
          OauthHelper.getRequestTokenAndRedirect)
      }
    }
  }
}

