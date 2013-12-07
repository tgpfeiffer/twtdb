package net.nablux.twtdb
package snippet

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.util.Helpers._
import net.nablux.twtdb.lib._
import scala.xml.NodeSeq
import bootstrap.liftweb.Site

class LoginForm extends Loggable {

  def oauthButton = {
    UserAccessToken.get match {
      case Full(token) => {
        // we do have an access token, so we render the logout button

        "#loginBtn" #> NodeSeq.Empty &
          "#logoutBtn" #> SHtml.onSubmitUnit(() => {
            UserRequestToken.set(Empty)
            UserAccessToken.set(Empty)
            S.seeOther(Site.oauthLogin.loc.calcDefaultHref)
          })
      }
      case _ => {
        // we don't have an access token yet, so we render the login button
        "#loginBtn" #> SHtml.onSubmitUnit(() =>
          OauthHelper.getRequestTokenAndRedirect) &
          "#logoutBtn" #> NodeSeq.Empty
      }
    }
  }
}

