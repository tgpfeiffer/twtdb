name := "twtdb - A Twitter Dashboard"

version := "0.0.1"

organization := "net.liftweb"

scalaVersion := "2.10.0"

resolvers ++= Seq("snapshots"     at "http://oss.sonatype.org/content/repositories/snapshots",
                  "staging"       at "http://oss.sonatype.org/content/repositories/staging",
                  "releases"      at "http://oss.sonatype.org/content/repositories/releases"
                 )

seq(com.github.siasia.WebPlugin.webSettings :_*)

unmanagedResourceDirectories in Test <+= (baseDirectory) { _ / "src/main/webapp" }

// for Selenium tests, prefer sequential execution
parallelExecution in Test := false

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

libraryDependencies ++= {
  val liftVersion = "2.6-M2"
  Seq(
    "net.liftweb"       %% "lift-webkit"        % liftVersion        % "compile",
    "net.liftweb"       %% "lift-mapper"        % liftVersion        % "compile",
    "net.liftmodules"   %% "fobo_2.6"           % "1.1"              % "compile",
    "org.eclipse.jetty" % "jetty-webapp"        % "8.1.7.v20120910"  % "container,test",
    "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container,test" artifacts Artifact("javax.servlet", "jar", "jar"),
    "ch.qos.logback"    % "logback-classic"     % "1.0.6",
    "org.specs2"        %% "specs2"             % "1.14"             % "test",
    "org.scalatest"     % "scalatest_2.10"      % "2.0"              % "test",
    // Selenium 2.37.0 requires manual addition of httpclient 4.3.1,
    // see <http://code.google.com/p/selenium/issues/detail?id=6432>
    "org.seleniumhq.selenium" % "selenium-java" % "2.37.0"           % "test",
    "org.apache.httpcomponents" % "httpclient" % "4.3.1",
    "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
    "com.h2database"    % "h2"                  % "1.3.167"
  )
}

