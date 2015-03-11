package code.model

import code.BaseMongoSessionWordSpec

/**
 * Created by Nataly on 05/03/2015.
 */
class ShapeCollectionSpec extends BaseMongoSessionWordSpec {

  override def debug = true

  "Shape Collection" should {
    "create, validate, save, and retrieve properly Shapes" in {
      val square = Square("Square", "", 10)
      val triangle = Triangle("Triangle", "", 10, 5)
      val circle = Circle("Circle", "", 50)

      val listShapes: List[Shape] = square :: triangle :: circle :: Nil

      val shapeCollection = ShapeCollection.createRecord

      shapeCollection.shapes.set(listShapes)

      val errorShapeCollection = shapeCollection.validate
      if (errorShapeCollection.length > 1) {
        fail("Validation error: " + errorShapeCollection.mkString(", "))
      }
      shapeCollection.validate.length should equal (0)
      shapeCollection.save(true)

      shapeCollection.shapes.value.map((shape: Shape) => shape match {
        case a: Square =>
          a.side should equal (10)
        case b: Triangle =>
          b.base should equal (10)
          b.height should equal (5)
        case c: Circle =>
          c.radius should equal (50)
      })

      // retrieve
      val sc = ShapeCollection.find(shapeCollection.id.get)
      sc.isDefined should equal(true)
      sc.toList.flatMap(_.shapes.get).map((shape: Shape) => shape match {
        case a: Square =>
          a.side should equal (10)
        case b: Triangle =>
          b.base should equal (10)
          b.height should equal (5)
        case c: Circle =>
          c.radius should equal (50)
      })
    }
  }

}
