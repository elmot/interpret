package xyz.elmot.interpret.eval

import org.antlr.v4.runtime.ParserRuleContext

/**
 * Generic script error information, used for both syntax and runtime errors
 */
data class ErrorInfo(val msg: String, val line: Int, val pos: Int, val len: Int) {
    override fun toString(): String {
        val end = pos + len
        return "$msg[$line,$pos..$end]"
    }
}

/**
 * Service runtime exception to silently break long running scripts
 */
class CancelException : RuntimeException()

/**
 * Exception thrown because of an error
 */
class EvalException(message: String?, val context: ParserRuleContext) : RuntimeException(message)
