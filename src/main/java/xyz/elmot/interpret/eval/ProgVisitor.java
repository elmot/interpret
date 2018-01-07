package xyz.elmot.interpret.eval;

import xyz.elmot.interpret.AtorBaseVisitor;
import xyz.elmot.interpret.AtorParser;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ProgVisitor extends AtorBaseVisitor<Void> {
    public ProgVisitor(Consumer<String> out) {
        this.out = out;
    }

    private final Consumer<String> out;
    private Map<String, Value> vars = new LinkedHashMap<>();

    @Override
    public Void visitPrint(AtorParser.PrintContext ctx) {
        String text = ctx.TEXT().getText();
        out.accept(text.substring(1, text.length() - 1));
        return null;
    }

    @Override
    public Void visitStmt(AtorParser.StmtContext ctx) {
        checkCancel();
        return super.visitStmt(ctx);
    }

    @SuppressWarnings("WeakerAccess")
    public static void checkCancel() {
        if (Thread.currentThread().isInterrupted()) {
            throw new CancelException();
        }
    }

    @Override
    public Void visitVar(AtorParser.VarContext ctx) {
        String name = ctx.NAME().getText();
        ExprVisitor exprVisitor = new ExprVisitor(vars);
        AtorParser.ExprContext expr = ctx.expr();
        expr.accept(exprVisitor);
        vars.put(name, exprVisitor.getResult(expr));
        return null;
    }

    @Override
    public Void visitOut(AtorParser.OutContext ctx) {
        ExprVisitor exprVisitor = new ExprVisitor(vars);
        AtorParser.ExprContext expr = ctx.expr();
        expr.accept(exprVisitor);
        out.accept(exprVisitor.getResult(expr).getString());
        return null;
    }
}
