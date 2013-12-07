package net.nablux.twtdb

import org.eclipse.jetty.server.handler.ContextHandler
import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext

import org.scalatest._
import org.scalatest.time._
import org.scalatest.selenium._

/** A trait setting up server and browser environment for Selenium tests.
  */
trait LiftSetup
  extends FlatSpec
  with BeforeAndAfterAll
  with ShouldMatchers
  with Chrome {

  private val runServer = true
  private var server: Server = null
  private val GUI_PORT = 8083
  // NB. "localhost" instead of "127.0.0.1" will not work since Twitter does
  //  not accept "localhost" as a valid location for the callback URL
  protected var host = "http://127.0.0.1:" + GUI_PORT.toString

  /** Start the Jetty server. */
  override def beforeAll() {
    if (runServer) {
      println("STARTING THE SERVER")
      server = new Server
      val scc = new SelectChannelConnector
      scc.setPort(GUI_PORT)
      server.setConnectors(Array(scc))

      val context = new WebAppContext()
      context.setServer(server)
      context.setWar("src/main/webapp")

      val context0: ContextHandler = new ContextHandler()
      context0.setHandler(context)
      server.setHandler(context0)

      server.start()
    }
    implicitlyWait(Span(2, Seconds))
    Thread.sleep(1000)
  }

  /** Stop the web driver and shut down the Jetty server. */
  override def afterAll() {
    // Close everything when done
    println("STOPPING THE WEBDRIVER")
    close()
    quit()
    if (runServer) {
      println("STOPPING THE SERVER")
      server.stop()
      server.join()
    }
    Thread.sleep(1000)
  }
}