package code.snippet

import net.liftweb.common._
import net.liftweb.http.LiftRules
import net.liftweb.json._
import net.liftweb.util.Helpers._
import net.liftweb.util.Props

object Assets {
  private lazy val assetsMap: Map[String, String] = {
    if (Props.mode == Props.RunModes.Development)
      Map.empty
    else {
      (LiftRules
        .loadResourceAsString("/assets.json")
        .flatMap { s => tryo(JsonParser.parse(s)) }
      ) match {
        case Full(jo: JObject) => jo.values.mapValues(_.toString)
        case _ => Map.empty
      }
    }
  }

  private def buildPath(asset: String): String =
    "/"+assetsMap.getOrElse(asset, asset)

  private lazy val stylesPath: String = buildPath("styles.css")
  private lazy val scriptsPath: String = buildPath("scripts.js")

  def styles = "* [href]" #> stylesPath
  def scripts = "* [src]" #> scriptsPath
}
