package classifier

object TextClass extends Enumeration {
  type TextClass = Value
  val NEGATIVE: Value = Value(-1)
  val NEUTRAL: Value = Value(0)
  val POSITIVE: Value = Value(1)
}
