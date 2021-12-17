package classifier

private[classifier] class ClassifierModel
(
  classWordsCount: Map[TextClass.Value, Int],
  classDocCount: Map[TextClass.Value, Int],
  eachWordCountByClass: Map[TextClass.Value, Map[String, Int]],
) {
  private val allWordsCount: Long = classWordsCount.values.sum
  private val allDocsCount: Long = classDocCount.values.sum

  def findLogOfWordInClassProbability(`class`: TextClass.Value, word: String): Double =
    math.log(
      (eachWordCountByClass(`class`).getOrElse(word, 0) + 1d) /
        (classWordsCount(`class`).toDouble + allWordsCount)
    )

  def findLogOfClassProbability(`class`: TextClass.Value): Double =
    math.log(
      classDocCount(`class`).toDouble /
        allDocsCount
    )

  def getClasses: Set[TextClass.Value] = classDocCount.keySet
}
