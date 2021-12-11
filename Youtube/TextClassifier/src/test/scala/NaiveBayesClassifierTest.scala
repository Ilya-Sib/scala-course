import classifier.{LearningAlgorithm, TextClass}
import org.specs2.mutable.Specification

class NaiveBayesClassifierTest extends Specification {
  "Naive bayes classifier test" should {
    val classifier = new LearningAlgorithm().getClassifier

    classifier.classify("Угадайте чья школа учится сегодня? Причем единственная в области:(")
      .shouldEqual(TextClass.NEGATIVE)
    classifier.classify("Внезапно настало лето? Дома из обоих кранов течёт холодная вода. :-/")
      .shouldEqual(TextClass.NEGATIVE)
    classifier.classify("Как я все ненавижу")
      .shouldEqual(TextClass.NEGATIVE)
    // dependency

    classifier.classify("У нас есть прекрасная история, как сдохнуть за неделю!!)")
      .shouldEqual(TextClass.POSITIVE)
    classifier.classify("Сегодня отличный день)")
      .shouldEqual(TextClass.POSITIVE)

  }
}