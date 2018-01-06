package xyz.elmot.interpret.eval;

import org.antlr.v4.runtime.ParserRuleContext;

public class EvalException extends RuntimeException{
    private final ParserRuleContext ctx;

    public EvalException(String message, ParserRuleContext ctx) {
        super(message);
        this.ctx = ctx;
    }

    public ParserRuleContext getCtx() {
        return ctx;
    }
}
