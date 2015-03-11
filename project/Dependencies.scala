import sbt._

object Dependencies {

  val resolutionRepos = Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  )

  def compile(deps: ModuleID*): Seq[ModuleID] = deps map (_ % "compile")
  def provided(deps: ModuleID*): Seq[ModuleID] = deps map (_ % "provided")
  def test(deps: ModuleID*): Seq[ModuleID] = deps map (_ % "test")
  def runtime(deps: ModuleID*): Seq[ModuleID] = deps map (_ % "runtime")
  def container(deps: ModuleID*): Seq[ModuleID] = deps map (_ % "container")

  object Ver {
    val lift = "2.6"
    val lift_edition = "2.6"
    val jetty = "9.2.9.v20150224"
  }

  // Lift
  val liftWebkit = "net.liftweb" %% "lift-webkit" % Ver.lift
  val liftMongodb = "net.liftweb" %% "lift-mongodb-record" % Ver.lift

  // Lift modules
  val liftExtras = "net.liftmodules" %% ("extras_"+Ver.lift_edition) % "0.4-SNAPSHOT"
  val liftMongoauth = "net.liftmodules" %% ("mongoauth_"+Ver.lift_edition) % "0.6-SNAPSHOT"

  // Rogue
  val rogueField      = "com.foursquare" %% "rogue-field"         % "2.5.0" intransitive()
  val rogueCore       = "com.foursquare" %% "rogue-core"          % "2.5.1" intransitive()
  val rogueLift       = "com.foursquare" %% "rogue-lift"          % "2.5.1" intransitive()
  val rogueIndex      = "com.foursquare" %% "rogue-index"         % "2.5.1" intransitive()

  // Jetty - http://download.eclipse.org/jetty/
  val jettyWebapp = "org.eclipse.jetty" % "jetty-webapp" % Ver.jetty
  val jettyPlus = "org.eclipse.jetty" % "jetty-plus" % Ver.jetty
  val servlet = "javax.servlet" % "javax.servlet-api" % "3.0.1"

  // Misc
  val logback = "ch.qos.logback" % "logback-classic" % "1.1.2"
  val scalatest = "org.scalatest" %% "scalatest" % "2.2.4"
}
