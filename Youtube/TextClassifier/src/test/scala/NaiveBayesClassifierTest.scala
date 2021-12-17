import classifier.{LearningAlgorithm, TextClass}
import org.specs2.mutable.Specification

class NaiveBayesClassifierTest extends Specification {
  "Naive bayes classifier test" should {
    val classifier = new LearningAlgorithm().getClassifier

    classifier.classify("Обычный, не примичательный текст")
      .shouldEqual(TextClass.NEUTRAL)
    classifier.classify("У нас есть прекрасная история, как сдохнуть за неделю!!)")
      .shouldEqual(TextClass.NEUTRAL)

    classifier.classify("Угадайте чья школа учится сегодня? Причем единственная в области:(")
      .shouldEqual(TextClass.NEGATIVE)
    classifier.classify("Внезапно настало лето? Дома из обоих кранов течёт холодная вода. :-/")
      .shouldEqual(TextClass.NEGATIVE)
    classifier.classify("Как я все ненавижу")
      .shouldEqual(TextClass.NEGATIVE)

    classifier.classify("Сегодня отличный день)")
      .shouldEqual(TextClass.POSITIVE)
    classifier.classify("Встречайте, мои супер одногруппницы, будущие историки искусства и моды:)")
      .shouldEqual(TextClass.POSITIVE)

  }
}