package code

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext

object JettyLauncher extends App {
  val port = Option(System.getProperty("port")).getOrElse("8080").toInt
  val server = new Server(port)

  val webapp = new WebAppContext(getClass.getClassLoader.getResource("webapp").toExternalForm, "/")
  webapp.setServer(server)
  webapp.setContextPath("/")

  server.setHandler(webapp)
  server.start
  server.join
}
