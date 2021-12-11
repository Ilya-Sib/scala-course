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
        list.map(text =>
          text.split(' ').length
        ).sum
      ).toMap,

      classDocCount = data.view.mapValues(_.length).toMap,

      eachWordCountByClass = data.view.mapValues(list =>
        list
          .flatMap(text => text.split(' '))
          .groupBy(identity)
          .view.mapValues(_.length)
          .toMap
      ).toMap
    )

  def getClassifier: NaiveBayesClassifier =
    new NaiveBayesClassifier(getModel)
}
