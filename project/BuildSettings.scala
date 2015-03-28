import sbt._
import sbt.Keys._

import java.security.MessageDigest

// import com.earldouglas.xwp.XwpPlugin.{jetty, postProcess, webapp}
import com.earldouglas.xsbtwebplugin.WebPlugin.{container, webSettings}
import com.earldouglas.xsbtwebplugin.PluginKeys._

import sbtassembly.Plugin._
import AssemblyKeys._

object BuildSettings {
  private var numReloads: Int = 0

  val resolutionRepos = Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  )

  val gulpTarget = settingKey[File]("Gulp target directory")

  // https://vaadin.com/blog/-/blogs/browsersync-and-jrebel-for-keeping-you-in-flow
  val browserSyncFile = settingKey[File]("BrowserSync file")
  val browserSync = taskKey[Unit]("Update BrowserSync file so grunt notices")

  val basicSettings = Defaults.defaultSettings ++ Seq(
    name := "lift-poly-example",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.11.2",
    scalacOptions := Seq("-deprecation", "-unchecked", "-feature", "-language:postfixOps", "-language:implicitConversions"),
    resolvers ++= resolutionRepos
  )

  val liftAppSettings = basicSettings ++
    webSettings ++
    assemblySettings ++
    addCommandAlias("pkg", ";packageWebapp ;assembly") ++
    addCommandAlias("ccr", "~ ;container:start ;container:reload /") ++
    addCommandAlias("ccrs", "~ ;container:start ;container:reload / ;browserSync") ++
    seq(
      gulpTarget <<= (target in Compile) / "gulp",

      // add gulp stuff to webapp
      (webappResources in Compile) <+= gulpTarget,

      // rename assets files with md5 checksum
      warPostProcess in Compile := {
        (warPath) =>
          val scriptsFile = new File(warPath, "scripts.js")
          val stylesFile = new File(warPath, "styles.css")

          val scriptsChecksum = checksum(scriptsFile)
          val stylesChecksum = checksum(stylesFile)

          val scriptsChecksumFile = new File(warPath, s"scripts-${scriptsChecksum}.js")
          val stylesChecksumFile = new File(warPath, s"styles-${stylesChecksum}.css")

          IO.move(scriptsFile, scriptsChecksumFile)
          IO.move(stylesFile, stylesChecksumFile)

          val mapFile = new File(warPath / "WEB-INF" / "classes", "assets.json")
          val mapFileContents =
            s"""|{
                |  "scripts": "${scriptsChecksumFile.getName}",
                |  "styles": "${stylesChecksumFile.getName}"
                |}
                |""".stripMargin

          IO.write(mapFile, mapFileContents)
      },

      browserSyncFile := (target in Compile).value / "browser-sync.txt",
      browserSync := {
        numReloads = numReloads + 1
        IO.write(browserSyncFile.value, numReloads.toString)
      },

      // include webapp target dir in the assembled jar
      resourceGenerators in Compile <+= (resourceManaged, baseDirectory) map { (managedBase, base) =>
        val webappBase = base / "target" / "webapp"
        for {
          (from, to) <- webappBase ** "*" x rebase(webappBase, managedBase / "main" / "webapp")
        } yield {
          Sync.copy(from, to)
          to
        }
      },

      // This doesn't do what the `pkg` alias does above (webapp dir is not included)
      //assembly <<= assembly dependsOn (packageWebapp in Compile),

      mainClass in assembly := Some("code.JettyLauncher")
    ) // ++ jetty()

  lazy val noPublishing = seq(
    publish := (),
    publishLocal := ()
  )

  private def checksum(file: File): String = {
    val digest = MessageDigest.getInstance("MD5")
    digest.digest(IO.readBytes(file)).map("%02x".format(_)).mkString
  }
}
