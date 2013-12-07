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

  ignore should "handle direct messages correctly" in {}

  it should "handle events correctly" in {
    val s = """{
              |  "target": "TARGET_USER",
              |  "source": "SOURCE_USER",
              |  "event":"EVENT_NAME",
              |  "target_object": "TARGET_OBJECT",
              |  "created_at": "Sat Sep 4 16:10:54 +0000 2010"
              |}
              | """.stripMargin
    val parsed = processor.parseRow(s)
    parsed should not be empty
    parsed.get shouldBe a[Event]
  }

  it should "handle 'too many follows' correctly" in {
    val s = """{
              |  "warning": {
              |    "code": "FOLLOWS_OVER_LIMIT",
              |    "message": "The requested user follows more accounts than the maximum supported by this streaming endpoint. Only a subset of 10000 followed accounts are included in this stream.",
              |    "user_id": 12345678
              |  }
              |}""".stripMargin
    val parsed = processor.parseRow(s)
    parsed should not be empty
    parsed.get shouldBe a[TooManyFollowsWarning]
  }

  it should "handle tweets without media correctly" in {
    val s = """{
              |  "created_at":"Sat Dec 07 12:28:05 +0000 2013",
              |  "id":409298265994043392,
              |  "id_str":"409298265994043392",
              |  "text":"i wanna meet @alliharvard agaaaaiiiin 😭",
              |  "source":"<a href=\"http://twitter.com/download/iphone\" rel=\"nofollow\">Twitter for iPhone</a>",
              |  "truncated":false,
              |  "in_reply_to_status_id":null,
              |  "in_reply_to_status_id_str":null,
              |  "in_reply_to_user_id":null,
              |  "in_reply_to_user_id_str":null,
              |  "in_reply_to_screen_name":null,
              |  "user":{
              |    "id":477695616,
              |    "id_str":"477695616",
              |    "name":"Raya Marcelo",
              |    "screen_name":"nagarayaaaaa",
              |    "location":"mnl / mafiugh",
              |    "url":null,
              |    "description":"and in that moment, i swear i still didn't care",
              |    "protected":false,
              |    "followers_count":525,
              |    "friends_count":443,
              |    "listed_count":0,
              |    "created_at":"Sun Jan 29 13:25:18 +0000 2012",
              |    "favourites_count":3091,
              |    "utc_offset":28800,
              |    "time_zone":"Beijing",
              |    "geo_enabled":true,
              |    "verified":false,
              |    "statuses_count":24852,
              |    "lang":"en",
              |    "contributors_enabled":false,
              |    "is_translator":false,
              |    "profile_background_color":"F9F8F6",
              |    "profile_background_image_url":"http://a0.twimg.com/profile_background_images/378800000029567109/a6c8329ed404a007bcb0194ad804958f.png",
              |    "profile_background_image_url_https":"https://si0.twimg.com/profile_background_images/378800000029567109/a6c8329ed404a007bcb0194ad804958f.png",
              |    "profile_background_tile":true,
              |    "profile_image_url":"http://pbs.twimg.com/profile_images/378800000761524365/a26a8c25f4df823f12e6d8d0ccccb941_normal.jpeg",
              |    "profile_image_url_https":"https://pbs.twimg.com/profile_images/378800000761524365/a26a8c25f4df823f12e6d8d0ccccb941_normal.jpeg",
              |    "profile_banner_url":"https://pbs.twimg.com/profile_banners/477695616/1384870472",
              |    "profile_link_color":"F5EAC9",
              |    "profile_sidebar_border_color":"FFFFFF",
              |    "profile_sidebar_fill_color":"FFFFFF",
              |    "profile_text_color":"000000",
              |    "profile_use_background_image":true,
              |    "default_profile":false,
              |    "default_profile_image":false,
              |    "following":null,
              |    "follow_request_sent":null,
              |    "notifications":null
              |  },
              |  "geo":null,
              |  "coordinates":null,
              |  "place":null,
              |  "contributors":null,
              |  "retweet_count":0,
              |  "favorite_count":0,
              |  "entities":{
              |    "hashtags":[],
              |    "symbols":[],
              |    "urls":[],
              |    "user_mentions":[{
              |      "screen_name":"alliharvard",
              |      "name":"allison harvard",
              |      "id":41671777,
              |      "id_str":"41671777",
              |      "indices":[13,25]
              |    }]
              |  },
              |  "favorited":false,
              |  "retweeted":false,
              |  "filter_level":"medium",
              |  "lang":"en"
              |}""".stripMargin
    val parsed = processor.parseRow(s)
    parsed should not be empty
    parsed.get shouldBe a[Tweet]
  }

  it should "handle tweets with media correctly" in {
    val s = """{
              |  "created_at":"Sat Dec 07 12:28:00 +0000 2013",
              |  "id":409298245001564160,
              |  "id_str":"409298245001564160",
              |  "text":"RT @pokopan0717: 【超拡散希望】\nthe GazettEポスターお譲りします。\nＰ缶のため取引後削除。\n画鋲跡ありは100円+送料\n画鋲跡なしは200円+送料\n裏表ありは300円+送料 http://t.co/RlYJqodURA",
              |  "source":"<a href=\"http://twtr.jp\" rel=\"nofollow\">Keitai Web</a>",
              |  "truncated":false,
              |  "in_reply_to_status_id":null,
              |  "in_reply_to_status_id_str":null,
              |  "in_reply_to_user_id":null,
              |  "in_reply_to_user_id_str":null,
              |  "in_reply_to_screen_name":null,
              |  "user":{
              |    "id":578227035,
              |    "id_str":"578227035",
              |    "name":"にょこ",
              |    "screen_name":"NpontenA",
              |    "location":"",
              |    "url":"http://ameblo.jp/skyship3/",
              |    "description":"V系とライブとお芝居と歌舞伎とタカラヅカと食べ歩き。",
              |    "protected":false,
              |    "followers_count":48,
              |    "friends_count":238,
              |    "listed_count":0,
              |    "created_at":"Sat May 12 15:21:24 +0000 2012",
              |    "favourites_count":3,
              |    "utc_offset":32400,
              |    "time_zone":"Tokyo",
              |    "geo_enabled":false,
              |    "verified":false,
              |    "statuses_count":12107,
              |    "lang":"ja",
              |    "contributors_enabled":false,
              |    "is_translator":false,
              |    "profile_background_color":"C0DEED",
              |    "profile_background_image_url":"http://abs.twimg.com/images/themes/theme1/bg.png",
              |    "profile_background_image_url_https":"https://abs.twimg.com/images/themes/theme1/bg.png",
              |    "profile_background_tile":false,
              |    "profile_image_url":"http://pbs.twimg.com/profile_images/378800000512854069/2f8200645fa2708a9e7b9ed351e39228_normal.jpeg",
              |    "profile_image_url_https":"https://pbs.twimg.com/profile_images/378800000512854069/2f8200645fa2708a9e7b9ed351e39228_normal.jpeg",
              |    "profile_link_color":"0084B4",
              |    "profile_sidebar_border_color":"C0DEED",
              |    "profile_sidebar_fill_color":"DDEEF6",
              |    "profile_text_color":"333333",
              |    "profile_use_background_image":true,
              |    "default_profile":true,
              |    "default_profile_image":false,
              |    "following":null,
              |    "follow_request_sent":null,
              |    "notifications":null
              |  },
              |  "geo":null,
              |  "coordinates":null,
              |  "place":null,
              |  "contributors":null,
              |  "retweeted_status":{
              |    "created_at":"Sat Dec 07 12:14:10 +0000 2013",
              |    "id":409294762391248896,
              |    "id_str":"409294762391248896",
              |    "text":"【超拡散希望】\nthe GazettEポスターお譲りします。\nＰ缶のため取引後削除。\n画鋲跡ありは100円+送料\n画鋲跡なしは200円+送料\n裏表ありは300円+送料 http://t.co/RlYJqodURA",
              |    "source":"<a href=\"http://twitter.com/download/android\" rel=\"nofollow\">Twitter for Android</a>",
              |    "truncated":false,
              |    "in_reply_to_status_id":null,
              |    "in_reply_to_status_id_str":null,
              |    "in_reply_to_user_id":null,
              |    "in_reply_to_user_id_str":null,
              |    "in_reply_to_screen_name":null,
              |    "user":{
              |      "id":1297234158,
              |      "id_str":"1297234158",
              |      "name":"幸羅＠全力Soan♡",
              |      "screen_name":"pokopan0717",
              |      "location":"Moranちゃんの居るところ……(つω`*)♥",
              |      "url":null,
              |      "description":"幸羅(ゆきら)です｡Moranに一途｡時々BORN｡脱雑食に成功(?)ﾏﾏ盲目Holic♥全力Soan動員｡れいともｺﾝﾋﾞが好き｡ｱﾆｵﾀで声優ｸﾗｽﾀでかなりの腐女子｡むっくんの嫁兼､ﾚｲｼﾞの奴隷｡※ﾂｲｰﾄ数多め｡",
              |      "protected":false,
              |      "followers_count":498,
              |      "friends_count":509,
              |      "listed_count":5,
              |      "created_at":"Mon Mar 25 01:29:27 +0000 2013",
              |      "favourites_count":877,
              |      "utc_offset":null,
              |      "time_zone":null,
              |      "geo_enabled":true,
              |      "verified":false,
              |      "statuses_count":17694,
              |      "lang":"ja",
              |      "contributors_enabled":false,
              |      "is_translator":false,
              |      "profile_background_color":"C0DEED",
              |      "profile_background_image_url":"http://abs.twimg.com/images/themes/theme1/bg.png",
              |      "profile_background_image_url_https":"https://abs.twimg.com/images/themes/theme1/bg.png",
              |      "profile_background_tile":false,
              |      "profile_image_url":"http://pbs.twimg.com/profile_images/378800000826935519/9ff714cfed94f57621058627aaadc170_normal.jpeg",
              |      "profile_image_url_https":"https://pbs.twimg.com/profile_images/378800000826935519/9ff714cfed94f57621058627aaadc170_normal.jpeg",
              |      "profile_banner_url":"https://pbs.twimg.com/profile_banners/1297234158/1385920496",
              |      "profile_link_color":"0084B4",
              |      "profile_sidebar_border_color":"C0DEED",
              |      "profile_sidebar_fill_color":"DDEEF6",
              |      "profile_text_color":"333333",
              |      "profile_use_background_image":true,
              |      "default_profile":true,
              |      "default_profile_image":false,
              |      "following":null,
              |      "follow_request_sent":null,
              |      "notifications":null
              |    },
              |    "geo":null,
              |    "coordinates":null,
              |    "place":null,
              |    "contributors":null,
              |    "retweet_count":5,
              |    "favorite_count":0,
              |    "entities":{
              |      "hashtags":[],
              |      "symbols":[],
              |      "urls":[],
              |      "user_mentions":[],
              |      "media":[{
              |        "id":409294762076680192,
              |        "id_str":"409294762076680192",
              |        "indices":[84,106],
              |        "media_url":"http://pbs.twimg.com/media/Ba4bauYCMAA6vjO.jpg",
              |        "media_url_https":"https://pbs.twimg.com/media/Ba4bauYCMAA6vjO.jpg",
              |        "url":"http://t.co/RlYJqodURA",
              |        "display_url":"pic.twitter.com/RlYJqodURA",
              |        "expanded_url":"http://twitter.com/pokopan0717/status/409294762391248896/photo/1",
              |        "type":"photo",
              |        "sizes":{
              |          "medium":{
              |            "w":600,
              |            "h":450,
              |            "resize":"fit"
              |          },
              |          "large":{
              |            "w":1024,
              |            "h":768,
              |            "resize":"fit"
              |          },
              |          "thumb":{
              |            "w":150,
              |            "h":150,
              |            "resize":"crop"
              |          },
              |          "small":{
              |            "w":340,
              |            "h":255,
              |            "resize":"fit"
              |          }
              |        }
              |      }]
              |    },
              |    "favorited":false,
              |    "retweeted":false,
              |    "possibly_sensitive":false,
              |    "lang":"ja"
              |  },
              |  "retweet_count":0,
              |  "favorite_count":0,
              |  "entities":{
              |    "hashtags":[],
              |    "symbols":[],
              |    "urls":[],
              |    "user_mentions":[{
              |      "screen_name":"pokopan0717",
              |      "name":"幸羅＠全力Soan♡",
              |      "id":1297234158,
              |      "id_str":"1297234158",
              |      "indices":[3,15]
              |    }],
              |    "media":[{
              |      "id":409294762076680192,
              |      "id_str":"409294762076680192",
              |      "indices":[101,123],
              |      "media_url":"http://pbs.twimg.com/media/Ba4bauYCMAA6vjO.jpg",
              |      "media_url_https":"https://pbs.twimg.com/media/Ba4bauYCMAA6vjO.jpg",
              |      "url":"http://t.co/RlYJqodURA",
              |      "display_url":"pic.twitter.com/RlYJqodURA",
              |      "expanded_url":"http://twitter.com/pokopan0717/status/409294762391248896/photo/1",
              |      "type":"photo",
              |      "sizes":{
              |        "medium":{
              |          "w":600,
              |          "h":450,
              |          "resize":"fit"
              |        },
              |        "large":{
              |          "w":1024,
              |          "h":768,
              |          "resize":"fit"
              |        },
              |        "thumb":{
              |          "w":150,
              |          "h":150,
              |          "resize":"crop"
              |        },
              |        "small":{
              |          "w":340,
              |          "h":255,
              |          "resize":"fit"
              |        }
              |      },
              |      "source_status_id":409294762391248896,
              |      "source_status_id_str":"409294762391248896"
              |    }]
              |  },
              |  "favorited":false,
              |  "retweeted":false,
              |  "possibly_sensitive":false,
              |  "filter_level":"medium",
              |  "lang":"ja"
              |}""".stripMargin
    val parsed = processor.parseRow(s)
    parsed should not be empty
    parsed.get shouldBe a[Tweet]
  }

  it should "handle delete messages correctly" in {
    val s = """{"delete":{"status":{"id":409312837010325504,"user_id":359824852,"id_str":"409312837010325504","user_id_str":"359824852"}}}"""
    val parsed = processor.parseRow(s)
    parsed should not be empty
    parsed.get shouldBe a[DeleteTweet]
  }
}
