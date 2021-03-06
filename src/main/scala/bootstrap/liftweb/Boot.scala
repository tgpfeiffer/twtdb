package bootstrap.liftweb

import net.liftweb._
import util._
import Helpers._

import common._
import http._
import sitemap._
import Loc._

import net.nablux.twtdb.lib._
import net.nablux.twtdb.model._
import net.liftmodules.FoBo

import scala.language.postfixOps
import scala.xml.Text

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {
    // where to search snippet
    LiftRules.addToPackages("net.nablux.twtdb")

    //The SiteMap is built in the Site object below
    LiftRules.setSiteMapFunc(() => Site.sitemap)

    //Init the FoBo - Front-End Toolkit module, 
    //see http://liftweb.net/lift_modules for more info
    FoBo.InitParam.JQuery = FoBo.JQuery1102
    FoBo.InitParam.ToolKit = FoBo.Bootstrap301
    FoBo.init()

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // What is the function to test if a user is logged in?
    LiftRules.loggedInTest = Empty

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))

    LiftRules.noticesAutoFadeOut.default.set((notices: NoticeType.Value) => {
      notices match {
        case NoticeType.Notice => Full((8 seconds, 4 seconds))
        case _ => Empty
      }
    }
    )
  }

  // OAuth callback
  LiftRules.dispatch.append(OauthHelper)
}

object Site {
  val divider1 = Menu("divider1") / "divider1"
  val ddLabel1 = Menu.i("UserDDLabel") / "ddlabel1"
  val home = Menu.i("Home") / "index"
  val oauthLogin = Menu("OauthLogin", S.loc("fobo.menu.loc.login", scala.xml.Text(S.?("login"))) ++ Text("/") ++
    S.loc("fobo.menu.loc.logout", scala.xml.Text(S.?("login")))) / "oauth_login"
  val static = Menu(Loc("Static", Link(List("static"), true, "/static/index"), S.loc("StaticContent", scala.xml.Text("Static Content")), Hidden))
  val twbs = Menu(Loc("Bootstrap3", Link(List("bootstrap301"), true, "/bootstrap301/index"), S.loc("Bootstrap3", scala.xml.Text("Bootstrap3")), LocGroup("lg2")))

  def sitemap = SiteMap(
    home >> LocGroup("lg1"),
    oauthLogin >> LocGroup("lg1"),
    static,
    twbs,
    ddLabel1 >> LocGroup("topRight") >> PlaceHolder submenus (
      divider1 >> FoBo.TBLocInfo.Divider
      )
  )
}
