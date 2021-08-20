object AuxVector extends App {

  import Vect.Aux

  trait Vect extends Any {
    type Item

    def length: Int

    def get(index: Int): Item

    def set(index: Int, item: Item): Aux[Item]
  }

  object Vect {
    type Aux[I] = Vect {type Item = I}
  }

  final case class StringVect(str: String) extends AnyVal with Vect {
    type Item = Char

    def length = str.length

    def get(index: Int) = str.charAt(index)

    def set(index: Int, item: Char): Aux[Char] = StringVect(str.updated(index, item))
  }

  final case class BoolVect64(values: Long) extends AnyVal with Vect {
    type Item = Boolean

    def length = 64

    def get(index: Int) = ((values >> index) % 2) == 1

    def set(index: Int, item: Boolean): Aux[Boolean] =
      BoolVect64(
        if (item) values | (1L << index)
        else values & ~(1L << index)
      )
  }

  final case class BoolVect8(values: Byte) extends AnyVal with Vect {
    type Item = Boolean

    def length = 8

    def get(index: Int) = ((values >> index.toByte) % 2) == 1

    def set(index: Int, item: Boolean): Aux[Boolean] =
      BoolVect8(
        if (item) (values | (1 << index.toByte)).toByte
        else (values & ~(1 << index.toByte)).toByte
      )
  }

  def toList(vect: Vect): List[vect.Item] = {
    Range(0, vect.length).map(i => vect.get(i)).toList
  }
}