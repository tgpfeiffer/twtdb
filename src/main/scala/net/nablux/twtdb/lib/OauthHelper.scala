package net.nablux.twtdb.lib

import dispatch._
import dispatch.Defaults._
import dispatch.oauth._
import com.ning.http.client.oauth.ConsumerKey
import net.liftweb.util.Props

object OauthHelper
  extends SomeHttp
  with SomeConsumer
  with SomeCallback
  with SomeEndpoints
  with Exchange {

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

  def main(args: Array[String]) = {
    // tests the functionality of this object
    (for {
      rt <- fetchRequestToken.right
    } yield {
      println("now go to " + authorize + "?oauth_token=" + rt.getKey)
      val verifier = readLine("verifier> ")
      (for {
        at <- fetchAccessToken(rt, verifier).right
      } yield println(at)).apply()
    }).apply
  }
}
