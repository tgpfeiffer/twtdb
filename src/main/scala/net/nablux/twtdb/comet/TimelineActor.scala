package net.nablux.twtdb.comet

import net.liftweb._
import http._
import net.liftweb.util.Helpers._
import scala.xml.Text
import net.nablux.twtdb.lib.{StopListening, StartListening, StreamProcessor, UserAccessToken}
import net.liftweb.common.{Loggable, Full}
import com.ning.http.client.oauth.RequestToken
import net.liftweb.http.js.jquery.JqJsCmds.AppendHtml

/**
 * Received from OauthHelper to show user has authenticated with Twitter.
 */
case class Login(accessToken: RequestToken)

/**
 * Received from OauthHelper to show user has destroyed tokens.
 */
case class Logout()

/**
 * Contains all data from one row in the Twitter stream.
 */
case class StreamRow(json: String)

/**
 * Renders the entities extracted from the Twitter stream to the webpage.
 */
class TimelineActor
  extends CometActor
  with Loggable {

  // actor that does the listening
  protected val processor = new StreamProcessor(this)

  // message that is rendered when first displayed
  def render = {
    "#message *" #> {
      UserAccessToken.get match {
        case Full(at) =>
          S.?("twtdb.retrieving-tweets")
        case _ =>
          S.?("twtdb.please-login")
      }
    }
  }

  override def highPriority: PartialFunction[Any, Unit] = {
    // when user logs in, tell the StreamProcessor to start the HTTP request
    case Login(token) => {
      logger.info("user logged in")
      processor ! StartListening(token)
    }
    // when the user logs out, tell the StreamProcessor to cancel the request
    case Logout => {
      logger.info("user logged out")
      processor ! StopListening
    }
  }

  override def lowPriority: PartialFunction[Any, Unit] = {
    // when we receive a row from the stream, just display it
    case StreamRow(json: String) => {
      partialUpdate(AppendHtml("message",
          <br /> ++ Text(now.toString + ": " + json)))
    }
  }

}