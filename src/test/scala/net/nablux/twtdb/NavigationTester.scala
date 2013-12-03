package net.nablux.twtdb

/** Tests functionality of the site navigation (dummy class for demo purpose).
  */
class NavigationTester extends LiftSetup {

  "Clicking on 'Log In'" should "take me to login page" in {
    go to host
    // click "Log In" link in the menu
    click on XPathQuery( """//*[@id='lmenu']/a[2]""")

    pageTitle should be("App: Log In")
  }
}