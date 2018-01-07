package xyz.elmot.interpret.eval

import xyz.elmot.interpret.AtorBaseVisitor
import xyz.elmot.interpret.AtorParser
import java.util.*

class ProgVisitor(val out: (String)->Unit) : AtorBaseVisitor<Any>() {
    /**
     * Global script variables
     */
    private val vars = LinkedHashMap<String, Value>()

    override fun visitPrint(ctx: AtorParser.PrintContext): Any? {
        val text = ctx.TEXT().text
        out.invoke(text.substring(1, text.length - 1))
        return null
    }

    override fun visitStmt(ctx: AtorParser.StmtContext): Any? {
        checkCancel()
        return super.visitStmt(ctx)
    }

    override fun visitVar(ctx: AtorParser.VarContext ):Any? {
        val name = ctx.NAME().text
        val value = ExprVisitor.calcValue(ctx.expr(), vars)
        vars.put(name, value)
        return null
    }

    override fun visitOut(ctx: AtorParser.OutContext ):Any? {
        val value = ExprVisitor.calcValue(ctx.expr(), vars)
        out.invoke(value.getString())
        return null
    }

    companion object {
        fun checkCancel() {
            if (Thread.interrupted()) {
                throw CancelException()
            }
        }

    }
}