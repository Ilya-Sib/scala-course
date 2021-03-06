package recfun

object RecFun extends RecFunInterface :

  def main(args: Array[String]): Unit =
    println("Pascal's Triangle")
    for row <- 0 to 10 do
      for col <- 0 to row do
        print(s"${pascal(col, row)} ")
      println()

  /**
   * Exercise 1
   */
  def pascal(c: Int, r: Int): Int =
    if (c < 0 || c > r) 0
    else if (c == 0 || c == r) 1
    else pascal(c - 1, r - 1) + pascal(c, r - 1)

  /**
   * Exercise 2
   */
  def balance(chars: List[Char]): Boolean = {
    def balanceRec(chars: List[Char], balance: Int): Boolean = {
      if (balance < 0) false
      else
        chars match {
          case Nil => balance == 0
          case head :: tail =>
            if (head == '(') balanceRec(tail, balance + 1)
            else if (head == ')') balanceRec(tail, balance - 1)
            else balanceRec(tail, balance)
        }
    }

    balanceRec(chars, 0)
  }

  /**
   * Exercise 3
   */
  def countChange(money: Int, coins: List[Int]): Int = {
    if (money < 0) 0
    else if (money == 0) 1
    else {
      coins match {
        case Nil => 0
        case head :: tail =>
          countChange(money - head, coins) + countChange(money, tail)
      }
    }
  }
