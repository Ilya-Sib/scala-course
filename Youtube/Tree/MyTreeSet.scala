import scala.annotation.tailrec
import scala.language.implicitConversions
import scala.util.Random

class MyTreeSet extends SimpleTreeSet {
  private sealed case class Node(
                                  value: Int,
                                  priority: Int = Random.nextInt(),
                                  var left: Option[Node] = None,
                                  var right: Option[Node] = None
                                )
  private implicit def NodeToOption(node: Node): Option[Node] = Option(node)

  private var root: Option[Node] = None

  private def split(value: Int, node: Option[Node] = root): (Option[Node], Option[Node]) = {
    node match {
      case None => (None, None)
      case Some(nodeValue) =>
        if (value > nodeValue.value) {
          val (left, right) = split(value, nodeValue.right)
          nodeValue.right = left
          (nodeValue, right)
        } else {
          val (left, right) = split(value, nodeValue.left)
          nodeValue.left = right
          (left, nodeValue)
        }
    }
  }

  private def merge(left: Option[Node], right: Option[Node]): Option[Node] = {
    (left, right) match {
      case (None, _) => right
      case (_, None) => left
      case (Some(leftValue), Some(rightValue)) =>
        if (leftValue.priority > rightValue.priority) {
          leftValue.right = merge(leftValue.right, rightValue)
          leftValue
        } else {
          rightValue.left = merge(leftValue, rightValue.left)
          rightValue
        }
    }
  }

  override def +(value: Int): MyTreeSet = {
    if (!contains(value)) {
      val (left, right) = split(value)
      root = merge(merge(left, Node(value)), right)
    }
    this
  }

  @tailrec
  private def find(value: Int, node: Option[Node] = root): Option[Node] = {
    node match {
      case None => None
      case Some(nodeValue) =>
        if (nodeValue.value == value) node
        else if (nodeValue.value < value) find(value, nodeValue.right)
        else find(value, nodeValue.left)
    }
  }

  override def contains(value: Int): Boolean = find(value).isDefined

  private def walkInOrder(f: Int => Unit, node: Option[Node] = root): Unit = {
    node match {
      case Some(nodeValue) =>
        if (nodeValue.left.isDefined) walkInOrder(f, nodeValue.left)
        f(nodeValue.value)
        if (nodeValue.right.isDefined) walkInOrder(f, nodeValue.right)
    }
  }

  override def foreach(f: Int => Unit): Unit = walkInOrder(f)
}

object MyTreeSet {
  def apply() = new MyTreeSet()
  def apply(args: Int*): MyTreeSet =
    args.foldLeft(apply())((acc, arg) => acc + arg)
}