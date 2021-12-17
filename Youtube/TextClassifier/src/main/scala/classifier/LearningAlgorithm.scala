package classifier

import scala.io.Source

class LearningAlgorithm {
  private val data = getData

  private def readCSV(fileName: String): List[String] = {
    val source = Source.fromFile(fileName)

    (for {line <- source.getLines()
          lineData = line.split(',')
          if lineData.length == 12}
    yield lineData(3)).toList
  }

  private def getData: Map[TextClass.Value, List[String]] = {

    val negative = readCSV("negative.csv")
    val positive = readCSV("positive.csv")

    Map(
      TextClass.NEGATIVE -> negative,
      TextClass.POSITIVE -> positive
    )
  }

  def getModel: ClassifierModel =
    new ClassifierModel(
      classWordsCount = data.view.mapValues(list =>
        splitListTest(list)
          .length
      ).toMap,

      classDocCount = data.view.mapValues(_.length).toMap,

      eachWordCountByClass = data.view.mapValues(list =>
        splitListTest(list)
          .groupBy(identity)
          .view.mapValues(_.length)
          .toMap
      ).toMap
    )

  private def splitListTest(list: List[String]): List[String] = {
    list
      .flatMap(text => text.split("\\s+"))
      .filter(_.nonEmpty)
      .map(_.trim)
  }

  def getClassifier: NaiveBayesClassifier =
    new NaiveBayesClassifier(getModel)
}
