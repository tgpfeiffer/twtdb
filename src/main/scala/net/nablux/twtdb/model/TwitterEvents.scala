package net.nablux.twtdb.model

// We make TwitterEvent a sealed trait so that we get compiler warnings when
// we forgot to deal with one kind of event in pattern matching situations.
sealed trait TwitterEvent

// User stream messages, cf.
// <https://dev.twitter.com/docs/streaming-apis/messages#User_stream_messages>

case class FriendList(friends: List[Long]) extends TwitterEvent
