package xyz.elmot.interpret.eval

import org.antlr.v4.runtime.ParserRuleContext
import xyz.elmot.interpret.AtorBaseVisitor
import xyz.elmot.interpret.AtorParser
import java.math.BigDecimal
import java.math.MathContext
import java.util.*
import java.util.stream.LongStream
import java.util.stream.Stream

/**
 * The expression calculation visitor. Inherited from automatically-generated ANTLR4 stub.
 * Supports two types of values - [BigDecimal] or [Stream] of [BigDecimal]s
 */
class ExprVisitor(private val vars: Map<String, Value>) : AtorBaseVisitor<Any>() {


    /**
     * Temporary stack of values, collected during operand-operation sequence execution
     */
    private val valueStack = ArrayDeque<Value>()
    /**
     * Temporary stack of operations, collected during operand-operation sequence execution
     */
    private val opStack = ArrayDeque<Op>()

    private var unaryMinus: Boolean = false

    /**
     * Name encountered
     */
    override fun visitName(ctx: AtorParser.NameContext): Void? {
        val name = ctx.NAME().text
        val value = vars[name]
        if (value == null) {
            throw EvalException(name + " is not defined", ctx)
        } else {
            valueStack.push(value)
        }
        return null
    }

    /**
     * Number literal encountered
     */
    override fun visitNumber(ctx: AtorParser.NumberContext): Void? {
        val v = BigDecimal(ctx.NUMBER().text)
        valueStack.push(Value.Num(v))
        return null
    }


    /**
     * Expression in braces encountered => perform sub- expression calculation
     */
    override fun visitBraces(ctx: AtorParser.BracesContext): Void? {
        valueStack.push(calcValue(ctx.expr(), vars))
        return null
    }

    /**
     * Map encountered
     */
    override fun visitMap(ctx: AtorParser.MapContext): Void? {
        val seq = calcValueSeq(ctx.expr(0), vars)
        val varName = ctx.NAME().text
        val lambda = ctx.expr(1)
        val valueStream = seq.map { n ->
            ProgVisitor.checkCancel()
            val localVars = HashMap(vars)
            localVars.put(varName, Value.Num(n))
            calcValueNum(lambda, localVars)
        }
        valueStack.push(Value.Seq(valueStream))
        return null
    }

    /**
     * Generic expression encountered => check unary minus
     */
    override fun visitExpr(ctx: AtorParser.ExprContext): Any? {
        if (ctx.MINUS() != null) {
            unaryMinus = true
        }
        return super.visitExpr(ctx)
    }

    /**
     * Operand encountered => use and reset unary minus flag (if set)
     */
    override fun visitOperand(ctx: AtorParser.OperandContext): Void? {
        super.visitOperand(ctx)
        if (unaryMinus) {
            unaryMinus = false
            val negated = valueStack.removeFirst().negate(ctx)
            valueStack.addFirst(negated)
        }
        return null
    }

    override fun visitOp(ctx: AtorParser.OpContext): Void? {
        val opText = ctx.OP().text
        val op = Op.create(opText, ctx)
        resolve(op.priority, ctx)
        opStack.push(op)
        return null
    }

    /**
     * Reduce encountered
     */
    override fun visitReduce(ctx: AtorParser.ReduceContext): Void? {
        val seq = calcValueSeq(ctx.expr(0), vars)
        val varNameA = ctx.NAME(0).text
        val varNameB = ctx.NAME(1).text
        val lambda = ctx.expr(2)
        val localVars = HashMap(vars)
        val res = seq.reduce(calcValueNum(ctx.expr(1), vars)) { a, b ->
            localVars.put(varNameA, Value.Num(a))
            localVars.put(varNameB, Value.Num(b))
            calcValueNum(lambda, localVars)
        }
        valueStack.push(Value.Num(res))
        return null
    }

    /**
     * `{a,b}` expression encountered
     */
    override fun visitSeq(ctx: AtorParser.SeqContext): Void? {
        val a = calcValueNum(ctx.expr(0), vars).toLong()
        val b = calcValueNum(ctx.expr(1), vars).toLong()

        valueStack.push(Value.Seq(LongStream.rangeClosed(a, b).mapToObj({ it.toBigDecimal() })))
        return null
    }

    /**
     * Performs final computations
     *
     * @param ctx diagnostics context
     * @return result of the expression
     */
    fun getResult(ctx: ParserRuleContext): Value {
        resolve(0, ctx)
        if (valueStack.size != 1 || !opStack.isEmpty()) {
            throw EvalException("Internal calc error", ctx)
        }
        return valueStack.peek()
    }

    /**
     * Performs full or partial computations over values accumulated in [.opStack] and [.valueStack].
     * The method scans accumulated data backward taking priorities in account.
     *
     * @param downToPriority minimal operation priority to be computed
     * @param ctx            context for diagnostics
     * @see Op.priority
     */
    private fun resolve(downToPriority: Int, ctx: ParserRuleContext) {
        while (!opStack.isEmpty() && opStack.peek().priority >= downToPriority) {
            val op = opStack.pop()
            val b = valueStack.pop().getNumber(ctx)
            val a = valueStack.pop().getNumber(ctx)
            try {
                valueStack.push(Value.Num(op.operation.invoke(a, b)))
            } catch (e: ArithmeticException) {
                throw EvalException("" + e.message, op.context)
            }

        }
    }

    /**
     * Calculate expression value of numeric type
     *
     * @param exprContext the parsed expression
     * @param vars        variables map
     * @return the result
     * @throws EvalException if something goes wrong or the result is not convertible to number
     */
    private fun calcValueNum(exprContext: AtorParser.ExprContext, vars: Map<String, Value>): BigDecimal {
        return calcValue(exprContext, vars).getNumber(exprContext)
    }

    companion object {
        /**
         * Calculate expression value of type sequence
         *
         * @param exprContext the parsed expression
         * @param vars        variables map
         * @return the result
         * @throws EvalException if something goes wrong or the result is not a sequence
         */
        private fun calcValueSeq(exprContext: AtorParser.ExprContext, vars: Map<String, Value>): Stream<BigDecimal> {
            return calcValue(exprContext, vars).getSeq(exprContext)
        }


        val MATH_PRECISION = 24
        val MATH_CONTEXT = MathContext(MATH_PRECISION)

        /**
         * Calculate a generic expression value
         *
         * @param exprContext the parsed expression
         * @param vars        variables map
         * @return the result
         * @throws EvalException if something goes wrong
         */
        fun calcValue(exprContext: AtorParser.ExprContext, vars: Map<String, Value>): Value {
            ProgVisitor.checkCancel()
            val exprVisitor = xyz.elmot.interpret.eval.ExprVisitor(vars)
            exprContext.accept(exprVisitor)
            return exprVisitor.getResult(exprContext)
        }
    }
}