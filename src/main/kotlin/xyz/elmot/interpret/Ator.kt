package xyz.elmot.interpret

import org.antlr.v4.runtime.*
import xyz.elmot.interpret.eval.ErrorInfo
import xyz.elmot.interpret.eval.EvalException
import xyz.elmot.interpret.eval.ProgVisitor

object Ator {

    fun runScript(input: String , stringConsumer: (String) -> Unit) :List<ErrorInfo> {
        val errors:MutableList<ErrorInfo>  = mutableListOf()
        val stream = CharStreams.fromString(input)
        val lexer = AtorLexer(stream)
        lexer.removeErrorListeners()
        val tokenStream = CommonTokenStream(lexer)
        val parser = AtorParser(tokenStream)
        parser.removeErrorListeners()
        parser.addErrorListener(object :BaseErrorListener (){

            override fun syntaxError(recognizer:Recognizer<*, *>?, offendingSymbol: Any?,
                                     line:Int, charPositionInLine:Int, msg:String, e:RecognitionException? ) {
                var len = 1
                if (offendingSymbol is Token && offendingSymbol.text != null) {
                    len = offendingSymbol.text.length
                }
                errors.add(ErrorInfo(msg, line - 1, charPositionInLine, len))
            }

        })

        val program = parser.program()
        if (parser.numberOfSyntaxErrors == 0) {
            val visitor = ProgVisitor(stringConsumer)
            try {
                program.accept(visitor)
            } catch (e:EvalException) {
                val start = e.context.start
                val stop = e.context.stop
                val line = start.line
                val pos = start.charPositionInLine
                var len = 0
                if (stop.line == line) {
                    len = stop.charPositionInLine - pos
                    if (stop.text != null) {
                        len += stop.text.length
                    }
                }
                if (len <= 0 && start.text != null) {
                    len = start.text.length
                }
                errors.add(ErrorInfo("" + e.message, line - 1, pos, len))
            }
        }
        return errors
    }

}