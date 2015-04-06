import sbt._
import sbt.Keys._

import java.security.MessageDigest

import com.earldouglas.xsbtwebplugin.WebPlugin.{container, webSettings}
import com.earldouglas.xsbtwebplugin.PluginKeys._

import com.typesafe.sbt.web.PathMapping
import com.typesafe.sbt.web.pipeline.Pipeline
import com.typesafe.sbt.web.Import._
import com.typesafe.sbt.less.Import.LessKeys
import com.typesafe.sbt.uglify.Import._
import com.typesafe.sbt.jshint.Import._
import com.typesafe.sbt.mocha.Import._
import com.typesafe.sbt.rjs.Import._
import net.ground5hark.sbt.concat.Import._

object BuildSettings {
  private var numReloads: Int = 0

  val resolutionRepos = Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  )

  val prepareAssets = taskKey[Unit]("prepare-assets")
  val copyVendorAssets = taskKey[Pipeline.Stage]("Copy vendor assets to dist directory")
  val assetDist = settingKey[File]("Asset dist directory")
  val webjarsDir = settingKey[File]("WebJars directory")
  val webappDir = settingKey[File]("Webapp directory")

  // https://vaadin.com/blog/-/blogs/browsersync-and-jrebel-for-keeping-you-in-flow
  val browserSyncFile = settingKey[File]("BrowserSync file")
  val browserSync = taskKey[Unit]("Update BrowserSync file so grunt notices")

  val basicSettings = Defaults.defaultSettings ++ Seq(
    name := "lift-poly-example",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.11.5",
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
    addCommandAlias("ccr", "~ ;container:start ;container:reload /") ++
    addCommandAlias("ccrs", "~ ;container:start ;container:reload / ;browserSync") ++
    seq(
      assetDist := (WebKeys.webTarget in Assets).value / "dist",
      webjarsDir := (WebKeys.webTarget in Assets).value / "web-modules" / "main" / "webjars",
      webappDir := baseDirectory.value / "src" / "main" / "webapp",

      LessKeys.sourceMap in Assets := false,
      LessKeys.compress in Assets := true,

      UglifyKeys.sourceMap := false,
      UglifyKeys.mangle := false,

      Concat.parentDir := "dist",
      Concat.groups := Seq(
        "styles.css" -> group(Seq("less/main.min.css") ++ vendorCss),
        "scripts.js" -> group(vendorJs ++ srcJs)
      ),

      RjsKeys.appDir := webappDir.value,
      // RjsKeys.baseUrl := "js",
      RjsKeys.mainConfig := "common",
      RjsKeys.dir := baseDirectory.value,
      RjsKeys.modules := Seq(
        WebJs.JS.Object(
          "name" -> "common",
          "include" -> Seq(
            "jquery",
            "bootstrap"
          )
        ),
        WebJs.JS.Object(
          "name" -> "user/login",
          "exclude" -> Seq(
            "common"
          )
        )
      ),

      copyVendorAssets := { mappings: Seq[PathMapping] =>
        val web = webjarsDir.value / "lib"
        val dist = assetDist.value

        // bootstrap font icons
        IO.copyDirectory(
          web / "bootstrap" / "fonts",
          dist / "fonts"
        )
        mappings
      },

      pipelineStages in Assets := Seq(uglify, concat, copyVendorAssets),
      pipelineStages := Seq(rjs),

      prepareAssets := {
        val a = (JshintKeys.jshint in Compile).value
        val b = (LessKeys.less in Compile).value
        val c = (WebKeys.pipeline in Assets).value
        ()
      },

      (packageWebapp in Compile) <<= (packageWebapp in Compile) dependsOn ((compile in Compile), MochaKeys.mocha),
      (start in container.Configuration) <<= (start in container.Configuration) dependsOn ((compile in Compile), prepareAssets),

      // add assetDist, where sbt-web plugins publish to, to the webapp
      (webappResources in Compile) <+= assetDist,
      (webappResources in Compile) <+= webjarsDir,

      // rename assets files with md5 checksum
      warPostProcess in Compile := {
        (warPath) =>
          val files: Seq[File] =
            (warPath / "scripts.js") ::
            (warPath / "styles.css") ::
            Nil

          val mapFile = warPath / "WEB-INF" / "classes" / "assets.json"
          val mapFileMappings = files
            .map(digestFile)
            .map { case (orig, hashed) => s""" "${orig}": "${hashed}" """ }
            .mkString(",")

          IO.write(mapFile, s"{ $mapFileMappings }")
      },

      browserSyncFile := (target in Compile).value / "browser-sync.txt",
      browserSync := {
        numReloads = numReloads + 1
        IO.write(browserSyncFile.value, numReloads.toString)
      }

    )

  lazy val noPublishing = seq(
    publish := (),
    publishLocal := ()
  )

  private def checksum(file: File): String = {
    val digest = MessageDigest.getInstance("MD5")
    digest.digest(IO.readBytes(file)).map("%02x".format(_)).mkString
  }

  private def digestFile(file: File): (String, String) = {
    val digest = checksum(file)
    val (base, ext) = file.baseAndExt
    val newFilename = s"$base-$digest.$ext"
    val newFile = new File(file.getParent, newFilename)

    IO.move(file, newFile)

    (file.getName, newFilename)
  }
}
