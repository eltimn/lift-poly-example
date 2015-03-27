import sbt._
import sbt.Keys._

import java.security.MessageDigest

// import com.earldouglas.xwp.XwpPlugin.{jetty, postProcess, webapp}
import com.earldouglas.xsbtwebplugin.WebPlugin.{container, webSettings}
import com.earldouglas.xsbtwebplugin.PluginKeys._

import com.typesafe.sbt.web.PathMapping
import com.typesafe.sbt.web.pipeline.Pipeline
import com.typesafe.sbt.web.Import._
import com.typesafe.sbt.less.Import.LessKeys
import com.typesafe.sbt.uglify.Import._
import com.typesafe.sbt.jshint.Import._
import net.ground5hark.sbt.concat.Import._

import sbtassembly.Plugin._
import AssemblyKeys._

object BuildSettings {
  private var numReloads: Int = 0

  val resolutionRepos = Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  )

  // https://vaadin.com/blog/-/blogs/browsersync-and-jrebel-for-keeping-you-in-flow
  val browserSyncFile = settingKey[File]("BrowserSync file")
  val browserSync = taskKey[Unit]("Update BrowserSync file so grunt notices")

  val prepareAssets = taskKey[Unit]("prepare-assets")
  val copyVendorAssets = taskKey[Pipeline.Stage]("Copy vendor assets to dist directory")

  val basicSettings = Defaults.defaultSettings ++ Seq(
    name := "lift-poly-example",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.11.2",
    scalacOptions := Seq("-deprecation", "-unchecked", "-feature", "-language:postfixOps", "-language:implicitConversions"),
    resolvers ++= resolutionRepos
  )

  val srcJs = Seq(
    "js/App.min.js",
    "js/views/user/Login.min.js"
  )
  val vendorJs = Seq(
    "lib/jquery/jquery.min.js",
    "lib/bootstrap/js/bootstrap.min.js",
    "query.bsAlerts.min.js",
    "jquery.bsFormAlerts.min.js",
    "liftAjax.js"
  )

  val vendorCss = Seq(
    "public/gravatar.min.css"
  )

  val liftAppSettings = basicSettings ++
    webSettings ++
    assemblySettings ++
    addCommandAlias("pkg", ";packageWebapp ;assembly") ++
    addCommandAlias("ccr", "~ ;container:start ;container:reload /") ++
    addCommandAlias("ccrs", "~ ;container:start ;container:reload / ;browserSync") ++
    seq(
      LessKeys.sourceMap in Assets := false,
      LessKeys.compress in Assets := true,

      UglifyKeys.sourceMap := false,
      UglifyKeys.mangle := false,

      Concat.parentDir := "dist",
      Concat.groups := Seq(
        "styles.css" -> group(Seq("less/main.min.css") ++ vendorCss),
        "scripts.js" -> group(vendorJs ++ srcJs)
      ),

      copyVendorAssets := { mappings: Seq[PathMapping] =>
        val webTarget = (WebKeys.webTarget in Assets).value
        // bootstrap font icons
        IO.copyDirectory(
          webTarget / "web-modules/main/webjars/lib/bootstrap/fonts",
          webTarget / "dist/fonts"
        )
        mappings
      },

      // pipelineStages in Assets := Seq(...
      //
      // which is distinct from:
      //
      // pipelineStages := Seq(...
      //
      // The former will execute only for dev mode, the latter will include the former and execute for prod mode.
      pipelineStages in Assets := Seq(uglify, concat, copyVendorAssets),

      prepareAssets := {
        val a = (JshintKeys.jshint in Compile).value
        val b = (LessKeys.less in Compile).value
        val c = (WebKeys.pipeline in Assets).value
        ()
      },

      (packageWebapp in Compile) <<= (packageWebapp in Compile) dependsOn ((compile in Compile), prepareAssets),
      (start in container.Configuration) <<= (start in container.Configuration) dependsOn ((compile in Compile), prepareAssets),

      // add managed resources, where sbt-web plugins publish to, to the webapp
      (webappResources in Compile) <+= (WebKeys.webTarget in Assets) / "dist",

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
