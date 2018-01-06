package xyz.elmot.interpret.ui;

import org.jetbrains.annotations.NotNull;
import xyz.elmot.interpret.Ator;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.util.List;

public class AtorGui {

    private JTextPane textPane;
    private Style errorStyle;
    private JTextArea outTextArea;
    private JLabel errorLabel;

    private AtorGui() {
        textPane = new JTextPane();
        errorStyle = textPane.getStyledDocument().addStyle("error", null);
        errorStyle.addAttribute(StyleConstants.Background, Color.pink);
        outTextArea = new JTextArea();
        errorLabel = new JLabel("Init...");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AtorGui().createAndShowGUI());
    }

    private void createAndShowGUI() {
        JFrame f = createFrame();
        textPane.getDocument().addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        runScript(textPane.getText());
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        runScript(textPane.getText());
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
        SwingUtilities.invokeLater(() ->
                textPane.getStyledDocument().setCharacterAttributes(0, Integer.MAX_VALUE, SimpleAttributeSet.EMPTY, true));
        try {
            List<Ator.ErrorInfo> errorInfos = Ator.runScript(text, s -> outTextArea.append(s));
            if (errorInfos.isEmpty()) {
                errorLabel.setForeground(Color.GREEN.darker());
                errorLabel.setText("Ready");
            } else {
                errorLabel.setForeground(Color.RED.darker());
                Ator.ErrorInfo errorInfo = errorInfos.get(0);
                errorLabel.setText(errorInfo.getMsg());
                SwingUtilities.invokeLater(() -> highlightErrors(errorInfos));
            }

        } catch (RuntimeException e) {
            errorLabel.setForeground(Color.RED.darker());
            errorLabel.setText(e.getClass().getSimpleName() + ": " + e.getMessage());
        }

    }

    private void highlightErrors(List<Ator.ErrorInfo> errorInfos) {
        StyledDocument document = textPane.getStyledDocument();
        for (Ator.ErrorInfo info : errorInfos) {
            Element element = document.getDefaultRootElement().getElement(info.getLine() - 1);
            if (element != null) {
                int lineOffset = element.getStartOffset();
                int endOffset = element.getEndOffset();
                int len = info.getLen();
                int pos = lineOffset + info.getPos();
                if (len <= 0) {
                    //Fallback #1 - mark text up to line end erroneous
                    len = endOffset - pos - 1;
                }
                if (len <= 0 || pos >= endOffset - 1) {
                    //Fallback #2 - mark whole line erroneous
                    pos = lineOffset;
                    len = element.getEndOffset() - pos - 1;
                }
                document.setCharacterAttributes(pos,
                        len, errorStyle, true);
            }
        }
    }

    @NotNull
    private JFrame createFrame() {
        JFrame f = new JFrame("Ator Demo");
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JSplitPane splitPane = new JSplitPane();

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JLabel("Script source"), BorderLayout.NORTH);
        textPane.setPreferredSize(new Dimension(600, 600));
        leftPanel.add(textPane, BorderLayout.CENTER);
        leftPanel.add(errorLabel, BorderLayout.SOUTH);
        splitPane.setLeftComponent(leftPanel);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JLabel("Output"), BorderLayout.NORTH);
        outTextArea.setPreferredSize(new Dimension(400, 100));
        rightPanel.add(outTextArea, BorderLayout.CENTER);
        splitPane.setRightComponent(rightPanel);


        f.add(splitPane);

        f.setLocationByPlatform(true);

        f.pack();
        return f;
    }
}
