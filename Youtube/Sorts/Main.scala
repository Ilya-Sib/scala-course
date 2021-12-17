import scala.collection.mutable

object Main extends App {
  def mergeSort(vector: Vector[Int]): Vector[Int] = {
    vector match {
      case Vector() | Vector(_) => vector
      case _ =>
        def merge(sortedLhs: Vector[Int], sortedRhs: Vector[Int]): Vector[Int] = {
          (sortedLhs, sortedRhs) match {
            case (lhs, rhs) if lhs.nonEmpty && rhs.nonEmpty =>
              if (lhs.head < rhs.head) lhs.head +: merge(lhs.tail, rhs)
              else rhs.head +: merge(lhs, rhs.tail)
            case (lhs, rhs) => lhs ++ rhs
          }
        }

        val (lhs, rhs) = vector.splitAt(vector.size / 2)
        merge(mergeSort(lhs), mergeSort(rhs))
    }
  }

  def topNSort(vector: Vector[Int], n: Int): Vector[Int] = {
    val priorityQueue =
      vector
        .take(n)
        .to(mutable.PriorityQueue)

    for (elem <- vector.drop(n)
         if priorityQueue.nonEmpty && elem < priorityQueue.head) {
      priorityQueue.dequeue()
      priorityQueue += elem
    }

    priorityQueue.toVector.sorted
  }

  def uniqueMergeSort(vector: Vector[Int]): Vector[Int] = {
    vector match {
      case Vector() | Vector(_) => vector
      case _ =>
        def merge(lhs: Vector[Int], rhs: Vector[Int]): Vector[Int] = {
          if (lhs.nonEmpty && rhs.nonEmpty) {
            if (lhs.head < rhs.head) lhs.head +: merge(
              lhs.dropWhile(_ == lhs.head),
              rhs
            ) else if (lhs.head > rhs.head) rhs.head +: merge(
              lhs,
              rhs.dropWhile(_ == rhs.head)
            ) else lhs.head +: merge(
              lhs.dropWhile(_ == lhs.head),
              rhs.dropWhile(_ == rhs.head)
            )
          } else lhs ++ rhs
        }

        val (lhs, rhs) = vector.splitAt(vector.size / 2)
        merge(mergeSort(lhs), mergeSort(rhs))
    }
  }

  assert(mergeSort(Vector(5, 2, 7, 11, 7, 9, 2, 11, 6, 2, 0, 1, -100, 2, 17, 1020301203)) ==
    Vector(-100, 0, 1, 2, 2, 2, 2, 5, 6, 7, 7, 9, 11, 11, 17, 1020301203))
  assert(mergeSort(Vector(1)) == Vector(1))
  assert(mergeSort(Vector()) == Vector())

  assert(topNSort(Vector(2, 4, 5, -1, 0, 1, 1, 0, 214, 123, 5124, -5), 4) == Vector(-5, -1, 0, 0))
  assert(topNSort(Vector(2, 4, 5, -1, 0, 1, 1, 0, 214, 123, 5124, -5), 8) == Vector(-5, -1, 0, 0, 1, 1, 2, 4))
  assert(topNSort(Vector(2, 4, 5, -1, 0, 1, 1, 0, 214, 123, 5124, -5), 104) == Vector(-5, -1, 0, 0, 1, 1, 2, 4, 5, 123, 214, 5124))
  assert(topNSort(Vector(2, 4, 5, -1, 0, 1, 1, 0, 214, 123, 5124, -5), 0) == Vector())
  assert(topNSort(Vector(2, 4, 5, -1, 0, 1, 1, 0, 214, 123, 5124, -5), -1) == Vector())

  assert(uniqueMergeSort(Vector(5, 2, 7, 11, 7, 9, 2, 11, 6, 2, 0, 1, -100, 2, 17, 1020301203)) ==
    Vector(-100, 0, 1, 2, 5, 6, 7, 9, 11, 17, 1020301203))
  assert(uniqueMergeSort(Vector(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)) == Vector(1))
  assert(uniqueMergeSort(Vector(1)) == Vector(1))
  assert(uniqueMergeSort(Vector()) == Vector())
}
