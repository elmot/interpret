package xyz.elmot.interpret.eval;

import org.antlr.v4.runtime.ParserRuleContext;
import xyz.elmot.interpret.AtorBaseVisitor;
import xyz.elmot.interpret.AtorParser;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * The expression calculation visitor. Inherited from automatically-generated ANTLR4 stub.
 * Supports two types of values - {@link BigDecimal} or {@link Stream} of {@link BigDecimal}s
 */
public class ExprVisitor extends AtorBaseVisitor<Void> {

    @SuppressWarnings("WeakerAccess")
    public static final int MATH_PRECISION = 24;
    @SuppressWarnings("WeakerAccess")
    public static final MathContext MATH_CONTEXT = new MathContext(MATH_PRECISION);

    /**
     * Global script variables
     */
    private final Map<String, Value> vars;

    /**
     * Temporary stack of values, collected during operand-operation sequence execution
     */
    private Deque<Value> valueStack = new ArrayDeque<>();
    /**
     * Temporary stack of operations, collected during operand-operation sequence execution
     */
    private Deque<Op> opStack = new ArrayDeque<>();

    private boolean unaryMinus;

    @SuppressWarnings("WeakerAccess")
    public ExprVisitor(Map<String, Value> vars) {
        this.vars = vars;
    }

    /**
     * Name encountered
     */
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

    /**
     * Number literal encountered
     */
    @Override
    public Void visitNumber(AtorParser.NumberContext ctx) {
        BigDecimal v = new BigDecimal(ctx.NUMBER().getText());
        valueStack.push(new Value.Num(v));
        return null;
    }


    /**
     * Expression in braces encountered => perform sub- expression calculation
     */
    @Override
    public Void visitBraces(AtorParser.BracesContext ctx) {
        valueStack.push(calcValue(ctx.expr(), vars));
        return null;
    }

    /**
     * Map encountered
     */
    @Override
    public Void visitMap(AtorParser.MapContext ctx) {
        Stream<BigDecimal> seq = calcValueSeq(ctx.expr(0), vars);
        String varName = ctx.NAME().getText();
        AtorParser.ExprContext lambda = ctx.expr(1);
        Stream<BigDecimal> valueStream = seq.map(n -> {
            ProgVisitor.checkCancel();
            Map<String, Value> localVars = new HashMap<>(vars);
            localVars.put(varName, new Value.Num(n));
            return calcValueNum(lambda, localVars);
        });
        valueStack.push(new Value.Seq(valueStream));
        return null;
    }

    /**
     * Generic expression encountered => check unary minus
     */
    @Override
    public Void visitExpr(AtorParser.ExprContext ctx) {
        if (ctx.MINUS() != null) {
            unaryMinus = true;
        }
        return super.visitExpr(ctx);
    }

    /**
     * Operand encountered => use and reset unary minus flag (if set)
     */
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
        Op op = Op.create(opText, ctx);
        resolve(op.getPriority(), ctx);
        opStack.push(op);
        return null;
    }

    /**
     * Reduce encountered
     */
    @Override
    public Void visitReduce(AtorParser.ReduceContext ctx) {
        Stream<BigDecimal> seq = calcValueSeq(ctx.expr(0), vars);
        String varNameA = ctx.NAME(0).getText();
        String varNameB = ctx.NAME(1).getText();
        AtorParser.ExprContext lambda = ctx.expr(2);
        Map<String, Value> localVars = new HashMap<>(vars);
        BigDecimal res = seq.reduce(calcValueNum(ctx.expr(1), vars), (a, b) -> {
            localVars.put(varNameA, new Value.Num(a));
            localVars.put(varNameB, new Value.Num(b));
            return calcValueNum(lambda, localVars);
        });
        valueStack.push(new Value.Num(res));
        return null;
    }

    /**
     * {@code {a,b}} expression encountered
     */
    @Override
    public Void visitSeq(AtorParser.SeqContext ctx) {
        long a = calcValueNum(ctx.expr(0), vars).longValue();
        long b = calcValueNum(ctx.expr(1), vars).longValue();

        valueStack.push(new Value.Seq(LongStream.rangeClosed(a, b).mapToObj(BigDecimal::new)));
        return null;
    }

    /**
     * Performs final computations
     *
     * @param ctx diagnostics context
     * @return result of the expression
     */
    @SuppressWarnings("WeakerAccess")
    public Value getResult(ParserRuleContext ctx) {
        resolve(0, ctx);
        if (valueStack.size() != 1 || !opStack.isEmpty()) {
            throw new EvalException("Internal calc error", ctx);
        }
        return valueStack.peek();
    }

    /**
     * Performs full or partial computations over values accumulated in {@link #opStack} and {@link #valueStack}.
     * The method scans accumulated data backward taking priorities in account.
     *
     * @param downToPriority minimal operation priority to be computed
     * @param ctx            context for diagnostics
     * @see Op#priority
     */
    private void resolve(int downToPriority, ParserRuleContext ctx) {
        while (!opStack.isEmpty() && opStack.peek().getPriority()>= downToPriority) {
            Op op = opStack.pop();
            BigDecimal b = valueStack.pop().getNumber(ctx);
            BigDecimal a = valueStack.pop().getNumber(ctx);
            try {
                valueStack.push(new Value.Num(op.getOperation().apply(a, b)));
            } catch (ArithmeticException e) {
                throw new EvalException(e.getMessage(), op.getContext());
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
    private BigDecimal calcValueNum(AtorParser.ExprContext exprContext, Map<String, Value> vars) {
        return calcValue(exprContext, vars).getNumber(exprContext);
    }

    /**
     * Calculate expression value of type sequence
     *
     * @param exprContext the parsed expression
     * @param vars        variables map
     * @return the result
     * @throws EvalException if something goes wrong or the result is not a sequence
     */
    private Stream<BigDecimal> calcValueSeq(AtorParser.ExprContext exprContext, Map<String, Value> vars) {
        return calcValue(exprContext, vars).getSeq(exprContext);
    }

    /**
     * Calculate a generic expression value
     *
     * @param exprContext the parsed expression
     * @param vars        variables map
     * @return the result
     * @throws EvalException if something goes wrong
     */
    public static Value calcValue(AtorParser.ExprContext exprContext, Map<String, Value> vars) {
        ProgVisitor.checkCancel();
        ExprVisitor exprVisitor = new ExprVisitor(vars);
        exprContext.accept(exprVisitor);
        return exprVisitor.getResult(exprContext);
    }

}
