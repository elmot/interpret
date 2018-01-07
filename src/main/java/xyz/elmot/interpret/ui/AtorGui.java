package xyz.elmot.interpret.ui;

import org.jetbrains.annotations.NotNull;
import xyz.elmot.interpret.Ator;
import xyz.elmot.interpret.eval.ErrorInfo;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.List;

public class AtorGui {

    public static final String PI_EXAMPLE = "var n = 500\n" +
            "var sequence = map({0, n}, i -> (-1)^i / (2.0 * i + 1))\n" +
            "var pi = 4 * reduce(sequence, 0, x y -> x + y)\n" +
            "print \"pi = \"\n" +
            "out pi";
    private static final Color BUSY_COLOR = Color.DARK_GRAY;
    private static final Color READY_COLOR = Color.GREEN.darker();
    private static final Color ERROR_COLOR = Color.RED.darker();
    private SyntaxTextPane scriptTextPane;
    private JTextArea outTextArea;
    private JLabel errorLabel;
    private JFrame frame;

    private AtorGui() {
        scriptTextPane = new SyntaxTextPane();
        outTextArea = new JTextArea();
        errorLabel = new JLabel();
        errorLabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        errorLabel.setPreferredSize(new Dimension(100, 32));
    }

    public static void main(String[] args) {
        ToolTipManager.sharedInstance().setInitialDelay(100);
        SwingUtilities.invokeLater(() -> new AtorGui().createAndShowGUI());
    }

    private void createAndShowGUI() {
        frame = new JFrame("Ator Demo");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JSplitPane splitPane = new JSplitPane();


        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JLabel("Script source"), BorderLayout.NORTH);
        scriptTextPane.setPreferredSize(new Dimension(600, 600));
        leftPanel.add(scriptTextPane, BorderLayout.CENTER);
        splitPane.setLeftComponent(leftPanel);


        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JLabel("Output"), BorderLayout.NORTH);
        outTextArea.setPreferredSize(new Dimension(400, 100));
        rightPanel.add(outTextArea, BorderLayout.CENTER);
        splitPane.setRightComponent(rightPanel);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.add(splitPane, BorderLayout.CENTER);
        rootPanel.add(createMenu(), BorderLayout.NORTH);
        rootPanel.add(errorLabel, BorderLayout.SOUTH);
        frame.add(rootPanel);

        frame.setLocationByPlatform(true);

        frame.pack();
        scriptTextPane.setTextChangeHandler((v, text) -> runScript(text));
        frame.setVisible(true);
    }

    private void runScript(String text) {
        outTextArea.setText("");
        outTextArea.setEditable(false);
        errorLabel.setForeground(BUSY_COLOR);
        errorLabel.setText("Running...");
        try {
            List<ErrorInfo> errorInfos = Ator.runScript(text, s -> outTextArea.append(s));
            if (errorInfos.isEmpty()) {
                errorLabel.setForeground(READY_COLOR);
                errorLabel.setText("Ready");
                SwingUtilities.invokeLater(() -> scriptTextPane.removeHighlights());
            } else {
                errorLabel.setForeground(ERROR_COLOR);
                ErrorInfo errorInfo = errorInfos.get(0);
                String msg = errorInfo.getMsg();
                switch (errorInfos.size()) {
                    case 1:
                        break;
                    case 2:
                        msg += " and one more error";
                        break;
                    default:
                        msg += " and " + (errorInfos.size() - 1) + " more errors";
                        break;
                }
                errorLabel.setText(msg);
                SwingUtilities.invokeLater(() -> scriptTextPane.highlightErrors(errorInfos));
            }

        } catch (RuntimeException e) {
            errorLabel.setForeground(Color.RED.darker());
            errorLabel.setText(e.getClass().getSimpleName() + ": " + e.getMessage());
        }

    }

    @NotNull
    private JComponent createMenu() {
        JMenu jMenu = new JMenu("File");
        JMenuItem loadPi = new JMenuItem("Load Pi Example");
        loadPi.addActionListener(e -> scriptTextPane.setText(PI_EXAMPLE));
        jMenu.add(loadPi);
        jMenu.addSeparator();
        JMenuItem exit = new JMenuItem("Exit", KeyEvent.VK_X);
        exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.ALT_MASK));
        exit.addActionListener(e -> frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)));
        jMenu.add(exit);
        JMenuBar jMenuBar = new JMenuBar();
        jMenuBar.add(jMenu);
        return jMenuBar;
    }
}
