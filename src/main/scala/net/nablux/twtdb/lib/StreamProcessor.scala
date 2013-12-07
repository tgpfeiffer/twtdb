package net.nablux.twtdb.lib

import java.util.Locale
import java.text.SimpleDateFormat
import net.liftweb.actor.LiftActor
import net.liftweb.common._
import net.liftweb.util.Helpers._
import net.liftweb.json._
import dispatch._
import dispatch.Defaults._
import dispatch.oauth._
import com.ning.http.client.oauth.RequestToken
import net.nablux.twtdb.model._

/**
 * Received from TimelineActor to signal we should connect to Twitter stream.
 */
case class StartListening(token: RequestToken)

/**
 * Received from TimelineActor to signal we should drop the stream connection.
 */
case class StopListening()

/**
 * Connects to Twitter API, parses events and sends them to TimelineActor.
 * @param parent the CometActor that listens to events from this actor
 */
class StreamProcessor(parent: LiftActor)
  extends LiftActor
  with Loggable {

  object FormatsWithTwitterDate extends DefaultFormats {
    override protected val dateFormatter = new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy", Locale.US)
  }

  implicit val formats = FormatsWithTwitterDate

  // we keep a copy of the token here
  protected var token: Box[RequestToken] = Empty

  // This function allows to stop a running request.
  // It is set when starting the request.
  protected var stopRequest: () => Unit = () => Unit

  protected def messageHandler = {
    case StartListening(at) => {
      token = Full(at)
      // stop a running request, just in case
      stopRequest()
      logger.info("connecting to Twitter REST API")

      // first, get a list of tweets from the normal REST API
      val restReq = url("https://api.twitter.com/1.1/statuses/home_timeline.json")
      OauthHelper.http(restReq <@(OauthHelper.consumer, at) OK as.String).
        either.map(_ match {
        // if we got a string, parse it and try to get tweets out
        case Right(s) => {
          tryo {
            parse(s).extract[List[Tweet]]
          } match {
            case Full(list) => {
              logger.debug("got " + list.length + " initial tweets")
              list.map(parent ! _)
            }
            case Failure(msg, _, _) =>
              logger.error("failed to extract list of tweets: " + msg +
                " from \n" + s)
            case Empty =>
              logger.warn("JSON contained no tweets: " + s)
          }
        }
        // otherwise, log the error
        case Left(t) =>
          logger.error("failed to get initial tweets: " + t.getMessage)
      })

      // now, connect to the streaming API
      logger.info("connecting to Twitter Stream API")
      // the handler deals with rows in the stream
      val handler = as.stream.Lines(line => {
        parseRow(line)
      })
      // remember the function to stop the request
      stopRequest = () => {
        logger.info("stopping running request")
        handler.stop()
      }
      // go
      val streamReq = url("https://userstream.twitter.com/1.1/user.json")
      OauthHelper.http(streamReq <@(OauthHelper.consumer, at) > handler).
        either.map(_ match {
        case Left(t) =>
          logger.warn("API connection closed unexpectedly: " + t.getMessage)
        case Right(_) =>
          logger.debug("API connection closed")
      })
    }

    case StopListening => {
      stopRequest()
      stopRequest = () => Unit
    }
  }

  def parseRow(msg: String): Box[TwitterEvent] = {
    tryo {
      // parse string into JSON representation
      parse(msg)
    } match {
      case Full(json) => {
        // check beforehand if a certain key is present to avoid too
        //  many unnecessary extract[X] calls
        def hasKey(j: JValue, key: String) = (j \ key) != JNothing

        val parsedObj: Box[TwitterEvent] = json match {
          case j if hasKey(j, "friends") =>
            tryo(j.extract[FriendList])
          case j if hasKey(j, "event") =>
            tryo(j.extract[Event])
          case j if hasKey(j, "warning") =>
            tryo(j.extract[TooManyFollowsWarning])
          case j if hasKey(j, "text") && !hasKey(j, "sender") =>
            tryo(j.extract[Tweet])
          case j if hasKey(j, "delete") =>
            tryo(j.extract[DeleteTweet])
          case _ =>
            Empty
        }
        // if we could extract an object, send to Comet Actor
        parsedObj match {
          case Full(obj) =>
            logger.debug("received " + obj)
            parent ! obj
          case Failure(msg, _, _) =>
            logger.error("failed to extract class: " + msg)
          case Empty =>
            logger.warn("no handler for JSON:\n" + tryo(compact(render(json))).getOrElse(msg))
        }
        parsedObj
      }

      case f@Failure(msg, _, _) => {
        logger.error("failed to parse (non-)JSON: " + msg)
        f
      }

      case e@Empty => {
        logger.warn("parsing string gave empty result: " + msg)
        e
      }
    }
  }
}
