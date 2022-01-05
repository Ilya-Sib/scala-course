package reductions

import scala.annotation.*
import org.scalameter.*

object ParallelParenthesesBalancingRunner:

  @volatile var seqResult = false

  @volatile var parResult = false

  val standardConfig: MeasureBuilder[Unit, Double] = config(
    Key.exec.minWarmupRuns := 40,
    Key.exec.maxWarmupRuns := 80,
    Key.exec.benchRuns := 120,
    Key.verbose := false
  ) withWarmer Warmer.Default()

  def main(args: Array[String]): Unit =
    val length = 100000000
    val chars = new Array[Char](length)
    val threshold = 10000
    val seqtime = standardConfig measure {
      seqResult = ParallelParenthesesBalancing.balance(chars)
    }
    println(s"sequential result = $seqResult")
    println(s"sequential balancing time: $seqtime")

    val fjtime = standardConfig measure {
      parResult = ParallelParenthesesBalancing.parBalance(chars, threshold)
    }
    println(s"parallel result = $parResult")
    println(s"parallel balancing time: $fjtime")
    println(s"speedup: ${seqtime.value / fjtime.value}")

object ParallelParenthesesBalancing extends ParallelParenthesesBalancingInterface :

  /** Returns `true` iff the parentheses in the input `chars` are balanced.
   */
  def balance(chars: Array[Char]): Boolean = {
    var count = 0

    for (ch <- chars) {
      if (count < 0) return false

      ch match {
        case '(' => count += 1
        case ')' => count -= 1
        case _ => ()
      }
    }

    count == 0
  }

  /** Returns `true` iff the parentheses in the input `chars` are balanced.
   */
  def parBalance(chars: Array[Char], threshold: Int): Boolean = {

    def traverse(idx: Int, unt: Int, arg1: Int, arg2: Int): (Int, Int) = {
      var opMinCl = arg1
      var clMinOp = arg2

      var i = idx
      while (i < unt) {
        chars(i) match {
          case '(' =>
            opMinCl += 1
            clMinOp -= 1
          case ')' =>
            clMinOp += 1
            opMinCl -= 1
          case _ => ()
        }
        i += 1
      }

      (opMinCl, clMinOp)
    }

    def reduce(from: Int, until: Int): (Int, Int) = {
      if (until - from < threshold) traverse(from, until, 0, 0)
      else {
        val m = from + (until - from) / 2

        val ((t1Open, t1Close), (t2Open, t2Close)) = parallel(
          reduce(from, m),
          reduce(m, until)
        )

        (t1Open + t2Open, t1Close + t2Close)
      }
    }

    reduce(0, chars.length) == (0, 0)
  }

// For those who want more:
// Prove that your reduction operator is associative!

