type Occurrences = List[(Char, Int)]

def combinations(occurrences: Occurrences): List[Occurrences] = {
  occurrences match {
    case Nil => List(Nil)
    case (ch, i) :: tail =>
      for {tailComb <- combinations(tail)
           times <- 0 to i}
      yield {
        if times == 0 then tailComb
        else (ch, times) :: tailComb
      }
  }
}

combinations(List(('a', 2), ('b', 2))) foreach println