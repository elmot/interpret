package xyz.elmot.interpret.ui

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.border.BevelBorder

class AtorGui {
    companion object {
        val PI_EXAMPLE = "var n = 500\n" +
                "var sequence = map({0, n}, i -> (-1)^i / (2.0 * i + 1))\n" +
                "var pi = 4 * reduce(sequence, 0, x y -> x + y)\n" +
                "print \"pi = \"\n" +
                "out pi"
    }
    private val BUSY_COLOR = Color.DARK_GRAY
    private val READY_COLOR = Color.GREEN.darker()
    private val ERROR_COLOR = Color.RED.darker()
    private val scriptTextPane: SyntaxTextPane
    private val outTextArea: JTextArea
    private val errorLabel: JLabel
    private var frame: JFrame
    private val scriptExecutor: BackgroundScriptExecutor

    fun createAndShowGUI() {
        frame = JFrame("Ator Demo")
        frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        val splitPane = JSplitPane()


        val leftPanel = JPanel(BorderLayout())
        leftPanel.add(JLabel("Script source"), BorderLayout.NORTH)
        scriptTextPane.preferredSize = Dimension(600, 600)
        leftPanel.add(scriptTextPane, BorderLayout.CENTER)
        splitPane.leftComponent = leftPanel


        val rightPanel = JPanel(BorderLayout())
        rightPanel.add(JLabel("Output"), BorderLayout.NORTH)
        outTextArea.preferredSize = Dimension(400, 100)
        rightPanel.add(outTextArea, BorderLayout.CENTER)
        splitPane.rightComponent = rightPanel

        val rootPanel = JPanel(BorderLayout())
        rootPanel.add(splitPane, BorderLayout.CENTER)
        rootPanel.add(createMenu(), BorderLayout.NORTH)
        rootPanel.add(errorLabel, BorderLayout.SOUTH)
        frame.add(rootPanel)

        frame.isLocationByPlatform = true

        frame.pack()
        scriptTextPane.textChangeHandler = {
            outTextArea.text = ""
            outTextArea.isEditable = false
            errorLabel.foreground = BUSY_COLOR
            errorLabel.text = "Running..."
            scriptExecutor.runBackgroundScript(scriptTextPane.getVersion(),
                    scriptTextPane.text,
                    { result ->
                        SwingUtilities.invokeLater { processResult(result) }
                    })
        }
        frame.isVisible = true
    }

    private fun createMenu(): JComponent {
        val jMenu = JMenu("File")
        val loadPi = JMenuItem("Load Pi Example")
        loadPi.addActionListener({ scriptTextPane.text = PI_EXAMPLE })
        jMenu.add(loadPi)
        jMenu.addSeparator()
        val exit = JMenuItem("Exit", KeyEvent.VK_X)
        exit.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.ALT_MASK)
        exit.addActionListener({ frame.dispatchEvent(WindowEvent(frame, WindowEvent.WINDOW_CLOSING)) })
        jMenu.add(exit)
        val jMenuBar = JMenuBar()
        jMenuBar.add(jMenu)
        return jMenuBar
    }

    init {
        scriptTextPane = SyntaxTextPane()
        outTextArea = JTextArea()
        errorLabel = JLabel()
        errorLabel.border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        errorLabel.preferredSize = Dimension(100, 32)
        scriptExecutor = BackgroundScriptExecutor()
        frame = JFrame("Ator Demo")
    }

    private fun processResult(result: BackgroundScriptExecutor.Result) {
        if (scriptTextPane.getVersion() == result.id) {
            val errors = result.errors
            outTextArea.text = result.output.toString()
            if (errors.isEmpty()) {
                errorLabel.foreground = READY_COLOR
                errorLabel.text = "Ready"
                scriptTextPane.removeHighlights()
            } else {
                errorLabel.foreground = ERROR_COLOR
                val errorInfo = errors[0]
                var msg = errorInfo.msg
                when (errors.size) {
                    2 -> msg += " and one more error"
                    in 3..Int.MAX_VALUE -> msg += " and " + (errors.size - 1) + " more errors"
                }
                errorLabel.text = msg
                scriptTextPane.highlightErrors(errors)
            }
        }
    }

}
fun main(args: Array<String>) {
    ToolTipManager.sharedInstance().initialDelay = 100
    SwingUtilities.invokeLater({ AtorGui().createAndShowGUI() })
}
