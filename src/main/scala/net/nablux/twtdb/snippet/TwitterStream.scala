package net.nablux.twtdb
package snippet

import net.liftweb.common._
import net.liftweb.util.Helpers._
import net.nablux.twtdb.lib._

class TwitterStream extends Loggable {

  def homeTimeline = {
    "#timeline" #> (TwitterApi.homeTimeline match {
      case Right(s) =>
        s
      case Left(msg) =>
        msg
    })
  }
}

