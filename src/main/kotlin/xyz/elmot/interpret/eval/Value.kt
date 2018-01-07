package xyz.elmot.interpret.eval

import org.antlr.v4.runtime.ParserRuleContext
import java.math.BigDecimal
import java.util.stream.Collectors
import java.util.stream.Stream

interface Value {
    fun getNumber(ctx: ParserRuleContext): BigDecimal {
        throw EvalException("Not a number", ctx)
    }

    fun getSeq(ctx: ParserRuleContext): Stream<BigDecimal> {
        throw EvalException("Not a sequence", ctx)
    }

    fun negate(ctx: ParserRuleContext): Value.Num

    fun getString(): String

    class Num(private var value: BigDecimal) : Value {

        override fun getNumber(ctx: ParserRuleContext): BigDecimal {
            return value
        }

        override fun getString(): String {
            return value.toString()
        }

        override fun negate(ctx: ParserRuleContext): Num {
            value = value.negate()
            return this
        }
    }

    class Seq(private val value: Stream<BigDecimal>) : Value {

        override fun getString(): String {
            return value.map(java.lang.String::valueOf)
                    .collect(Collectors.joining(",", "[", "]"))
        }

        override fun getSeq(ctx: ParserRuleContext): Stream<BigDecimal> {
            return value
        }

        override fun getNumber(ctx: ParserRuleContext): BigDecimal {
            return value.reduce({ _, _ -> throw EvalException("Not a number", ctx) })
                    .orElseThrow({ EvalException("Empty sequence is not a number", ctx) })
        }

        override fun negate(ctx :ParserRuleContext ):Num
        {
            val number:Number  = value.findFirst ().orElseThrow({EvalException("Empty sequence instead of number", ctx)})
            value.findFirst().ifPresent({_ -> throw EvalException("Not a number", ctx)})
            return Num (number as BigDecimal)
        }
    }

}