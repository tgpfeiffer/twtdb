package net.nablux.twtdb.comet

import scala.xml.{NodeSeq, Text}
import net.liftweb.http._
import net.liftweb.common.{Empty, Box, Loggable, Full}
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.http.js.jquery.JqJsCmds.PrependHtml
import net.liftweb.util.Helpers._
import com.ning.http.client.oauth.RequestToken
import net.nablux.twtdb.lib.{StopListening, StartListening, StreamProcessor, UserAccessToken}
import net.nablux.twtdb.model._
import scala.collection.mutable.MutableList

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
  val events: MutableList[TwitterEvent] = MutableList()

  // actor that does the listening
  protected val processor = new StreamProcessor(this)

  UserAccessToken.get.map(this ! Login(_))

  // message that is rendered when first displayed
  def render = {
    "#timeline *" #> {
      UserAccessToken.get match {
        case Full(at) =>
          events.map(renderEvent(_)).foldLeft(NodeSeq.Empty)(_ ++ _)
        case _ =>
          Text(S.?("twtdb.please-login"))
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
          t +=: events
          partialUpdate(PrependHtml("timeline", renderEvent(t)))
        }
        case dm: DirectMessage => {
          dm +=: events
          partialUpdate(PrependHtml("timeline", renderEvent(dm)))
        }
        case e: Event => {
          e +=: events
          partialUpdate(PrependHtml("timeline", renderEvent(e)))
        }
        case _: TooManyFollowsWarning | _: DeleteTweet => {}
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
        val tmpl = Templates("templates-hidden" :: "_tweet" :: Nil)
        val transform = {
          ".tweet [id]" #> ("item-" + t.id) &
            ".tweet-text *" #> t.text &
            ".tweet-name *" #> t.user.name &
            ".tweet-user-profile_image_url [src]" #> t.user.profile_image_url.
              replace("_normal.", "_mini.") &
            ".tweet-screen_name *" #> ("@" + t.user.screen_name) &
            ".tweet-screen_name [href]" #> ("https://twitter.com/" + t.user.screen_name) &
            ".tweet-created_at *" #> t.created_at.toString
        }
        tmpl.map(transform).getOrElse(<p>error</p>)
      }

      case dm: DirectMessage => {
        val tmpl = Templates("templates-hidden" :: "_dm" :: Nil)
        val d = dm.direct_message
        val transform = {
          ".dm [id]" #> ("item-" + d.id) &
            ".dm-text *" #> d.text &
            ".dm-sender" #> {
              ".dm-name *" #> d.sender.name &
                ".dm-user-profile_image_url [src]" #> d.sender.profile_image_url.
                  replace("_normal.", "_mini.") &
                ".dm-screen_name *" #> ("@" + d.sender.screen_name) &
                ".dm-screen_name [href]" #> ("https://twitter.com/" + d.sender.screen_name)
            } &
            ".dm-recipient" #> {
              ".dm-name *" #> d.recipient.name &
                ".dm-user-profile_image_url [src]" #> d.recipient.profile_image_url.
                  replace("_normal.", "_mini.") &
                ".dm-screen_name *" #> ("@" + d.recipient.screen_name) &
                ".dm-screen_name [href]" #> ("https://twitter.com/" + d.recipient.screen_name)
            } &
            ".dm-created_at *" #> d.created_at.toString
        }
        tmpl.map(transform).getOrElse(<p>error</p>)
      }

      case e: Event => {
        val tmpl = Templates("templates-hidden" :: "_event" :: Nil)
        val transform = {
          ".event-text *" #> e.event &
            ".event-source" #> {
              ".event-name *" #> e.source.name &
                ".event-user-profile_image_url [src]" #> e.source.profile_image_url.
                  replace("_normal.", "_mini.") &
                ".event-screen_name *" #> ("@" + e.source.screen_name) &
                ".event-screen_name [href]" #> ("https://twitter.com/" + e.source.screen_name)
            } &
            ".event-target" #> {
              ".event-name *" #> e.target.name &
                ".event-user-profile_image_url [src]" #> e.target.profile_image_url.
                  replace("_normal.", "_mini.") &
                ".event-screen_name *" #> ("@" + e.target.screen_name) &
                ".event-screen_name [href]" #> ("https://twitter.com/" + e.target.screen_name)
            } &
            ".event-created_at *" #> e.created_at.toString
        }
        tmpl.map(transform).getOrElse(<p>error</p>)
      }

      case _: TooManyFollowsWarning | _: DeleteTweet =>
          <div/>
    }
  }

  // Wrapper for renderEvent() on Boxes.
  protected def renderEvent(bte: Box[TwitterEvent]): NodeSeq = {
    bte.map(renderEvent(_)).getOrElse(NodeSeq.Empty)
  }

}