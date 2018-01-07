package xyz.elmot.interpret.ui;

import org.jetbrains.annotations.NotNull;
import xyz.elmot.interpret.Ator;
import xyz.elmot.interpret.eval.ErrorInfo;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
    private SyntaxTextPane scriptTextPane;
    private JTextArea outTextArea;
    private JLabel errorLabel;
    private JFrame frame;

    private AtorGui() {
        scriptTextPane = new SyntaxTextPane();
        outTextArea = new JTextArea();
        errorLabel = new JLabel("Init...");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AtorGui().createAndShowGUI());
    }

    private void createAndShowGUI() {
        JFrame f = createFrame();
        scriptTextPane.getDocument().addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        runScript(scriptTextPane.getText());
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        runScript(scriptTextPane.getText());
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                    }
                }
        );

        f.setVisible(true);
    }

    private void runScript(String text) {
        outTextArea.setText("");
        outTextArea.setEditable(false);
        errorLabel.setForeground(Color.DARK_GRAY);
        errorLabel.setText("Running...");
        try {
            List<ErrorInfo> errorInfos = Ator.runScript(text, s -> outTextArea.append(s));
            if (errorInfos.isEmpty()) {
                errorLabel.setForeground(Color.GREEN.darker());
                errorLabel.setText("Ready");
                SwingUtilities.invokeLater(() -> scriptTextPane.removeHighlights());
            } else {
                errorLabel.setForeground(Color.RED.darker());
                ErrorInfo errorInfo = errorInfos.get(0);
                errorLabel.setText(errorInfo.getMsg());
                SwingUtilities.invokeLater(() -> scriptTextPane.highlightErrors(errorInfos));
            }

        } catch (RuntimeException e) {
            errorLabel.setForeground(Color.RED.darker());
            errorLabel.setText(e.getClass().getSimpleName() + ": " + e.getMessage());
        }

    }

    @NotNull
    private JFrame createFrame() {
        frame = new JFrame("Ator Demo");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JSplitPane splitPane = new JSplitPane();


        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JLabel("Script source"), BorderLayout.NORTH);

        scriptTextPane.setPreferredSize(new Dimension(600, 600));
        leftPanel.add(scriptTextPane, BorderLayout.CENTER);
        leftPanel.add(errorLabel, BorderLayout.SOUTH);
        splitPane.setLeftComponent(leftPanel);


        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JLabel("Output"), BorderLayout.NORTH);
        outTextArea.setPreferredSize(new Dimension(400, 100));
        rightPanel.add(outTextArea, BorderLayout.CENTER);
        splitPane.setRightComponent(rightPanel);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.add(splitPane, BorderLayout.CENTER);
        rootPanel.add(createMenu(), BorderLayout.NORTH);
        frame.add(rootPanel);

        frame.setLocationByPlatform(true);

        frame.pack();
        return frame;
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
