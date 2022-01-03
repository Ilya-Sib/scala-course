package example

import java.util.NoSuchElementException

/**
 * This class implements a munit test suite for the methods in object
 * `Lists` that need to be implemented as part of this assignment. A test
 * suite is simply a collection of individual tests for some specific
 * component of a program.
 *
 * To run this test suite, start "sbt" then run the "test" command.
 */
class ListsSuite extends munit.FunSuite:

  /**
   * Tests are written using the `test("description") { ... }` syntax
   * The most common way to implement a test body is using the method `assert`
   * which tests that its argument evaluates to `true`. So one of the simplest
   * successful tests is the following:
   */
  test("one plus one is two (0pts)") {
    assert(1 + 1 == 2)
  }

  test("one plus one is three (0pts)?") {
    assert(1 + 1 + 1 == 3) // This assertion fails! Go ahead and fix it.
  }

  /**
   * One problem with the previous (failing) test is that munit will
   * only tell you that a test failed, but it will not tell you what was
   * the reason for the failure. The output looks like this:
   *
   * {{{
   * ==> X example.ListSuite.one plus one is three (0pts)?  0.007s munit.FailException: /tmp/example/src/test/scala/example/ListSuite.scala:26 assertion failed
   * 25:  test("one plus one is two (0pts)") {
   * 26:      assert(1 + 1 == 3)
   * 27:  }
   * }}}
   *
   * This situation can be improved by using a assertEquals
   * (this is only possible in munit). So if you
   * run the next test, munit will show the following output:
   *
   * {{{
   * ==> X example.ListSuite.details why one plus one is not three (0pts)  0.006s munit.FailException: /tmp/example/src/test/scala/example/ListSuite.scala:72
   * 71:  test("details why one plus one is not three (0pts)") {
   * 72:      assertEquals(1 + 1, 3) // Fix me, please!
   * 73:  }
   * values are not the same
   * => Obtained
   * 3
   * => Diff (- obtained, + expected)
   * -3
   * +2
   * }}}
   *
   * We recommend to always use the assertEquals equality operator
   * when writing tests.
   */
  test("details why one plus one is not three (0pts)") {
    assertEquals(1 + 1 + 1, 3) // Fix me, please!
  }

  /**
   * Exceptional behavior of a methods can be tested using a try/catch
   * and a failed assertion.
   *
   * In the following example, we test the fact that the method `intNotZero`
   * throws an `IllegalArgumentException` if its argument is `0`.
   */
  test("intNotZero throws an exception if its argument is 0") {
    try
      intNotZero(0)
      fail("No exception has been thrown")
    catch
      case e: IllegalArgumentException => ()
  }

  def intNotZero(x: Int): Int =
    if x == 0 then throw IllegalArgumentException("zero is not allowed")
    else x

  /**
   * Now we finally write some tests for the list functions that have to be
   * implemented for this assignment. We fist import all members of the
   * `List` object.
   */
  import Lists.*


  /**
   * We only provide two very basic tests for you. Write more tests to make
   * sure your `sum` and `max` methods work as expected.
   *
   * In particular, write tests for corner cases: negative numbers, zeros,
   * empty lists, lists with repeated elements, etc.
   *
   * It is allowed to have multiple `assert` statements inside one test,
   * however it is recommended to write an individual `test` statement for
   * every tested aspect of a method.
   */
  test("sum of a few numbers (10pts)") {
    assertEquals(sum(List(1,2,0)), 3)
    assertEquals(sum(List(1,2,0,1,2,0,1,2,0,1,2,0,1,2,0,1,2,0)), 18)
    assertEquals(sum(List(100,222,0)), 322)
  }

  test("sum of a negate numbers (10pts)") {
    assertEquals(sum(List(-1,-2,0)), -3)
    assertEquals(sum(List(1,2,0,-1,-2,0,1,2,0,-1,-2,0,1,2,0,-1,-2,0)), 0)
    assertEquals(sum(List(-100,222,0)), 122)
  }

  test("sum of a zeroes numbers (10pts)") {
    assertEquals(sum(List(0,0,0)), 0)
    assertEquals(sum(List(0)), 0)
  }

  test("sum of a same numbers (10pts)") {
    assertEquals(sum(List(100,100,100)), 300)
    assertEquals(sum(List(111, -20, 31)), 122)
  }

  test("sum of a f(numbers) (10pts)") {
    assertEquals(sum(List(10+10,12-10,31*3)), 115)
    assertEquals(sum(List(1+1+1, -2+0, 31)), 32)
  }

  test("sum of a empty list (10pts)") {
    assertEquals(sum(Nil), 0)
  }

  test("max of a few numbers (10pts)") {
    assertEquals(max(List(3, 7, 2)), 7)
  }

  test("max of a negate numbers (10pts)") {
    assertEquals(max(List(-1,-2,0)), 0)
    assertEquals(max(List(1,2,0,-1,-2,0,1,2,0,-1,-2,0,1,2,0,-1,-2,0)), 2)
    assertEquals(max(List(-100,222,0)), 222)
  }

  test("max of a zeroes numbers (10pts)") {
    assertEquals(max(List(0,0,0)), 0)
  }

  test("max of a same numbers (10pts)") {
    assertEquals(max(List(100,100,100)), 100)
    assertEquals(max(List(111, -20, 31)), 111)
  }

  test("max of a f(numbers) (10pts)") {
    assertEquals(max(List(10+10,12-10,31*3)), 93)
    assertEquals(max(List(1+1+1, -2+0, 31)), 31)
  }

  test("max of a empty list (10pts)") {
    try
      max(Nil)
      fail("max of a empty list must return exception")
    catch
      case e: NoSuchElementException => ()
  }


  import scala.concurrent.duration.*
  override val munitTimeout = 1.seconds
