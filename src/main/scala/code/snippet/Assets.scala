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
      (
        LiftRules
          .loadResourceAsString("/assets.json")
          .flatMap { s => tryo(JsonParser.parse(s)) }
      ) match {
        case Full(jo: JObject) => jo.values.mapValues(_.toString)
        case _ => Map.empty
      }
    }
  }

  private lazy val cssPath: String = "/"+assetsMap.getOrElse("styles", "styles.css")
  private lazy val jsPath: String = "/"+assetsMap.getOrElse("scripts", "scripts.js")

  def css = "* [href]" #> cssPath
  def js = "* [src]" #> jsPath
}
