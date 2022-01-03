package scalashop

import org.scalameter.*
import java.awt.*
import java.awt.event.*
import javax.swing.*
import javax.swing.event.*
import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag

object ScalaShop:

  class ScalaShopFrame extends JFrame("ScalaShop\u2122"):
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

    val filterLabel: JLabel = JLabel("Filter")
    controls.add(filterLabel)

    val filterCombo: JComboBox[String] = JComboBox(Array(
      "horizontal-box-blur",
      "vertical-box-blur"
    ))
    controls.add(filterCombo)

    val radiusLabel: JLabel = JLabel("Radius")
    controls.add(radiusLabel)

    val radiusSpinner: JSpinner = JSpinner(SpinnerNumberModel(3, 1, 16, 1))
    controls.add(radiusSpinner)

    val tasksLabel: JLabel = JLabel("Tasks")
    controls.add(tasksLabel)

    val tasksSpinner: JSpinner = JSpinner(SpinnerNumberModel(32, 1, 128, 1))
    controls.add(tasksSpinner)

    val stepbutton: JButton = JButton("Apply filter")
    stepbutton.addActionListener((e: ActionEvent) => {
      val time = measure {
        canvas.applyFilter(getFilterName, getNumTasks, getRadius)
      }
      updateInformationBox(time.value)
    })
    controls.add(stepbutton)

    val clearButton: JButton = JButton("Reload")
    clearButton.addActionListener((e: ActionEvent) => {
      canvas.reload()
    })
    controls.add(clearButton)

    val info: JTextArea = JTextArea("   ")
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

    def updateInformationBox(time: Double): Unit =
      info.setText(s"Time: $time")

    def getNumTasks: Int = tasksSpinner.getValue.asInstanceOf[Int]

    def getRadius: Int = radiusSpinner.getValue.asInstanceOf[Int]

    def getFilterName: String =
      filterCombo.getSelectedItem.asInstanceOf[String]


  try
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)
  catch
    case _: Exception => println("Cannot set look and feel, using the default one.")

  val frame: ScalaShopFrame = ScalaShopFrame()

  def main(args: Array[String]): Unit =
    frame.repaint()

