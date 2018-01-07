package xyz.elmot.interpret.eval;

import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Exception thrown because of an error
 */
public class EvalException extends RuntimeException{
    private final ParserRuleContext ctx;

    @SuppressWarnings("WeakerAccess")
    public EvalException(String message, ParserRuleContext ctx) {
        super(message);
        this.ctx = ctx;
    }

    public ParserRuleContext getContext() {
        return ctx;
    }
}
