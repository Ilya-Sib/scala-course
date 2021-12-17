package classifier

class NaiveBayesClassifier(model: ClassifierModel) {
  private val EPS = 0.5

  def classify(text: String): TextClass.Value = {
    val result = model.getClasses.map(`class` =>
      (`class`, findLogProbability(`class`, text))
    )

    val min = result.minBy(_._2)
    val max = result.maxBy(_._2)

    if (math.abs(min._2 - max._2) < EPS) TextClass.NEUTRAL
    else max._1
  }

  private def findLogProbability(`class`: TextClass.Value, text: String): Double = {
    text.split("\\s+").filter(_.nonEmpty).map(word =>
      model.findLogOfWordInClassProbability(`class`, word.trim)
    ).sum + model.findLogOfClassProbability(`class`)
  }
}
