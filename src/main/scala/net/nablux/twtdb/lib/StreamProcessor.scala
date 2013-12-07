package net.nablux.twtdb.lib

import net.liftweb.actor.LiftActor
import net.liftweb.common.{Loggable, Full, Empty, Box}
import dispatch._
import dispatch.Defaults._
import dispatch.oauth._
import com.ning.http.client.oauth.RequestToken
import net.nablux.twtdb.comet.{StreamRow, TimelineActor}

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
class StreamProcessor(parent: TimelineActor)
  extends LiftActor
  with Loggable {

  // we keep a copy of the token here
  protected var token: Box[RequestToken] = Empty

  // This function allows to stop a running request.
  // It is set when starting the request.
  protected var stopRequest: () => Unit = () => Unit

  protected def messageHandler = {
    case StartListening(at) => {
      // stop a running request, just in case
      stopRequest()
      logger.info("connecting to Twitter Stream API: " + at)
      token = Full(at)
      // the handler deals with rows in the stream
      val handler = as.stream.Lines(line => {
        println("got: " + line)
        parent ! StreamRow(line)
      })
      // remember the function to stop the request
      stopRequest = () => {
        logger.info("stopping running request")
        handler.stop()
      }
      // go
      val req = url("https://userstream.twitter.com/1.1/user.json")
      OauthHelper.http(req <@(OauthHelper.consumer, at) > handler)
    }

    case StopListening => {
      stopRequest()
      stopRequest = () => Unit
    }
  }
}
