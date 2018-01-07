package xyz.elmot.interpret.ui;

import xyz.elmot.interpret.eval.ErrorInfo;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class SyntaxTextPane extends JTextPane {
    private Style errorStyle;
    private List<ErrorLocation> errors = new ArrayList<>();


    @SuppressWarnings("WeakerAccess")
    public SyntaxTextPane() {
        init();
    }

    private void init() {
        errorStyle = getStyledDocument().addStyle("error", null);
        errorStyle.addAttribute(StyleConstants.Background, Color.pink);
        ToolTipManager.sharedInstance().registerComponent(this);
        ToolTipManager.sharedInstance().setInitialDelay(100);
    }

    @SuppressWarnings("unused")
    public SyntaxTextPane(StyledDocument doc) {
        super(doc);
        init();
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
                    //Fallback #2 - mark the whole line erroneous
                    pos = lineOffset;
                    len = element.getEndOffset() - pos - 1;
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
        return errors.stream().filter(e -> e.start <= i && e.end >= i).map(e -> e.msg).findAny().orElse(null);
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
