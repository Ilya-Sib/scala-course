package scalashop

import org.scalameter.*

import java.util.concurrent.ForkJoinTask
import scala.annotation.tailrec

object VerticalBoxBlurRunner:

  val standardConfig: MeasureBuilder[Unit, Double] = config(
    Key.exec.maxWarmupRuns := 10,
    Key.exec.minWarmupRuns := 5,
    Key.exec.benchRuns := 10,
    Key.verbose := false
  ) withWarmer Warmer.Default()

  def main(args: Array[String]): Unit =
    val radius = 3
    val width = 1920
    val height = 1080
    val src = Img(width, height)
    val dst = Img(width, height)
    val seqtime = standardConfig measure {
      VerticalBoxBlur.blur(src, dst, 0, width, radius)
    }
    println(s"sequential blur time: $seqtime")

    val numTasks = 32
    val partime = standardConfig measure {
      VerticalBoxBlur.parBlur(src, dst, numTasks, radius)
    }
    println(s"fork/join blur time: $partime")
    println(s"speedup: ${seqtime.value / partime.value}")


/** A simple, trivially parallelizable computation. */
object VerticalBoxBlur extends VerticalBoxBlurInterface:

  /** Blurs the columns of the source image `src` into the destination image
   *  `dst`, starting with `from` and ending with `end` (non-inclusive).
   *
   *  Within each column, `blur` traverses the pixels by going from top to
   *  bottom.
   */
  def blur(src: Img, dst: Img, from: Int, end: Int, radius: Int): Unit =
    for {
      y <- 0 until src.height
      x <- from until end
    } dst(x, y) = boxBlurKernel(src, x, y, radius)

  /** Blurs the columns of the source image in parallel using `numTasks` tasks.
   *
   *  Parallelization is done by stripping the source image `src` into
   *  `numTasks` separate strips, where each strip is composed of some number of
   *  columns.
   */
  def parBlur(src: Img, dst: Img, numTasks: Int, radius: Int): Unit = {
    val taskSize = (src.width + numTasks - 1) / numTasks // math.ceil(src.width / numTasks)

    @tailrec
    def loop(from: Int = 0, end: Int = taskSize,
             result: List[ForkJoinTask[Unit]] = Nil): List[ForkJoinTask[Unit]] = {
      if (from == src.width) result
      else loop(end, math.min(src.width, end + taskSize),
        task { blur(src, dst, from, end, radius) } :: result
      )
    }

    loop().foreach(_.join)
  }

