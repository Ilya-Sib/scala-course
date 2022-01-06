package kmeans

import java.util.concurrent.*
import scala.collection.{Map, Seq, mutable}
import scala.collection.parallel.{ParMap, ParSeq}
import scala.collection.parallel.CollectionConverters.*
import scala.collection.parallel.immutable.ParVector
import scala.math.*

class KMeansSuite extends munit.FunSuite:
  object KM extends KMeans
  import KM.*

  def checkParClassify(points: ParSeq[Point], means: ParSeq[Point], expected: ParMap[Point, ParSeq[Point]]): Unit =
    assertEquals(classify(points, means), expected, s"classify($points, $means) should equal to $expected")

  test("'classify' should work for empty 'points' and empty 'means'") {
    val points: ParSeq[Point] = IndexedSeq().par
    val means: ParSeq[Point] = IndexedSeq().par
    val expected = ParMap[Point, ParSeq[Point]]()
    checkParClassify(points, means, expected)
  }

  test("classify(ParVector(), ParVector((1.0, 1.0, 1.0))) should equal to ParHashMap((1.0, 1.0, 1.0) -> ParVector())") {
    val points: ParSeq[Point] = ParSeq.empty[Point]
    val means: ParSeq[Point] = ParSeq(Point(1.0, 1.0, 1.0))
    val expected = ParMap(Point(1.0, 1.0, 1.0) -> ParSeq.empty[Point])
    checkParClassify(points, means, expected)
  }

  test("classify(ParVector((1.0, 1.0, 0.0), (1.0, -1.0, 0.0), (-1.0, 1.0, 0.0), (-1.0, -1.0, 0.0)), ParVector((1.0, 0.0, 0.0), (-1.0, 0.0, 0.0))) should equal to ParMap((1.0, 0.0, 0.0) -> ParVector((1.0, 1.0, 0.0), (1.0, -1.0, 0.0)), (-1.0, 0.0, 0.0) -> ParVector((-1.0, 1.0, 0.0), (-1.0, -1.0, 0.0)))") {
    val points: ParSeq[Point] = ParSeq(Point(1.0, 1.0, 0.0), Point(1.0, -1.0, 0.0), Point(-1.0, 1.0, 0.0), Point(-1.0, -1.0, 0.0))
    val means: ParSeq[Point] =  ParSeq(Point(1.0, 0.0, 0.0), Point(-1.0, 0.0, 0.0))
    val expected = ParMap(
      Point(1.0, 0.0, 0.0) -> ParVector(Point(1.0, 1.0, 0.0), Point(1.0, -1.0, 0.0)),
      Point(-1.0, 0.0, 0.0) -> ParVector(Point(-1.0, 1.0, 0.0), Point(-1.0, -1.0, 0.0))
    )
    checkParClassify(points, means, expected)
  }

  test("'kMeans' should work for 'points' == ParSeq((0, 0, 1), (0,0, -1), (0,1,0), (0,10,0)) and 'oldMeans' == ParSeq((0, -1, 0), (0, 2, 0)) and 'eta' == 12,25") {
    val points: ParSeq[Point] = ParSeq(Point(0, 0, 1), Point(0,0, -1), Point(0,1,0), Point(0,10,0))
    val oldMeans: ParSeq[Point] = ParSeq(Point(0, -1, 0), Point(0, 2, 0))
    val eta = 12.25d
    kMeans(points, oldMeans, eta) == ParSeq(Point(0.0, 0.0, 0.0), Point(0.0, 5.5, 0.0))
  }

  import scala.concurrent.duration.*
  override val munitTimeout: Duration = 10.seconds


