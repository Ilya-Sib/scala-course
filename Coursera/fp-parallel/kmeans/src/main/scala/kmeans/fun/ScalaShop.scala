package kmeans
package fun

import java.awt.*
import java.awt.event.*
import javax.swing.*
import javax.swing.event.*
import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag
import org.scalameter.*

object ScalaShop:

  class ScalaShopFrame extends JFrame("ScalaShop\u2122"):
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    setSize(800, 500)
    setLayout(BorderLayout())

    val rightpanel: JPanel = JPanel()
    rightpanel.setBorder(BorderFactory.createEtchedBorder(border.EtchedBorder.LOWERED))
    rightpanel.setLayout(BorderLayout())
    add(rightpanel, BorderLayout.EAST)

    val allControls: JPanel = JPanel()
    allControls.setLayout(BoxLayout(allControls, BoxLayout.Y_AXIS))
    rightpanel.add(allControls, BorderLayout.NORTH)

    // Color count selection
    val colorControls: JPanel = JPanel()
    colorControls.setLayout(GridLayout(0, 2))
    allControls.add(colorControls)

    val colorCountLabel: JLabel = JLabel("Colors")
    colorControls.add(colorCountLabel)

    val colorCountSpinner: JSpinner = JSpinner(SpinnerNumberModel(32, 16, 512, 16))
    colorControls.add(colorCountSpinner)

    // Initial selection
    val initSelectionControls: JPanel = JPanel()
    initSelectionControls.setLayout(GridLayout(0, 1))
    allControls.add(initSelectionControls)

    val initialSelectionGroup: ButtonGroup = ButtonGroup()

    val initSelectionLabel: JLabel = JLabel("Initial Color Selection:")
    initSelectionControls.add(initSelectionLabel)

    val uniformSamplingButton: JRadioButton = JRadioButton("Uniform Sampling")
    uniformSamplingButton.setSelected(true)
    initSelectionControls.add(uniformSamplingButton)

    val randomSamplingButton: JRadioButton = JRadioButton("Random Sampling")
    initSelectionControls.add(randomSamplingButton)

    val uniformChoiceButton: JRadioButton = JRadioButton("Uniform Choice")
    initSelectionControls.add(uniformChoiceButton)

    initialSelectionGroup.add(randomSamplingButton)
    initialSelectionGroup.add(uniformSamplingButton)
    initialSelectionGroup.add(uniformChoiceButton)

    // Initial means selection
    val convergenceControls: JPanel = JPanel()
    convergenceControls.setLayout(BoxLayout(convergenceControls, BoxLayout.Y_AXIS))
    allControls.add(convergenceControls)

    val convergenceGroup: ButtonGroup = ButtonGroup()

    val convergenceLabel: JLabel = JLabel("Convergence criteria:")
    initSelectionControls.add(convergenceLabel)

    val criteriaControls: JPanel = JPanel()
    criteriaControls.setLayout(GridLayout(0, 2))
    convergenceControls.add(criteriaControls)

    val stepConvergenceButton: JRadioButton = JRadioButton("Steps")
    criteriaControls.add(stepConvergenceButton)

    val stepCountSpinner: JSpinner = JSpinner(SpinnerNumberModel(5, 1, 50, 1))
    criteriaControls.add(stepCountSpinner)

    val etaConvergenceButton: JRadioButton = JRadioButton("Eta")
    etaConvergenceButton.setSelected(true)
    criteriaControls.add(etaConvergenceButton)

    val etaCountSpinner: JSpinner = JSpinner(SpinnerNumberModel(0.001, 0.00001, 0.01, 0.00001))
    criteriaControls.add(etaCountSpinner)

    val snrConvergenceButton: JRadioButton = JRadioButton("Sound-to-noise")
    criteriaControls.add(snrConvergenceButton)

    val snrCountSpinner: JSpinner = JSpinner(SpinnerNumberModel(40, 10, 80, 1))
    criteriaControls.add(snrCountSpinner)

    convergenceGroup.add(stepConvergenceButton)
    convergenceGroup.add(etaConvergenceButton)
    convergenceGroup.add(snrConvergenceButton)

    // Action Buttons
    val actionControls: JPanel = JPanel()
    actionControls.setLayout(GridLayout(0, 2))
    allControls.add(actionControls)

    val stepbutton: JButton = JButton("Apply filter")
    stepbutton.addActionListener((e: ActionEvent) => {
      var status = ""
      val time = measure {
        status = canvas.applyIndexedColors(getColorCount, getInitialSelectionStrategy, getConvergenceStragegy)
      }
      updateInformationBox(status, time.value)
    })
    actionControls.add(stepbutton)

    val clearButton: JButton = JButton("Reload")
    clearButton.addActionListener((e: ActionEvent) => {
      canvas.reload()
    })
    actionControls.add(clearButton)

    val info: JTextArea = JTextArea("              ")
    info.setBorder(BorderFactory.createLoweredBevelBorder)
    rightpanel.add(info, BorderLayout.SOUTH)

    val mainMenuBar: JMenuBar = JMenuBar()

    val fileMenu: JMenu = JMenu("File")
    val openMenuItem: JMenuItem = JMenuItem("Open...")
    openMenuItem.addActionListener((e: ActionEvent) => {
      val fc = JFileChooser()
      if fc.showOpenDialog(ScalaShopFrame.this) == JFileChooser.APPROVE_OPTION then {
        canvas.loadFile(fc.getSelectedFile.getPath)
      }
    })
    fileMenu.add(openMenuItem)
    val saveMenuItem: JMenuItem = JMenuItem("Save...")
    saveMenuItem.addActionListener((e: ActionEvent) => {
      val fc = JFileChooser("epfl-view.png")
      if fc.showSaveDialog(ScalaShopFrame.this) == JFileChooser.APPROVE_OPTION then {
        canvas.saveFile(fc.getSelectedFile.getPath)
      }
    })
    fileMenu.add(saveMenuItem)
    val exitMenuItem: JMenuItem = JMenuItem("Exit")
    exitMenuItem.addActionListener((e: ActionEvent) => {
      sys.exit(0)
    })
    fileMenu.add(exitMenuItem)

    mainMenuBar.add(fileMenu)

    val helpMenu: JMenu = JMenu("Help")
    val aboutMenuItem: JMenuItem = JMenuItem("About")
    aboutMenuItem.addActionListener((e: ActionEvent) => {
      JOptionPane.showMessageDialog(null, "ScalaShop, the ultimate image manipulation tool\nBrought to you by EPFL, 2015")
    })
    helpMenu.add(aboutMenuItem)

    mainMenuBar.add(helpMenu)

    setJMenuBar(mainMenuBar)

    val canvas: PhotoCanvas = PhotoCanvas()

    val scrollPane: JScrollPane = JScrollPane(canvas)

    add(scrollPane, BorderLayout.CENTER)
    setVisible(true)

    def updateInformationBox(status: String, time: Double): Unit =
      info.setText(s"$status\nTime: ${time.toInt} ms.")

    def getColorCount: Int =
      colorCountSpinner.getValue.asInstanceOf[Int]

    def getInitialSelectionStrategy: InitialSelectionStrategy =
      if randomSamplingButton.isSelected then
        RandomSampling
      else if uniformSamplingButton.isSelected then
        UniformSampling
      else
        UniformChoice

    def getConvergenceStragegy: ConvergenceStrategy =
      if stepConvergenceButton.isSelected then
        ConvergedAfterNSteps(stepCountSpinner.getValue.asInstanceOf[Int])
      else if etaConvergenceButton.isSelected then
        ConvergedAfterMeansAreStill(etaCountSpinner.getValue.asInstanceOf[Double])
      else
        ConvergedWhenSNRAbove(snrCountSpinner.getValue.asInstanceOf[Int])

  try
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)
  catch
    case _: Exception => println("Cannot set look and feel, using the default one.")

  val frame: ScalaShopFrame = ScalaShopFrame()

  def main(args: Array[String]): Unit =
    frame.repaint()

