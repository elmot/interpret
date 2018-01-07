package xyz.elmot.interpret.ui

import xyz.elmot.interpret.eval.ErrorInfo
import java.awt.Color
import java.awt.event.MouseEvent
import java.util.concurrent.atomic.AtomicLong
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import javax.swing.ToolTipManager
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.*

class SyntaxTextPane : JTextPane {
    var textChangeHandler: (()->Unit)? = null
    private var errorStyle: Style? = null
    private var errors: MutableList<ErrorLocation> = mutableListOf()
    private val version = AtomicLong()

    constructor() : super() {
        init()
        initDocument()
    }

    @Suppress("unused")
    constructor(doc: StyledDocument) : super(doc) {
        init()
    }

    override fun setStyledDocument(doc: StyledDocument) {
        super.setStyledDocument(doc)
        initDocument()
    }

    override fun setDocument(doc: Document) {
        super.setDocument(doc)
        initDocument()
    }

    private fun documentChanged() {
        version.incrementAndGet()
        SwingUtilities.invokeLater({
            removeHighlights()
            textChangeHandler?.invoke()
        })
    }

    private fun init() {
        ToolTipManager.sharedInstance().registerComponent(this)
        document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) {
                documentChanged()
            }

            override fun removeUpdate(e: DocumentEvent) {
                documentChanged()
            }

            override fun changedUpdate(e: DocumentEvent) {
            }
        })
    }

    private fun initDocument() {
        errorStyle = styledDocument.addStyle("error", null)
        errorStyle?.addAttribute(StyleConstants.Background, Color.pink)
    }

    fun removeHighlights() {
        val document = styledDocument
        errors.clear()
        document.setCharacterAttributes(0, document.length, SimpleAttributeSet.EMPTY, true)
    }

    fun highlightErrors(errors: List<ErrorInfo>) {
        removeHighlights()
        val document = styledDocument
        for (info in errors) {
            val paragraph = document.defaultRootElement.getElement(info.line)
            if (paragraph != null) {
                val lineOffset = paragraph.startOffset
                val endOffset = paragraph.endOffset
                var len = info.len
                var pos = lineOffset + info.pos
                if (len <= 0) {
                    //Fallback #1 - mark text up to line end erroneous
                    len = endOffset - pos - 1
                }
                if (len <= 0 || pos >= endOffset - 1) {
                    //Fallback #2 - mark the whole line erroneous
                    pos = lineOffset
                    len = endOffset - pos - 1
                }
                document.setCharacterAttributes(pos,
                        len, errorStyle, true)
                this.errors.add(ErrorLocation(info.msg, pos, pos + len))
            }
        }
    }

    override fun getToolTipText(event: MouseEvent): String? {
        val i = viewToModel(event.point)
        return errors.stream().filter({ e -> e.start <= i && e.end > i }).map({ e -> e.msg }).findAny().orElse(null)
    }

    fun getVersion(): Long {
        return version.get()
    }

    data class ErrorLocation(val msg: String, val start: Int, val end: Int)
}