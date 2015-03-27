package code
package config

import net.liftweb._
import common._
import http._
import json._
import mongodb._
import util._

import com.mongodb._

object MongoConfig extends Factory with Loggable {

  // configure your MongoMetaRecords to use this. See lib/RogueMetaRecord.scala.
  val defaultId = new FactoryMaker[ConnectionIdentifier](DefaultConnectionIdentifier) {}

  lazy val uri = new MongoClientURI(
    s"""mongodb://${Props.get("mongo.default.uri", "127.0.0.1:27017/lift-poly-example")}"""
  )

  def init(): Unit = {
    val client = new MongoClient(uri)

    MongoDB.defineDb(
      defaultId.vend,
      client,
      uri.getDatabase
    )

    logger.info(s"MongoDB inited: ${uri.getHosts}")
    logger.debug(s"MongoDB options: ${uri.getOptions}")
  }
}
