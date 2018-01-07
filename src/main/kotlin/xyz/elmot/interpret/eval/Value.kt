package xyz.elmot.interpret.eval

import org.antlr.v4.runtime.ParserRuleContext
import java.math.BigDecimal
import java.util.stream.Collectors
import java.util.stream.Stream

interface Value {
    companion object {
        val ZERO: Value = Num(BigDecimal.ZERO)
    }

    fun getNumber(ctx: ParserRuleContext): BigDecimal {
        throw EvalException("Not a number", ctx)
    }

    fun getSeq(ctx: ParserRuleContext): Stream<BigDecimal> {
        throw EvalException("Not a sequence", ctx)
    }

    fun getString(): String

    class Num(private var value: BigDecimal) : Value {

        override fun getNumber(ctx: ParserRuleContext): BigDecimal {
            return value
        }

        override fun getString(): String {
            return value.toString()
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

    }

}