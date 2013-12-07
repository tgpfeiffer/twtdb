package net.nablux.twtdb.model

import java.util.Date

// We make TwitterEvent a sealed trait so that we get compiler warnings when
// we forgot to deal with one kind of event in pattern matching situations.
sealed trait TwitterEvent

// User stream messages, cf.
// <https://dev.twitter.com/docs/streaming-apis/messages#User_stream_messages>

case class FriendList(friends: List[Long]) extends TwitterEvent

// TODO: Direct Messages

case class Event(target: String,
                 source: String,
                 event: String,
                 //target_object: String,
                 created_at: Date) extends TwitterEvent

case class TooManyFollowsWarning(warning: TooManyFollows) extends TwitterEvent

case class TooManyFollows(code: String,
                          message: String,
                          user_id: Long)

// Tweets, cf.
// <https://dev.twitter.com/docs/platform-objects/tweets>

case class Tweet(created_at: Date,
                  id: Long,
                  text: String,
                  source: String,
                  truncated: Boolean,
                  user: TFullUser,
                  retweet_count: Int,
                  entities: TEntities) extends TwitterEvent

trait TUser

case class TFullUser(id: Long,
                     name: String,
                     screen_name: String) extends TUser

case class TMentionUser(id: Long,
                        name: String,
                        screen_name: String) extends TUser

// See <https://dev.twitter.com/docs/entities>
case class TEntities(hashtags: List[THashtag],
                     urls: List[TUrl],
                     user_mentions: List[TMentionUser],
                     media: Option[List[TMedia]])

// See <https://dev.twitter.com/docs/entities#The_hashtags_entity>
case class THashtag(text: String)

// See <https://dev.twitter.com/docs/entities#The_urls_entity>
case class TUrl(url: String,
                display_url: String,
                expanded_url: String)

// See <https://dev.twitter.com/docs/entities#The_media_entity>
case class TMedia(id: Long,
                  media_url: String)

case class DeleteTweet(delete: Delete) extends TwitterEvent

case class Delete(status: DeleteStatus)

case class DeleteStatus(id: Long, user_id: Long)