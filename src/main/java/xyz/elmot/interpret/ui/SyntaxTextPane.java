package xyz.elmot.interpret.ui;

import xyz.elmot.interpret.eval.ErrorInfo;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * JTextPane supporting error highlighting, error message tooltips, and change notifications
 */
public class SyntaxTextPane extends JTextPane {
    private Style errorStyle;
    private List<ErrorLocation> errors = new ArrayList<>();
    private Runnable textChangeHandler;
    private final AtomicLong version = new AtomicLong();

    public void setTextChangeHandler(Runnable textChangeHandler) {
        this.textChangeHandler = textChangeHandler;
    }

    @SuppressWarnings("WeakerAccess")
    public SyntaxTextPane() {
        init();
        initDocument();
    }

    @SuppressWarnings("unused")
    public SyntaxTextPane(StyledDocument doc) {
        super(doc);
        init();
    }

    @Override
    public void setStyledDocument(StyledDocument doc) {
        super.setStyledDocument(doc);
        initDocument();
    }

    @Override
    public void setDocument(Document doc) {
        super.setDocument(doc);
        initDocument();
    }

    private void documentChanged() {
        version.incrementAndGet();
        SwingUtilities.invokeLater(() -> {
            removeHighlights();
            if (textChangeHandler != null) {
                textChangeHandler.run();
            }
        });
    }

    private void init() {
        ToolTipManager.sharedInstance().registerComponent(this);
        getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                documentChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                documentChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });
    }

    private void initDocument() {
        errorStyle = getStyledDocument().addStyle("error", null);
        errorStyle.addAttribute(StyleConstants.Background, Color.pink);
    }

    public void removeHighlights() {
        StyledDocument document = getStyledDocument();
        errors.clear();
        document.setCharacterAttributes(0, document.getLength(), SimpleAttributeSet.EMPTY, true);
    }

    public void highlightErrors(List<ErrorInfo> errorInfos) {
        removeHighlights();
        StyledDocument document = getStyledDocument();
        for (ErrorInfo info : errorInfos) {
            Element paragraph = document.getDefaultRootElement().getElement(info.getLine());
            if (paragraph != null) {
                int lineOffset = paragraph.getStartOffset();
                int endOffset = paragraph.getEndOffset();
                int len = info.getLen();
                int pos = lineOffset + info.getPos();
                if (len <= 0) {
                    //Fallback #1 - mark text up to line end erroneous
                    len = endOffset - pos - 1;
                }
                if (len <= 0 || pos >= endOffset - 1) {
                    //Fallback #2 - mark the whole line erroneous
                    pos = lineOffset;
                    len = endOffset - pos - 1;
                }
                document.setCharacterAttributes(pos,
                        len, errorStyle, true);
                errors.add(new ErrorLocation(info.getMsg(), pos, pos + len));
            }
        }
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        int i = viewToModel(event.getPoint());
        return errors.stream().filter(e -> e.start <= i && e.end > i).map(e -> e.msg).findAny().orElse(null);
    }

    public long getVersion() {
        return version.get();
    }

    private static class ErrorLocation {
        private final String msg;
        private final int start;
        private final int end;

        private ErrorLocation(String msg, int start, int end) {
            this.msg = msg;
            this.start = start;
            this.end = end;
        }
    }
}