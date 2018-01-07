package xyz.elmot.interpret.eval;

import xyz.elmot.interpret.AtorParser;

import java.math.BigDecimal;
import java.util.function.BiFunction;

class Op {
    final BiFunction<BigDecimal, BigDecimal, BigDecimal> operation;
    final int priority;

    private Op(BiFunction<BigDecimal, BigDecimal, BigDecimal> operation, int priority) {
        this.operation = operation;
        this.priority = priority;
    }

    private static BigDecimal pow(BigDecimal a, BigDecimal b) {
        if (b.scale() > 0) {
            double pow = Math.pow(a.doubleValue(), b.doubleValue());
            if (Double.isFinite(pow)) {
                return new BigDecimal(pow, ExprVisitor.MATH_CONTEXT);
            } else {
                throw new ArithmeticException("" + pow);
            }
        } else {
            return a.pow(b.intValue(), ExprVisitor.MATH_CONTEXT);
        }

    }

    @SuppressWarnings("WeakerAccess")
    public static Op create(String opText, AtorParser.OpContext ctx) {
        Op op;
        switch (opText) {
            case "+":
                op = new Op((a, b) -> a.add(b, ExprVisitor.MATH_CONTEXT), 1);
                break;
            case "-":
                op = new Op((a, b) -> a.subtract(b, ExprVisitor.MATH_CONTEXT), 2);
                break;
            case "*":
                op = new Op((a, b) -> a.multiply(b, ExprVisitor.MATH_CONTEXT), 3);
                break;
            case "/":
                op = new Op((a, b) -> a.divide(b, ExprVisitor.MATH_CONTEXT), 4);
                break;
            case "^":
                op = new Op(Op::pow, 5);
                break;
            default:
                throw new EvalException("Unknown operation " + opText, ctx);
        }
        return op;
    }
}
