package net.nablux.twtdb

import org.scalatest._
import net.liftweb.actor.LiftActor
import net.nablux.twtdb.lib.StreamProcessor
import net.nablux.twtdb.model._

class JsonParsing
  extends FlatSpec
  with Matchers {

  class DummyActor extends LiftActor {
    protected def messageHandler = {
      case _ => Unit
    }
  }

  val processor = new StreamProcessor(new DummyActor)

  "The Stream Processor" should "handle friends list correctly" in {
    val s = """{"friends":[19142351,1217327924,404759173,1217351232,324964907,1217347848,14230524,1217354750,1217232470,37019708,1217332968,108146158]}"""
    val parsed = processor.parseRow(s)
    parsed should not be empty
    parsed.get shouldBe a[FriendList]
  }
}
