package xyz.elmot.interpret.eval;

import org.antlr.v4.runtime.ParserRuleContext;
import xyz.elmot.interpret.AtorBaseVisitor;
import xyz.elmot.interpret.AtorParser;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class ExprVisitor extends AtorBaseVisitor<Void> {

    private final Map<String, Value> vars;
    private Deque<Value> valueStack = new ArrayDeque<>();
    private Deque<Op> opStack = new ArrayDeque<>();
    private boolean unaryMinus;
    public static final int MATH_PRECISION = 24;
    private static final MathContext MATH_CONTEXT = new MathContext(MATH_PRECISION);

    @SuppressWarnings("WeakerAccess")
    public ExprVisitor(Map<String, Value> vars) {
        this.vars = vars;
    }

    @Override
    public Void visitName(AtorParser.NameContext ctx) {
        String name = ctx.NAME().getText();
        Value value = vars.get(name);
        if (value == null) {
            throw new EvalException(name + " is not defined", ctx);
        } else {
            valueStack.push(value);
        }
        return null;
    }

    @Override
    public Void visitNumber(AtorParser.NumberContext ctx) {
        BigDecimal v = new BigDecimal(ctx.NUMBER().getText());
        valueStack.push(new Value.Num(v));
        return null;
    }

    public Value getResult(ParserRuleContext ctx) {
        resolve(0, ctx);
        if (valueStack.size() != 1 || !opStack.isEmpty()) {
            throw new RuntimeException("Internal calc error");
        }
        return valueStack.peek();
    }

    @Override
    public Void visitBraces(AtorParser.BracesContext ctx) {
        valueStack.push(calcValue(ctx.expr(), vars));
        return null;
    }

    @Override
    public Void visitMap(AtorParser.MapContext ctx) {
        Stream<BigDecimal> seq = calcValueSeq(ctx.expr(0), vars);
        String varName = ctx.NAME().getText();
        AtorParser.ExprContext lambda = ctx.expr(1);
        Stream<BigDecimal> valueStream = seq.map(n -> {
            Map<String, Value> localVars = new HashMap<>(vars);
            localVars.put(varName, new Value.Num(n));
            return calcValueNum(lambda, localVars);
        });
        valueStack.push(new Value.Seq(valueStream));
        return null;
    }

    @Override
    public Void visitExpr(AtorParser.ExprContext ctx) {
        if (ctx.MINUS() != null) {
            unaryMinus = true;
        }
        return super.visitExpr(ctx);
    }

    @Override
    public Void visitOperand(AtorParser.OperandContext ctx) {
        super.visitOperand(ctx);
        if (unaryMinus) {
            unaryMinus = false;
            Value.Num negated = valueStack.removeFirst().negate(ctx);
            valueStack.addFirst(negated);
        }
        return null;
    }

    @Override
    public Void visitOp(AtorParser.OpContext ctx) {
        String opText = ctx.OP().getText();
        Op op;
        switch (opText) {
            case "+":
                op = new Op((a, b) -> a.add(b, MATH_CONTEXT), 1);
                break;
            case "-":
                op = new Op((a, b) -> a.subtract(b, MATH_CONTEXT), 2);
                break;
            case "*":
                op = new Op((a, b) -> a.multiply(b, MATH_CONTEXT), 3);
                break;
            case "/":
                op = new Op((a, b) -> a.divide(b, MATH_CONTEXT), 4);
                break;
            case "^":
                op = new Op(
                        (a, b) -> {
                            try {
                                if (b.scale() > 0) {
                                    double pow = Math.pow(a.doubleValue(), b.doubleValue());
                                    if (Double.isFinite(pow)) {
                                        return new BigDecimal(pow, MATH_CONTEXT);
                                    } else {
                                        throw new EvalException("" + pow, ctx);
                                    }
                                } else {
                                    return a.pow(b.intValue(), MATH_CONTEXT);
                                }
                            } catch (ArithmeticException e) {
                                throw new EvalException(e.getMessage(), ctx);
                            }
                        }
                        , 5);
                break;
            default:
                throw new EvalException("Unknown operation " + opText, ctx);
        }
        resolve(op.priority, ctx);
        opStack.push(op);

        return null;
    }

    @Override
    public Void visitReduce(AtorParser.ReduceContext ctx) {
        Stream<BigDecimal> seq = calcValueSeq(ctx.expr(0), vars);
        BigDecimal value[] = new BigDecimal[]{calcValueNum(ctx.expr(1), vars)};//todo make ok
        String varNameA = ctx.NAME(0).getText();
        String varNameB = ctx.NAME(1).getText();
        AtorParser.ExprContext lambda = ctx.expr(2);
        Map<String, Value> localVars = new HashMap<>(vars);
        seq.forEachOrdered(n -> {
            localVars.put(varNameA, new Value.Num(value[0]));
            localVars.put(varNameB, new Value.Num(n));
            value[0] = calcValueNum(lambda, localVars);

        });
        valueStack.push(new Value.Num(value[0]));
        return null;
    }

    private void resolve(int downToPriority, ParserRuleContext ctx) {
        while (!opStack.isEmpty() && opStack.peek().priority >= downToPriority) {
            Op op = opStack.pop();
            BigDecimal b = valueStack.pop().getNumber(ctx);
            BigDecimal a = valueStack.pop().getNumber(ctx);
            valueStack.push(new Value.Num(op.operation.apply(a, b)));
        }
    }

    @Override
    public Void visitSeq(AtorParser.SeqContext ctx) {
        long a = calcValueNum(ctx.expr(0), vars).longValue();
        long b = calcValueNum(ctx.expr(1), vars).longValue();

        valueStack.push(new Value.Seq(LongStream.rangeClosed(a, b).mapToObj(BigDecimal::new)));
        return null;
    }

    private BigDecimal calcValueNum(AtorParser.ExprContext exprContext, Map<String, Value> vars) {
        return calcValue(exprContext, vars).getNumber(exprContext);
    }

    private Stream<BigDecimal> calcValueSeq(AtorParser.ExprContext exprContext, Map<String, Value> vars) {
        return calcValue(exprContext, vars).getSeq(exprContext);
    }

    private Value calcValue(AtorParser.ExprContext exprContext, Map<String, Value> vars) {
        ExprVisitor exprVisitor = new ExprVisitor(vars);
        exprContext.accept(exprVisitor);
        return exprVisitor.getResult(exprContext);
    }

    private static class Op {
        final BiFunction<BigDecimal, BigDecimal, BigDecimal> operation;
        final int priority;

        Op(BiFunction<BigDecimal, BigDecimal, BigDecimal> operation, int priority) {
            this.operation = operation;
            this.priority = priority;
        }
    }
}
