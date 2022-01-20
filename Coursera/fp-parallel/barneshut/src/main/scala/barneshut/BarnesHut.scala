package barneshut

import java.awt.*
import java.awt.event.*
import javax.swing.*
import javax.swing.event.*
import scala.compiletime.uninitialized
import scala.collection.parallel.*
import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag

object 
BarnesHut:

  val model: SimulationModel = SimulationModel()

  var simulator: Simulator = uninitialized

  def initialize(parallelismLevel: Int, pattern: String, nbodies: Int): Unit =
    model.initialize(parallelismLevel, pattern, nbodies)
    model.timeStats.clear()
    simulator = Simulator(model.taskSupport, model.timeStats)

  class BarnesHutFrame extends JFrame("Barnes-Hut"):
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    setSize(1024, 600)
    setLayout(BorderLayout())

    val rightpanel: JPanel = JPanel()
    rightpanel.setBorder(BorderFactory.createEtchedBorder(border.EtchedBorder.LOWERED))
    rightpanel.setLayout(BorderLayout())
    add(rightpanel, BorderLayout.EAST)

    val controls: JPanel = JPanel()
    controls.setLayout(GridLayout(0, 2))
    rightpanel.add(controls, BorderLayout.NORTH)

    val parallelismLabel: JLabel = JLabel("Parallelism")
    controls.add(parallelismLabel)

    val items: Array[String] = (1 to Runtime.getRuntime.availableProcessors).map(_.toString).toArray
    val parcombo: JComboBox[String] = JComboBox[String](items)
    parcombo.setSelectedIndex(items.length - 1)
    parcombo.addActionListener((e: ActionEvent) => {
      initialize(getParallelism, "two-galaxies", getTotalBodies)
      canvas.repaint()
    })
    controls.add(parcombo)

    val bodiesLabel: JLabel = JLabel("Total bodies")
    controls.add(bodiesLabel)

    val bodiesSpinner: JSpinner = JSpinner(SpinnerNumberModel(25000, 32, 1000000, 1000))
    bodiesSpinner.addChangeListener((e: ChangeEvent) => {
      if frame != null then {
        initialize(getParallelism, "two-galaxies", getTotalBodies)
        canvas.repaint()
      }
    })
    controls.add(bodiesSpinner)

    val stepbutton: JButton = JButton("Step")
    stepbutton.addActionListener((e: ActionEvent) => {
      stepThroughSimulation()
    })
    controls.add(stepbutton)

    val startButton: JToggleButton = JToggleButton("Start/Pause")
    val startTimer: Timer = javax.swing.Timer(0, (e: ActionEvent) => {
      stepThroughSimulation()
    })
    startButton.addActionListener((e: ActionEvent) => {
      if startButton.isSelected then startTimer.start()
      else startTimer.stop()
    })
    controls.add(startButton)

    val quadcheckbox: JToggleButton = JToggleButton("Show quad")
    quadcheckbox.addActionListener((e: ActionEvent) => {
      model.shouldRenderQuad = quadcheckbox.isSelected
      repaint()
    })
    controls.add(quadcheckbox)

    val clearButton: JButton = JButton("Restart")
    clearButton.addActionListener((e: ActionEvent) => {
      initialize(getParallelism, "two-galaxies", getTotalBodies)
    })
    controls.add(clearButton)

    val info: JTextArea = JTextArea("   ")
    info.setBorder(BorderFactory.createLoweredBevelBorder)
    rightpanel.add(info, BorderLayout.SOUTH)

    val canvas: SimulationCanvas = SimulationCanvas(model)
    add(canvas, BorderLayout.CENTER)
    setVisible(true)

    def updateInformationBox(): Unit =
      val text = model.timeStats.toString
      frame.info.setText("--- Statistics: ---\n" + text)

    def stepThroughSimulation(): Unit =
      SwingUtilities.invokeLater(() => {
        val (bodies, quad) = simulator.step(model.bodies)
        model.bodies = bodies
        model.quad = quad
        updateInformationBox()
        repaint()
      })

    def getParallelism: Int =
      val selidx = parcombo.getSelectedIndex
      parcombo.getItemAt(selidx).toInt

    def getTotalBodies: Int = bodiesSpinner.getValue.asInstanceOf[Int]

    initialize(getParallelism, "two-galaxies", getTotalBodies)

  try
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)
  catch
    case _: Exception => println("Cannot set look and feel, using the default one.")

  val frame: BarnesHutFrame = BarnesHutFrame()

  def main(args: Array[String]): Unit =
    frame.repaint()

