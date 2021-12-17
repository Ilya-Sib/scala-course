object Main extends App {
  var tree = MyTreeSet(2, 5, 6, -7, 1, 2, -3, 4, 502394)

  // simple contains test
  assert(tree.contains(2))
  assert(tree.contains(-7))
  assert(!tree.contains(0))

  tree += 0

  // contains test
  assert(tree.contains(2))
  assert(tree.contains(-7))
  assert(tree.contains(0))
  assert(!tree.contains(123))

  var sum = 0
  tree.foreach(value => sum += value)

  // sum test
  assert(sum == List(5, 6, -7, 1, 2, -3, 4, 502394).sum)

  tree = ((tree + 10) + 5) + 1031
  sum = 0
  tree.foreach(value => sum += value)

  // sum and contains test
  assert(sum == List(5, 6, -7, 1, 2, -3, 4, 502394, 10, 1031).sum)
  assert(tree.contains(1031))
  assert(tree.contains(2))
  assert(!tree.contains(21023))

  // order test
  val vectorBuilder = Vector.newBuilder[Int]
  tree.foreach(value => vectorBuilder += value)
  assert(vectorBuilder.result() == Vector(-7, -3, 0, 1, 2, 4, 5, 6, 10, 1031, 502394))


  // stress test
  Range(0, 100000).foldLeft(tree)((acc, i) => acc + i)
  assert(Range(0, 100000).forall(tree.contains(_)))
  assert(tree.contains(-7))
  assert(tree.contains(-3))
  assert(tree.contains(502394))
  assert(!tree.contains(1293288))
}
