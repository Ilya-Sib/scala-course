package funsets

/**
 * This class is a test suite for the methods in object FunSets.
 *
 * To run this test suite, start "sbt" then run the "test" command.
 */
class FunSetSuite extends munit.FunSuite:

  import FunSets.*

  test("contains is implemented") {
    assert(contains(x => true, 100))
  }

  /**
   * When writing tests, one would often like to re-use certain values for multiple
   * tests. For instance, we would like to create an Int-set and have multiple test
   * about it.
   *
   * Instead of copy-pasting the code for creating the set into every test, we can
   * store it in the test class using a val:
   *
   *   val s1 = singletonSet(1)
   *
   * However, what happens if the method "singletonSet" has a bug and crashes? Then
   * the test methods are not even executed, because creating an instance of the
   * test class fails!
   *
   * Therefore, we put the shared values into a separate trait (traits are like
   * abstract classes), and create an instance inside each test method.
   *
   */

  trait TestSets:
    val s1 = singletonSet(1)
    val s2 = singletonSet(2)
    val s3 = singletonSet(3)
    val neg: FunSet = x => x < 0

  /**
   * This test is currently disabled (by using @Ignore) because the method
   * "singletonSet" is not yet implemented and the test would fail.
   *
   * Once you finish your implementation of "singletonSet", remove the
   * .ignore annotation.
   */
  test("singleton set one contains one") {

    /**
     * We create a new instance of the "TestSets" trait, this gives us access
     * to the values "s1" to "s3".
     */
    new TestSets:
      /**
       * The string argument of "assert" is a message that is printed in case
       * the test fails. This helps identifying which assertion failed.
       */
      assert(contains(s1, 1), "Singleton")
  }

  test("union contains all elements of each set") {
    new TestSets:
      var s = union(s1, s2)
      assert(contains(s, 1), "Union 1")
      assert(contains(s, 2), "Union 2")
      assert(!contains(s, 3), "Union 3")
      s = union(neg, s)
      assert(contains(s, -50), "Union 1")
      assert(contains(s, 2), "Union 2")
      assert(!contains(s, 3), "Union 3")
  }

  test("intersect contains elements of every set") {
    new TestSets:
      var s = intersect(s1, s2)
      assert(!contains(s, 1), "Union 1")
      assert(!contains(s, 2), "Union 2")
      assert(!contains(s, 3), "Union 3")
      s = intersect(s1, s1)
      assert(contains(s, 1), "Union 1")
      assert(!contains(s, 2), "Union 2")
      s = intersect(s, neg)
      assert(!contains(s, 1), "Union 1")
      assert(!contains(s, 2), "Union 2")
  }

  // Test description: diff of {1,2,3,4} and {-1000,0}(funsets.FunSetSuite)
  test("diff contains elements one of sets") {
    new TestSets:
      var s = diff(s1, s2)
      assert(contains(s, 1), "Union 1")
      assert(!contains(s, 2), "Union 2")
      assert(!contains(s, 3), "Union 3")
      s = diff(neg, s)
      assert(contains(s, -50), "Union 1")
      assert(!contains(s, 2), "Union 2")
      assert(!contains(s, 3), "Union 3")
      s = diff(s1, s1)
      assert(!contains(s, 1), "Union 1")
      assert(!contains(s, 2), "Union 2")
      s = diff(union(singletonSet(1), union(singletonSet(2),
        union(singletonSet(3), singletonSet(4)))),
        union(singletonSet(-1000), singletonSet(0)))
      assert(!contains(s, -1000))
      assert(!contains(s, 0))
  }

  test("filter test") {
    new TestSets:
      var s = filter(union(union(s1, s2), s3), x => x < 2)
      assert(contains(s, 1), "Union 1")
      assert(!contains(s, 2), "Union 2")
      assert(!contains(s, 3), "Union 3")
      s = filter(union(intersect(s1, s1), neg), x => x > 0)
      assert(contains(s, 1), "Union 1")
      assert(!contains(s, 2), "Union 2")
      assert(!contains(s, 0), "Union 2")
      assert(!contains(s, -20), "Union 2")
  }

  test("forall test") {
    new TestSets:
      var s = union(union(s1, s2), s3)
      assert(forall(s, x => x < 4), "Union 1")
      assert(forall(s, x => x > 0), "Union 2")
      assert(!forall(s, x => x < 3), "Union 3")
      s = diff(neg, s)
      assert(forall(s, x => x < 4), "Union 1")
      assert(!forall(s, x => x > -5), "Union 2")
      assert(forall(s, x => x < 0), "Union 3")
  }

  test("exists test") {
    new TestSets:
      var s = union(union(s1, s2), s3)
      assert(!exists(s, x => x < 0), "Union 1")
      assert(!exists(s, x => x > 4), "Union 2")
      assert(exists(s, x => x < 3), "Union 3")
      s = diff(neg, s)
      assert(!exists(s, x => x > 4), "Union 1")
      assert(exists(s, x => x < 0), "Union 2")
      assert(!exists(s, x => x == 2), "Union 3")
  }

  test("map test") {
    new TestSets:
      var s = map(union(union(s1, s2), s3), x => 3 * x)
      assert(contains(s, 6), "Union 1")
      assert(contains(s, 9), "Union 2")
      assert(!contains(s, 12), "Union 3")
      s = map(intersect(s1, s1), x => x + 1)
      assert(!contains(s, 1), "Union 1")
      assert(contains(s, 2), "Union 2")
  }

  import scala.concurrent.duration.*
  override val munitTimeout = 10.seconds
