import scala.annotation.tailrec
import scala.io.StdIn.{readInt, readLine}

object Battleship {
  object Naval {
    type Point = (Int, Int) // Клетка корабля - пара координат этой клетки на поле
    type Field = Vector[Vector[Boolean]] // Игровое поле - двумерный массив, хранящий для каждой ячейки булево значение - стоит ли на этой клетке корабль?
    type Ship = List[Point] // Корабль как список точек
    type Fleet = Map[String, Ship] // Множество всех кораблей на поле как ассоциативный массив. По строковому ключу (имени корабля) находится список точек корабля
    type Game = (Field, Fleet) // Игровое поле и список кораблей
  }

  object Lesson {
    val field = Vector(
      Vector(false, false, false, false, false, false, false, false, false, false),
      Vector(false, false, false, false, false, false, false, false, false, false),
      Vector(false, false, false, false, false, false, false, false, false, false),
      Vector(false, false, false, false, false, false, false, false, false, false),
      Vector(false, false, false, false, false, false, false, false, false, false),
      Vector(false, false, false, false, false, false, false, false, false, false),
      Vector(false, false, false, false, false, false, false, false, false, false),
      Vector(false, false, false, false, false, false, false, false, false, false),
      Vector(false, false, false, false, false, false, false, false, false, false),
      Vector(false, false, false, false, false, false, false, false, false, false)
    )
  }

  def main(args: Array[String]) {
    import Lesson.field
    import Naval.{Field, Fleet, Game, Ship}

    // определить, является ли корабль строго горизонтальным
    def isHorizontal(ship: Ship): Boolean = {
      val listY = ship.map(_._2)
      listY.forall(_ == listY.head)
    }

    // определить, является ли корабль строго вертикальным
    def isVertical(ship: Ship): Boolean = {
      val listX = ship.map(_._1)
      listX.forall(_ == listX.head)
    }

    // определить, является ли корабль подходящим по длине
    def checkSize(ship: Ship): Boolean = ship.size <= 4

    // определить, входит ли точка в поле
    def isGoodPoint(point: (Int, Int)): Boolean =
      0 <= point._1 && point._1 <= 9 && 0 <= point._2 && point._2 <= 9

    // определить, входит ли корабль в поле
    def checkBounds(ship: Ship): Boolean =
      ship.forall(isGoodPoint)

    // определить, есть ли корабли рядом
    def checkShipsNearby(ship: Ship, field: Field): Boolean = {
      (for {(x, y) <- ship
            dx <- -1 to 1
            dy <- -1 to 1}
        yield isGoodPoint((x + dx, y + dy)) &&
          field.apply(x + dx).apply(y + dy)).contains(true)
    }

    // определить, подходит ли корабль по своим характеристикам
    def validateShip(ship: Ship): Boolean =
      checkSize(ship) && (isHorizontal(ship) || isVertical(ship))

    // определить, можно ли его поставить
    def validatePosition(ship: Ship, field: Field): Boolean =
      checkBounds(ship) && !checkShipsNearby(ship, field)

    // добавить корабль во флот
    def enrichFleet(fleet: Fleet, name: String, ship: Ship): Fleet = {
      fleet + (name -> ship)
    }

    // пометить клетки, которые занимает добавляемый корабль
    @tailrec
    def markUsedCells(field: Field, ship: Ship): Field = {
      ship match {
        case Nil => field
        case (x, y) :: tail => markUsedCells(field.updated(x, field.apply(x).updated(y, true)), tail)
      }
    }

    // логика вызовов методов выше
    def tryAddShip(game: Game, name: String, ship: Ship): Game = {
      if (validateShip(ship) && validatePosition(ship, game._1)) {
        markUsedCells(game._1, ship) -> enrichFleet(game._2, name, ship)
      } else game
    }

    @tailrec
    def startGame(numberOfShips: Int, alreadyRead: Int = 0, game: Game = (field, Map())): Game = {
      if (alreadyRead == numberOfShips) game
      else {
        val (name: String, length: Int) = readLine().split(" ") match {
          case Array(name, length) => (name, length.toInt)
        }
        val ship = (for (_ <- 0 until length)
          yield readLine().split(" ") match {
            case Array(x, y) => (x.toInt, y.toInt)
          }).toList
        startGame(numberOfShips, alreadyRead + 1, tryAddShip(game, name, ship))
      }
    }

    val numberOfShips = readInt()
    val game = startGame(numberOfShips)
    println(game._2.keys.mkString("\n"))
  }
}