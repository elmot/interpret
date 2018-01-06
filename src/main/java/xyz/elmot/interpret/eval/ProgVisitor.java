package xyz.elmot.interpret.eval;

import xyz.elmot.interpret.AtorBaseVisitor;
import xyz.elmot.interpret.AtorParser;

import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProgVisitor extends AtorBaseVisitor<Void> {
    public ProgVisitor(PrintWriter out) {
        this.out = out;
    }

    private final PrintWriter out;
    private Map<String, Value> vars = new LinkedHashMap<>();

    @Override
    public Void visitPrint(AtorParser.PrintContext ctx) {
        String text = ctx.TEXT().getText();
        out.print(text.substring(1, text.length() - 1));
        return null;
    }

    @Override
    public Void visitVar(AtorParser.VarContext ctx) {
        String name = ctx.NAME().getText();
        ExprVisitor exprVisitor = new ExprVisitor(vars);
        ctx.expr().accept(exprVisitor);
        vars.put(name, exprVisitor.getResult(ctx));
        return null;
    }

    @Override
    public Void visitOut(AtorParser.OutContext ctx) {
        ExprVisitor exprVisitor = new ExprVisitor(vars);
        ctx.expr().accept(exprVisitor);
        out.print(exprVisitor.getResult(ctx).getString());
        return null;
    }
}
