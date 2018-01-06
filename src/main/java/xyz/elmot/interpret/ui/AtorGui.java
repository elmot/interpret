package xyz.elmot.interpret.ui;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class AtorGui {

    private static JTextPane textPane = new JTextPane();
    private static JTextArea outTextArea = new JTextArea();
    private static JLabel errorLabel = new JLabel(" ");

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AtorGui::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame f = createFrame();

        f.setVisible(true);
    }

    @NotNull
    private static JFrame createFrame() {
        JFrame f = new JFrame("Ator Demo");
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JSplitPane splitPane = new JSplitPane();

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JLabel("Script source"),BorderLayout.NORTH);
        textPane.setPreferredSize(new Dimension(600,600));
        leftPanel.add(textPane,BorderLayout.CENTER);
        leftPanel.add(errorLabel,BorderLayout.SOUTH);
        splitPane.setLeftComponent(leftPanel);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JLabel("Output"),BorderLayout.NORTH);
        outTextArea.setPreferredSize(new Dimension(400,100));
        rightPanel.add(outTextArea, BorderLayout.CENTER);
        splitPane.setRightComponent(rightPanel);


        f.add(splitPane);

        f.setLocationByPlatform(true);

        f.pack();
        return f;
    }
}
