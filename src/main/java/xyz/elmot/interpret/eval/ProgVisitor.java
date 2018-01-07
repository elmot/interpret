package xyz.elmot.interpret.eval;

import xyz.elmot.interpret.AtorBaseVisitor;
import xyz.elmot.interpret.AtorParser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ProgVisitor extends AtorBaseVisitor<Void> {

    public ProgVisitor(Consumer<String> out) {
        this.out = out;
    }

    private final Consumer<String> out;
    private Map<String, Value> vars = new ConcurrentHashMap<>();

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

    @Override
    public Void visitVar(AtorParser.VarContext ctx) {
        String name = ctx.NAME().getText();
        Value value = ExprVisitor.calcValue(ctx.expr(), vars);
        vars.put(name, value);
        return null;
    }

    @Override
    public Void visitOut(AtorParser.OutContext ctx) {
        Value value = ExprVisitor.calcValue(ctx.expr(), vars);
        out.accept(value.getString());
        return null;
    }

    public static void checkCancel() {
        if (Thread.interrupted()) {
            throw new CancelException();
        }
    }

}
