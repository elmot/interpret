package xyz.elmot.interpret.eval

import org.antlr.v4.runtime.ParserRuleContext
import xyz.elmot.interpret.AtorParser
import java.math.BigDecimal
import kotlin.math.pow

/**
 * Arithmetic binary operation over {@link BigDecimal} with priority and precision
 * defined in {@link ExprVisitor#MATH_CONTEXT}
 */
class Op(val operation: (BigDecimal, BigDecimal) -> BigDecimal,
         val priority: Int,
         val context: ParserRuleContext) {

    companion object {
        /**
         * Pow is tricky - we can raise any base to int positive power
         * or raise positive base to any power.
         * Also the result might be too big.
         * @param base the base
         * @param power the exponent
         * @return result
         * @throws ArithmeticException if not computable
         */
        private fun pow(base: BigDecimal, power: BigDecimal): BigDecimal {
            return if (power.scale() > 0) {
                val pow = base.toDouble().pow(power.toDouble())
                if (pow.isFinite()) {
                    BigDecimal(pow, ExprVisitor.MATH_CONTEXT)
                } else {
                    throw ArithmeticException(pow.toString())
                }
            } else {
                base.pow(power.toInt(), ExprVisitor.MATH_CONTEXT)
            }
        }

        fun create(opText: String, ctx: AtorParser.OpContext): Op {
            val op: Op
            op = when (opText) {
                "+" ->
                    Op({ a, b -> a.add(b, ExprVisitor.MATH_CONTEXT) }, 1, ctx)
                "-" ->
                    Op({ a, b -> a.subtract(b, ExprVisitor.MATH_CONTEXT) }, 2, ctx)
                "*" ->
                    Op({ a, b -> a.multiply(b, ExprVisitor.MATH_CONTEXT) }, 3, ctx)
                "/" ->
                    Op({ a, b -> a.divide(b, ExprVisitor.MATH_CONTEXT) }, 4, ctx)
                "^" ->
                    Op(Op.Companion::pow, 5, ctx)
                else ->
                    throw EvalException("Unknown operation " + opText, ctx)
            }
            return op
        }


    }
}