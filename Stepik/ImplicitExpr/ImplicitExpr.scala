object ImplicitExpr extends App {
  trait Expr[T] {
    def literalInt(value: Int): T

    def variable(name: String): T

    def times(x: T, y: T): T

    def plus(x: T, y: T): T

    def minus(x: T, y: T): T = plus(x, negate(y))

    def negate(x: T): T = times(x, literalInt(-1))
  }

  object exprSyntax {
    def literalInt[T](value: Int)(implicit expr: Expr[T]): T = expr.literalInt(value)

    def X[T](implicit expr: Expr[T]): T = expr.variable("x")

    def Y[T](implicit expr: Expr[T]): T = expr.variable("y")

    def Z[T](implicit expr: Expr[T]): T = expr.variable("z")

    implicit class IntToExpr[T](x: Int)(implicit expr: Expr[T]) {
      def lit: T = expr.literalInt(x)
    }

    implicit class NumOps[T](val x: T) extends AnyVal {
      def +(y: T)(implicit expr: Expr[T]): T = expr.plus(x, y)

      def -(y: T)(implicit expr: Expr[T]): T = expr.minus(x, y)

      def *(y: T)(implicit expr: Expr[T]): T = expr.times(x, y)

      def unary_-(implicit expr: Expr[T]): T = expr.negate(x)
    }

  }

  object Expr {
    implicit val stringExpr: Expr[String] = new Expr[String] {
      override def literalInt(value: Int): String = s"$value"

      override def variable(name: String): String = s"${name.toUpperCase}"

      override def times(x: String, y: String): String = s"($x)*($y)"

      override def plus(x: String, y: String): String = s"($x)+($y)"

      override def minus(x: String, y: String): String = s"($x)-($y)"

      override def negate(x: String): String = s"-($x)"
    }

    type Calc[T] = Map[String, T] => T

    implicit def numericExpr[T: Numeric]: Expr[Calc[T]] = new Expr[Calc[T]] {

      import Numeric.Implicits._

      override def literalInt(value: Int): Calc[T] = _ => implicitly[Numeric[T]].fromInt(value)

      override def variable(name: String): Calc[T] = map => map(name)

      override def times(x: Calc[T], y: Calc[T]): Calc[T] = map => x(map) * y(map)

      override def plus(x: Calc[T], y: Calc[T]): Calc[T] = map => x(map) + y(map)

      override def minus(x: Calc[T], y: Calc[T]): Calc[T] = map => x(map) - y(map)

      override def negate(x: Calc[T]): Calc[T] = map => -x(map)
    }

    final case class Print(s: String, priority: Int, isLit: Boolean = false) {
      println(this)

      def print(outer: Int = 0): String = if (outer <= priority) s else s"($s)"
    }

    implicit val stringOrderExpr: Expr[Print] = new Expr[Print] {
      override def literalInt(value: Int): Print = Print(s"$value", 4, isLit = true)

      override def variable(name: String): Print = Print(s"${name.toUpperCase}", 5)

      override def times(x: Print, y: Print): Print = Print(
        if (!x.isLit) s"${x.print(3)}*${y.print(3)}"
        else s"${x.print(3)}${y.print(3)}",
        3)

      override def plus(x: Print, y: Print): Print = Print(s"${x.print(2)}+${y.print(2)}", 2)

      override def minus(x: Print, y: Print): Print = Print(s"${x.print(2)}-${y.print(2)}", 2)

      override def negate(x: Print): Print = Print(s"-${x.print(1)}", 1)
    }
  }

  import Expr.Print
  import ImplicitExpr.exprSyntax._

  def function[T: Expr]: T = X * X + 2.lit * (Y + Z * X * 2.lit) - 7.lit * Z + 4.lit

  println(function[Print].print()) // X*X+2(Y+Z*X*2)-7Z+4
}
