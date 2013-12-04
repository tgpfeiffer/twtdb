package net.nablux.twtdb.lib

import dispatch._
import dispatch.Defaults._
import dispatch.oauth._
import com.ning.http.client.oauth.{ConsumerKey, RequestToken}
import net.liftweb.common._
import net.liftweb.util.Props
import net.liftweb.http._
import net.liftweb.http.rest._
import bootstrap.liftweb.Site

object UserRequestToken extends SessionVar[Box[RequestToken]](Empty)

object UserAccessToken extends SessionVar[Box[RequestToken]](Empty)

object OauthHelper
  extends SomeHttp
  with SomeConsumer
  with SomeCallback
  with SomeEndpoints
  with Exchange
  with RestHelper
  with Loggable {

  // implements SomeHttp
  override def http = new Http

  // implements SomeConsumer
  override def consumer = new ConsumerKey(
    Props.get("twitter.apikey") openOr "",
    Props.get("twitter.apisecret") openOr "")

  // implements SomeCallback
  override def callback: String = "http://127.0.0.1:8083/oauth/callback"

  // implements SomeEndpoints
  val twitterBase = "https://api.twitter.com"

  override def requestToken: String = twitterBase + "/oauth/request_token"

  override def accessToken: String = twitterBase + "/oauth/access_token"

  override def authorize: String = twitterBase + "/oauth/authorize"

  /** Gets a request token from Twitter and redirects to their login page.
    *
    * NB. Must be used from stateful environment since we use the S object.
    */
  def getRequestTokenAndRedirect = {
    val tokReq = fetchRequestToken
    tokReq.apply match {
      case Right(rt) =>
        // we got a request token; store it in SessionVar and redirect
        //  to Twitter for user authentication
        logger.info("got request token: " + rt)
        UserRequestToken.set(Full(rt))
        val target = authorize + "?oauth_token=" + rt.getKey
        S.redirectTo(target)

      case Left(msg) =>
        // we did not get a token; do nothing
        logger.error("did not get a request token: " + msg)
        S.error(S.?("twtdb.no-req-token-received"))
    }
  }

  // Now come the callback handlers in the OAuth workflow.
  serve {
    // if we return from Twitter, we must have a request token ...
    case net.liftweb.http.Req("oauth" :: "callback" :: Nil, _, GetRequest)
      if UserRequestToken.get.isDefined => {
      // ... and a oauth_verifier parameter in the URL
      S.param("oauth_verifier") match {
        case Full(verifier) =>
          // if we got a verifier as a parameter, get an access token next
          logger.info("return from Twitter with verifier :" + verifier)
          val rt = UserRequestToken.get.get

          // get an access token using request token and verifier
          fetchAccessToken(rt, verifier).apply match {
            case Right(at) =>
              // store in session var and go to front page
              logger.info("got access token: " + at)
              UserAccessToken.set(Full(at))
              S.notice(S.?("twtdb.login-success"))
              RedirectResponse(Site.home.loc.calcDefaultHref)

            case Left(msg) =>
              // remove request token as well (something went wrong)
              logger.error("did not receive access token: " + msg)
              UserRequestToken.remove
              S.error(S.?("twtdb.no-access-token-received"))
              RedirectResponse(Site.home.loc.calcDefaultHref)
          }

        case _ =>
          // if there is no oauth_verifier parameter, something went wrong
          logger.warn("no verifier passed when returning from Twitter")
          S.error(S.?("twtdb.no-oauth-verifier-from-twitter"))
          RedirectResponse(Site.home.loc.calcDefaultHref)
      }
    }

    // if we do not have a request token, display error and go to front page
    case net.liftweb.http.Req("oauth" :: "callback" :: Nil, _, GetRequest)
      if UserRequestToken.get.isEmpty => {
      logger.warn("no request token known when returning from Twitter")
      RedirectResponse(Site.home.loc.calcDefaultHref)
    }
  }
}
