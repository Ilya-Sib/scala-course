package quickcheck

import org.scalacheck.*
import Arbitrary.*
import Gen.*
import Prop.forAll

import scala.annotation.tailrec
import scala.util.Random

abstract class QuickCheckHeap extends Properties("Heap") with IntHeap {
  lazy val genHeap: Gen[H] = oneOf(
    const(empty),
    for {
      e <- arbitrary[Int]
      h <- oneOf(const(empty), genHeap)
    } yield insert(e, h)
  )

  given Arbitrary[H] = Arbitrary(genHeap)

  property("gen1") = forAll { (h: H) =>
    val m = if isEmpty(h) then 0 else findMin(h)
    findMin(insert(m, h)) == m
  }

  property("min2") = forAll { (e1: Int, e2: Int) =>
    findMin(insert(e1, insert(e2, empty))) == math.min(e1, e2)
  }

  property("is empty") = forAll { (e1: Int) =>
    !isEmpty(insert(e1, empty))
  }

  property("remove from single") = forAll { (e1: Int) =>
    isEmpty(deleteMin(insert(e1, empty)))
  }

  property("sorted") = forAll { (h: H) =>
    @tailrec
    def loop(res: List[Int], h: H): List[Int] =
      if (isEmpty(h)) res
      else loop(findMin(h) :: res, deleteMin(h))

    val list = loop(Nil, h)
    list == list.sorted.reverse
  }

  property("min of two") = forAll { (h1: H, h2: H) =>
    val h12 = meld(h1, h2)
    val min = if ord.lt(findMin(h1), findMin(h2)) then findMin(h1) else findMin(h2)
    findMin(h12) == min
  }
}

