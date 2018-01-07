package xyz.elmot.interpret.eval;

import org.antlr.v4.runtime.ParserRuleContext;
import xyz.elmot.interpret.AtorParser;

import java.math.BigDecimal;
import java.util.function.BiFunction;

/**
 * Arithmetic binary operation over {@link BigDecimal} with priority and precision
 * defined in {@link ExprVisitor#MATH_CONTEXT}
 */
class Op {
    private final BiFunction<BigDecimal, BigDecimal, BigDecimal> operation;
    private final int priority;
    private final ParserRuleContext context;

    private Op(BiFunction<BigDecimal, BigDecimal, BigDecimal> operation, int priority, ParserRuleContext context) {
        this.operation = operation;
        this.priority = priority;
        this.context = context;
    }

    /**
     * Pow is tricky - we can raise any base to int positive power
     * or raise positive base to any power.
     * Also the result might be too big.
     * @param base the base
     * @param power the exponent
     * @return result
     * @throws ArithmeticException if not computable
     */
    private static BigDecimal pow(BigDecimal base, BigDecimal power) {
        if (power.scale() > 0) {
            double pow = Math.pow(base.doubleValue(), power.doubleValue());
            if (Double.isFinite(pow)) {
                return new BigDecimal(pow, ExprVisitor.MATH_CONTEXT);
            } else {
                throw new ArithmeticException("" + pow);
            }
        } else {
            return base.pow(power.intValue(), ExprVisitor.MATH_CONTEXT);
        }

    }

    @SuppressWarnings("WeakerAccess")
    public static Op create(String opText, AtorParser.OpContext ctx) {
        Op op;
        switch (opText) {
            case "+":
                op = new Op((a, b) -> a.add(b, ExprVisitor.MATH_CONTEXT), 1, ctx);
                break;
            case "-":
                op = minus(ctx);
                break;
            case "*":
                op = new Op((a, b) -> a.multiply(b, ExprVisitor.MATH_CONTEXT), 3, ctx);
                break;
            case "/":
                op = new Op((a, b) -> a.divide(b, ExprVisitor.MATH_CONTEXT), 4, ctx);
                break;
            case "^":
                op = new Op(Op::pow, 5, ctx);
                break;
            default:
                throw new EvalException("Unknown operation " + opText, ctx);
        }
        return op;
    }

    public BiFunction<BigDecimal, BigDecimal, BigDecimal> getOperation() {
        return operation;
    }

    public int getPriority() {
        return priority;
    }

    public ParserRuleContext getContext() {
        return context;
    }

    public static Op minus(AtorParser.OpContext ctx) {
        return new Op((a, b) -> a.subtract(b, ExprVisitor.MATH_CONTEXT), 2, ctx);
    }
}
