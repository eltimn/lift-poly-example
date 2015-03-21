package code
package model

import code.lib.RogueMetaRecord

import net.liftweb.common.{Box, Empty, Full}
import net.liftweb.json._
import net.liftweb.mongodb.{JObjectParser, ObjectIdSerializer}
import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.field.{MongoCaseClassListField, ObjectIdPk}
import net.liftweb.util.Helpers.tryo

import org.bson.types.ObjectId
import com.mongodb._

trait Shape {
  def name: String
  def description: String
  def id: ObjectId
}

case class Circle(name: String, description: String, radius: Int, id: ObjectId = ObjectId.get) extends Shape
case class Square(name: String, description: String, side: Int, id: ObjectId = ObjectId.get) extends Shape
case class Triangle(name: String, description: String, base: Int, height: Int, id: ObjectId = ObjectId.get) extends Shape

class ShapeCollection private() extends MongoRecord[ShapeCollection] with ObjectIdPk[ShapeCollection]{
  def meta = ShapeCollection

  object shapes extends MongoCaseClassListField[ShapeCollection, Shape](this) {
    override implicit lazy val formats: Formats =
      DefaultFormats.withHints(
        ShortTypeHints(List(classOf[Circle], classOf[Square], classOf[Triangle]))
      ) + new ObjectIdSerializer
  }
}

object ShapeCollection extends ShapeCollection with RogueMetaRecord[ShapeCollection]
