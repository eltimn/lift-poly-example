import sbt._
import sbt.Keys._

import com.earldouglas.xwp.XwpPlugin
// import com.earldouglas.xsbtwebplugin.WebPlugin.{container, webSettings}
// import com.earldouglas.xsbtwebplugin.PluginKeys._

// import com.typesafe.sbt.web.pipeline.Pipeline
import com.typesafe.sbt.web.Import._
import com.typesafe.sbt.less.Import.LessKeys
import com.typesafe.sbt.uglify.Import._

import net.ground5hark.sbt.concat.Import._

import sbtbuildinfo.Plugin._
// import less.Plugin._
// import sbtclosure.SbtClosurePlugin._

object BuildSettings {

  val buildTime = settingKey[String]("build-time")
  // val webappSrcDir = settingKey[File]("webapp-dir")
  val vendorDir = settingKey[File]("vendor-dir")

  // val pkgPipelineTask = taskKey[Pipeline.Stage]("Prepare assets for packaging")

  val basicSettings = Defaults.defaultSettings ++ Seq(
    name := "lift-poly-example",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.11.2",
    scalacOptions := Seq("-deprecation", "-unchecked", "-feature", "-language:postfixOps", "-language:implicitConversions"),
    resolvers ++= Dependencies.resolutionRepos
  )

  val jsSrcs = Seq("js/App.min.js", "js/views/user/Login.min.js")
  val vendorLibs = Seq(
    "vendor/jquery-1.10.2.min.js",
    "vendor/bootstrap-3.0.2.min.js"
  )

  val liftAppSettings = basicSettings ++
    buildInfoSettings ++
    seq(
      buildTime := System.currentTimeMillis.toString,
      // webappSrcDir := sourceDirectory.value / "main" / "webapp",
      vendorDir := baseDirectory.value / "src" / "main" / "assets",

      // build-info
      buildInfoKeys ++= Seq[BuildInfoKey](buildTime),
      buildInfoPackage := "code",
      sourceGenerators in Compile <+= buildInfo,

      /*excludeFilter in uglify := new FileFilter {
        def accept(arg0: File): Boolean = {
          println("arg0.getPath: "+arg0.getPath)
          println("webappSrcDir: "+webappSrcDir.value.getPath)
          val relPath = arg0.getPath.replace(webappSrcDir.value.getPath, "")
          println("relPath: "+relPath)
          relPath.startsWith("/vendor")
        }
      },
      UglifyKeys.appDir := webappSrcDir.value,*/

      excludeFilter in uglify := new SimpleFileFilter({
        f =>
          f.relativeTo((sourceDirectory in Assets).value)
            .map(_.getPath.startsWith("vendor"))
            .getOrElse(false)
      }),

      UglifyKeys.sourceMap := false,
      // UglifyKeys.buildDir := WebKeys.webTarget.value,
      UglifyKeys.mangle := false,

      Concat.groups := Seq(
        // "script-group.js" -> group((resourceManaged.value / "js") * "*.min.js"),
        // "sources-group.js" -> group(jsSrcs),
        //"vendor-group.js" -> group(((vendorDir.value) * "*.js"))
        // "vendor-group.js" -> group(vendorLibs),
        // "combined-group.js" -> group(Seq("vendor-group.js", "sources-group.js"))

        "scripts.js" -> group(vendorLibs ++ jsSrcs)
      ),

      pipelineStages := Seq(uglify, concat),

      // less
      LessKeys.compress in Assets := true,

      // add managed resources, where sbt-web plugins publish to
      //(webappResources in Compile) <+= (WebKeys.webTarget in Assets)
      unmanagedResourceDirectories in Compile <+= {

        (WebKeys.webTarget in Assets)
      }

      /*postProcess in webapp := {
        webappDir =>
          import java.io.File
          import com.yahoo.platform.yui.compressor.YUICompressor
          val src  = new File(webappDir, "script.js")
          val dest = new File(webappDir, "script-min.js")
          YUICompressor.main(Array(src.getPath, "-o", dest.getPath))
      }*/
    ) ++ XwpPlugin.jetty()

  lazy val noPublishing = seq(
    publish := (),
    publishLocal := ()
  )
}
