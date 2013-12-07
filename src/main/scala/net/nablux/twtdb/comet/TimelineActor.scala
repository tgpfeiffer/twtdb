package net.nablux.twtdb.comet

import scala.xml.NodeSeq
import net.liftweb.http._
import net.liftweb.common.{Empty, Box, Loggable, Full}
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.http.js.jquery.JqJsCmds.AppendHtml
import com.ning.http.client.oauth.RequestToken
import net.nablux.twtdb.lib.{StopListening, StartListening, StreamProcessor, UserAccessToken}
import net.nablux.twtdb.model._

/**
 * Received from OauthHelper to show user has authenticated with Twitter.
 */
case class Login(accessToken: RequestToken)

/**
 * Received from OauthHelper to show user has destroyed tokens.
 */
case class Logout()

/**
 * Renders the entities extracted from the Twitter stream to the webpage.
 */
class TimelineActor
  extends CometActor
  with Loggable {

  // store some of the data received for display
  var friendList: Box[FriendList] = Empty

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
    } &
      "#friendlist *" #> renderEvent(friendList)
  }

  // handle some technical events with high priority
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

  // handle rendering events with low priority
  override def lowPriority: PartialFunction[Any, Unit] = {
    case te: TwitterEvent => {
      te match {
        case fl: FriendList => {
          friendList = Full(fl)
          partialUpdate(SetHtml("friendlist", renderEvent(fl)))
        }
        case t: Tweet => {
          partialUpdate(AppendHtml("message", renderEvent(t)))
        }
        case _: Event | _: TooManyFollowsWarning | _: DeleteTweet => {}
      }
    }
  }

  // Generates HTML for event; separate function since we may need the HTML
  // in both render() and lowPriority().
  protected def renderEvent(te: TwitterEvent): NodeSeq = {
    te match {
      case fl: FriendList => {
        val li = fl.friends.map(id => <li>
          {id.toString}
        </li>)
        <ul>
          {li.foldLeft(NodeSeq.Empty)(_ ++ _)}
        </ul>
      }
      case t: Tweet => {
        <div>
          <p>
            {t.text}
          </p>
          <small>from {t.user.name} (@{t.user.screen_name})</small>
        </div> ++ <hr />
      }
      case _: Event | _: TooManyFollowsWarning | _: DeleteTweet =>
          <div/>
    }
  }

  // Wrapper for renderEvent() on Boxes.
  protected def renderEvent(bte: Box[TwitterEvent]): NodeSeq = {
    bte.map(renderEvent(_)).getOrElse(NodeSeq.Empty)
  }

}