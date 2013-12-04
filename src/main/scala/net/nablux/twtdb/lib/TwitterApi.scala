package net.nablux.twtdb.lib

import dispatch._
import dispatch.Defaults._
import dispatch.oauth._
import net.liftweb.common._

object TwitterApi {
  val twitterBase = url(OauthHelper.twitterBase)

  def homeTimeline: Either[String, String] = {
    val req = twitterBase / "1.1" / "statuses" / "home_timeline.json"
    UserAccessToken.get match {
      case Full(at) =>
        val fut = OauthHelper.http(req <@(OauthHelper.consumer, at)
          > as.String).either
        fut.left.map(_.getMessage).apply
      case _ =>
        Left("no access token!")
    }
  }

}
