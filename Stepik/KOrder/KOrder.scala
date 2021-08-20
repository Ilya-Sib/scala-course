import scala.annotation.tailrec
import scala.io.StdIn.{readInt, readLine}

object KOrder {
  def main(args: Array[String]) {
    @tailrec
    def kOrder(list: List[Int], k: Int): Int = {
      val element = list.head

      @tailrec
      def go(restElements: List[Int], smaller: List[Int] = Nil, equal: List[Int] = Nil, higher: List[Int] = Nil)
      : (List[Int], List[Int], List[Int]) = {
        restElements match {
          case Nil => (smaller, equal, higher)
          case restHead :: restTail =>
            if (restHead < element) go(restTail, restHead :: smaller, equal, higher)
            else if (restHead > element) go(restTail, smaller, equal, restHead :: higher)
            else go(restTail, smaller, restHead :: equal, higher)
        }
      }

      val (smaller, equal, higher) = go(list)

      if (smaller.size >= k) kOrder(smaller, k)
      else if (smaller.size < k && k <= list.size - higher.size) element
      else kOrder(higher, k - smaller.size - equal.size)
    }

    val k = readInt()
    val list = readLine().split(' ').map(_.toInt).toList
    println(kOrder(list, k))
  }
}