package net.nablux.twtdb.snippet

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.util.Helpers._
import net.nablux.twtdb.lib._
import scala.xml.{NodeSeq, Text}
import net.liftweb.http.js.JE.JsFunc
import net.liftweb.http.js.jquery.JqJE.JqId
import net.nablux.twtdb.model.NewTweet
import bootstrap.liftweb.Site

class TweetForm {
  def render = {
    var body = ""

    UserAccessToken.get match {
      case Full(token) => {
        // we do have an access token, so we render the tweet form
        "#tweet-body" #> SHtml.textarea("", body = _) &
          ":submit" #> SHtml.ajaxSubmit("Tweet", () => {
            if (body.trim.isEmpty) {
              S.warning(S.?("twtdb.no-tweet-body"))
            } else {
              val res = OauthHelper.postTweet(NewTweet(body.trim))
              res match {
                case Right(s) => {
                  S.notice(S.?("twtdb.tweet-successful"))
                  (JqId("tweet-body") ~> JsFunc("val", "")).cmd
                }
                case Left(err) => {
                  S.error(S.?("twtdb.tweet-err", err))
                }
              }
            }
          })

      }
      case _ => {
        // we don't have an access token yet, so we hide the form
        S.warning(S.?("twtdb.login-required"))
        S.redirectTo(Site.oauthLogin.loc.calcDefaultHref)
        "*" #> NodeSeq.Empty
      }
    }
  }
}
