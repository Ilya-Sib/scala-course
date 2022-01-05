package reductions

import org.scalameter.*
import reductions.Tree.Leaf

import scala.annotation.tailrec

object LineOfSightRunner:

  val standardConfig: MeasureBuilder[Unit, Double] = config(
    Key.exec.minWarmupRuns := 40,
    Key.exec.maxWarmupRuns := 80,
    Key.exec.benchRuns := 100,
    Key.verbose := false
  ) withWarmer Warmer.Default()

  def main(args: Array[String]): Unit =
    val length = 10000000
    val input = (0 until length).map(_ % 100 * 1.0f).toArray
    val output = new Array[Float](length + 1)
    val seqtime = standardConfig measure {
      LineOfSight.lineOfSight(input, output)
    }
    println(s"sequential time: $seqtime")

    val partime = standardConfig measure {
      LineOfSight.parLineOfSight(input, output, 1000)
    }
    println(s"parallel time: $partime")
    println(s"speedup: ${seqtime.value / partime.value}")

enum Tree(val maxPrevious: Float):
  case Node(left: Tree, right: Tree) extends Tree(left.maxPrevious.max(right.maxPrevious))
  case Leaf(from: Int, until: Int, override val maxPrevious: Float) extends Tree(maxPrevious)

object LineOfSight extends LineOfSightInterface :

  def lineOfSight(input: Array[Float], output: Array[Float]): Unit = {
    @tailrec
    def loop(maxTg: Float = 0f, i: Int = 1): Unit = {
      if (i != input.length) {
        val currTg = input(i) / i
        val tg = math.max(maxTg, currTg)

        output(i) = tg
        loop(tg, i + 1)
      }
    }

    loop()
  }

  /** Traverses the specified part of the array and returns the maximum angle.
   */
  def upsweepSequential(input: Array[Float], from: Int, until: Int): Float =
    Range(from, until).map(i => input(i) / i).max

  /** Traverses the part of the array starting at `from` and until `end`, and
   * returns the reduction tree for that part of the array.
   *
   * The reduction tree is a `Tree.Leaf` if the length of the specified part of the
   * array is smaller or equal to `threshold`, and a `Tree.Node` otherwise.
   * If the specified part of the array is longer than `threshold`, then the
   * work is divided and done recursively in parallel.
   */
  def upsweep(input: Array[Float], from: Int, until: Int,
              threshold: Int): Tree =
    if (until - from < threshold) Tree.Leaf(from, until, upsweepSequential(input, from, until))
    else {
      val m = until - (until - from) / 2
      val (left, right) =
        parallel(
          upsweep(input, from, m, threshold),
          upsweep(input, m, until, threshold)
        )
      Tree.Node(left, right)
    }

  /** Traverses the part of the `input` array starting at `from` and until
   * `until`, and computes the maximum angle for each entry of the output array,
   * given the `startingAngle`.
   */
  @tailrec
  def downsweepSequential(input: Array[Float], output: Array[Float],
                          startingAngle: Float, from: Int, until: Int): Unit =
    if (from != until) {
      val tg = math.max(startingAngle, input(from) / from)
      output(from) = tg
      downsweepSequential(input, output, tg, from + 1, until)
    }

  /** Pushes the maximum angle in the prefix of the array to each leaf of the
   * reduction `tree` in parallel, and then calls `downsweepSequential` to write
   * the `output` angles.
   */
  def downsweep(input: Array[Float], output: Array[Float], startingAngle: Float,
                tree: Tree): Unit = tree match {
    case Tree.Leaf(from, until, maxPrevious) =>
      downsweepSequential(input, output, startingAngle, from, until)
    case Tree.Node(left, right) =>
      parallel(
        downsweep(input, output, startingAngle, left),
        downsweep(input, output, left.maxPrevious, right)
      )
  }

  /** Compute the line-of-sight in parallel. */
  def parLineOfSight(input: Array[Float], output: Array[Float],
                     threshold: Int): Unit = {
    val tree = upsweep(input, 1, input.length, threshold)
    downsweep(input, output, 0f, tree)
  }
