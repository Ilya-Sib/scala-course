package classifier

class NaiveBayesClassifier(model: ClassifierModel) {
  def classify(text: String): TextClass.Value = {
    model.getClasses.map(`class` =>
      (`class`, math.exp(findLogProbability(`class`, text)))
    ).maxBy(_._2)._1
  }

  private def findLogProbability(`class`: TextClass.Value, text: String): Double = {
    text.split(' ').map(word =>
      model.findLogOfWordInClassProbability(`class`, word)
    ).sum + model.findLogOfClassProbability(`class`)
  }
}
