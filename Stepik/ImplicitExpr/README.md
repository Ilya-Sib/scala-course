# ImplicitExpr

Данная задача состоит из двух частей.  
  
**Часть 1**  
Мы написали трейт, представляющий собой часть математического выражения, использующего переменные, сложение, вычитание и умножение:  
```
trait Expr[T] {
  def literalInt(value: Int): T
  def variable(name: String): T
  def times(x: T, y: T): T
  def plus(x: T, y: T): T
  def minus(x: T, y: T): T = plus(x, negate(y))
  def negate(x: T): T      = times(x, literalInt(-1))
}
```
Объект ExprSyntax содержит методы, добавляющие "синтаксический сахар":  
```
object exprSyntax {
  def literalInt[T](value: Int)(implicit expr: Expr[T]): T = expr.literalInt(value)
  def X[T](implicit expr: Expr[T]): T                      = expr.variable("x")
  def Y[T](implicit expr: Expr[T]): T                      = expr.variable("y")
  def Z[T](implicit expr: Expr[T]): T                      = expr.variable("z")

  implicit class IntToExpr[T](x: Int)(implicit expr: Expr[T]) {
    def lit: T = expr.literalInt(x)
  }

  implicit class NumOps[T](val x: T) extends AnyVal {
    def +(y: T)(implicit expr: Expr[T]): T  = expr.plus(x, y)
    def -(y: T)(implicit expr: Expr[T]): T   = expr.minus(x, y)
    def *(y: T)(implicit expr: Expr[T]): T   = expr.times(x, y)
    def unary_-(implicit expr: Expr[T]): T = expr.negate(x)
  }
}
```
В качестве примера написали одну реализацию:  
```
object Expr {
  implicit val stringExpr: Expr[String] = new Expr[String] {
   ﻿ override def literalInt(value: Int): String = s"$value"
     override def variable(name: String): String      = s"${name.toUpperCase}"
     override def times(x: String, y: String): String = s"($x)*($y)"
     override def plus(x: String, y: String): String  = s"($x)+($y)"
     override def minus(x: String, y: String): String = s"($x)-($y)"
     override def negate(x: String): String           = s"-($x)"
   }
 }
```
Она расставляет в выражении скобки согласно с приоритетами операций:  
```  
def function[T: Expr]: T = X * X + 2.lit * (Y + Z * X * 2.lit) - 7.lit * Z + 4.lit
println(function[String]) // ((((X)*(X))+((2)*((Y)+(((Z)*(X))*(2)))))-((7)*(Z)))+(4)
```
Ваша задача - написать реализацию трейта, которая позволит вычислять подобные выражения на заданных значениях переменных. Подстановки переменных удобно хранить в виде функции из ассоциативного массива значений переменных в сами значения (Calc в коде ниже):  
```
type Calc[T] = Map[String, T] => T

implicit def numericExpr[T: Numeric]: Expr[Calc[T]] = new Expr[Calc[T]] {
  import Numeric.Implicits._
  // methods
}
```
Тогда наше выражение, записанное в function можно будет переиспользовать таким образом:
```
println(function[Calc[Double]].apply(Map("x" -> 1, "y" -> -1, "z" -> 0.2))) // 2.4
```
Ничего выводить в консоль и считывать не надо. Просто реализуйте недостающие методы.  
  
Дополнительная информация: 
* Подсказка: для реализации метода конвертации числа в литерал вам, скорее всего, понадобится посмотреть интерфейс scala.math.Numeric.
* Почему выражение выполняется в правильном порядке? https://docs.scala-lang.org/tour/operators.html
* Context bounds 
 `def g[A : B](a: A) = h(a)`
 https://docs.scala-lang.org/tutorials/FAQ/context-bounds.html
 

**Часть 2**
  
В прошлом степе мы реализовали аппарат по вычислению математических выражений. Давайте теперь реализуем строковое представление этих выражений.  
  
Заведём вспомогательную сущность. Она будет помогать нам расставлять скобки, где нужно:  
```
final case class Print(s: String, priority: Int, isLit: Boolean = false) {
  def print(outer: Int = 0) = if (outer <= priority) s else s"($s)"
}
```
*s* - математическое выражение, которому соответствует данный объект;  
*priority* - приоритет математического выражения. Определяется приоритетом среди всех операций, переменных и литералов, содержащихся в нём.  
*isLit* - является ли выражение литералом  
  
Список приоритетов:  
1. Унарный минус
2. Сложение и вычитание
3. Умножение
4. Литерал
5. Переменная

Примеры: 
* X * Y * 2  имеет приоритет 3
* 42 имеет приоритет 4
* X имеет приоритет 5
* -X+Y*3 имеет приоритет 1

Необходимо реализовать тот же интерфейс, что и в прошлом степе:  
```
implicit val stringOrderExpr: Expr[Print] = new Expr[Print] {  override def literalInt(value: Int) = ???
  override def variable(name: String): Print = ???
  override def times(x: Print, y: Print): Print = ???
  override def plus(x: Print, y: Print): Print  = ???
  override def minus(x: Print, y: Print): Print = ???
  override def negate(x: Print): Print          = ???
}
```
Использование выглядит так:  
```
def function[T: Expr]: T = X * X + 2.lit * (Y + Z * X * 2.lit) - 7.lit * Z + 4.lit
println(function[Print].print()) // X*X+2(Y+Z*X*2)-7Z+4
```
Разделители операций:
* Сложение - "+"
* Вычитание - "-"
* Унарный минус - "-", например "-X"
* Умножение - если первый множитель литерал, то пустая строка "", иначе - "*". Например "2X" и "X*Y"

> Ничего выводить в консоль и считывать из неё не надо. просто реализуйте недостающие методы.