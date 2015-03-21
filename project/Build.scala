import sbt._
import sbt.Keys._

import com.typesafe.sbt.web.SbtWeb

object LiftProjectBuild extends Build {

  import Dependencies._
  import BuildSettings._

  lazy val root = Project("lift-poly-example", file("."))
    .settings(liftAppSettings: _*)
    .settings(libraryDependencies ++=
      compile(
        liftWebkit,
        liftMongodb,
        liftExtras,
        liftMongoauth,
        logback,
        rogueField,
        rogueCore,
        rogueLift,
        rogueIndex
      ) ++
      test(scalatest)
    )
    .enablePlugins(SbtWeb)
}
