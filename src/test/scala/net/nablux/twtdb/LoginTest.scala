package net.nablux.twtdb

import net.liftweb.util.Props

/** Tests functionality of the site navigation (dummy class for demo purpose).
  */
class LoginTest extends LiftSetup {

  "The Login page" should "not display logout button when not logged in" in {
    // go to login page and check for login/logout button
    go to (host + "/oauth_login")
    find(id("loginBtn")).isDefined should be(true)
    find(id("logoutBtn")).isDefined should be(false)
  }

  "OAuth Login" should "redirect to Twitter and back" in {
    // go to login page and click button
    go to (host + "/oauth_login")
    find(id("loginBtn")).isDefined should be(true)
    click on id("loginBtn")
    // after redirect to Twitter, click "allow" button
    currentUrl should startWith("https://api.twitter.com/oauth/authorize")
    if (find(id("username_or_email")).isDefined) {
      textField("username_or_email").value = Props.get("twitter.user", "")
      pwdField("password").value = Props.get("twitter.password", "")
    }
    click on id("allow")
    // we should now be back on the front page
    pageTitle should be("App: Home")
  }

  "The Login page" should "not display login button when logged in" in {
    // go to login page and check for login/logout button
    go to (host + "/oauth_login")
    find(id("loginBtn")).isDefined should be(false)
    find(id("logoutBtn")).isDefined should be(true)
  }

  "OAuth Logout" should "work when logged in" in {
    // go to login page and click button
    go to (host + "/oauth_login")
    find(id("logoutBtn")).isDefined should be(true)
    click on id("logoutBtn")
    // we should stay on login page
    pageTitle should be("App: Log In/Log Out")
    // check for login/logout button
    find(id("loginBtn")).isDefined should be(true)
    find(id("logoutBtn")).isDefined should be(false)
  }
}