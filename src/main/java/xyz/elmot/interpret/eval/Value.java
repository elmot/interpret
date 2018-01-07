package xyz.elmot.interpret.eval;

import org.antlr.v4.runtime.ParserRuleContext;

import java.math.BigDecimal;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Value {
    default BigDecimal getNumber(ParserRuleContext ctx) {
        throw new EvalException("Not a number", ctx);
    }

    default Stream<BigDecimal> getSeq(ParserRuleContext ctx) {
        throw new EvalException("Not a sequence", ctx);
    }

    Value.Num negate(ParserRuleContext ctx);

    String getString();


    class Seq implements Value {
        private final Stream<BigDecimal> value;

        @SuppressWarnings("WeakerAccess")
        public Seq(Stream<BigDecimal> value) {
            this.value = value;
        }

        @Override
        public String getString() {
            return value.map(Object::toString)
                    .collect(Collectors.joining(",", "[", "]"));
        }

        @Override
        public Stream<BigDecimal> getSeq(ParserRuleContext ctx) {
            return value;
        }

        @Override
        public BigDecimal getNumber(ParserRuleContext ctx) {
            //noinspection ConstantConditions
            return value.reduce((a, b) -> {
                throw new EvalException("Not a number", ctx);
            }).orElseThrow(()->new EvalException("Empty sequence is not a number",ctx));
        }

        @Override
        public Num negate(ParserRuleContext ctx) {
            Number number = value.findFirst().orElseThrow(() -> new EvalException("Empty sequence instead of number", ctx));
            value.findFirst().ifPresent(n -> new EvalException("Not a number", ctx));
            return new Num((BigDecimal) number);
        }
    }

    class Num implements Value {
        private BigDecimal value;

        @SuppressWarnings("WeakerAccess")
        public Num(BigDecimal value) {
            this.value = value;
        }

        @Override
        public BigDecimal getNumber(ParserRuleContext ctx) {
            return value;
        }

        @Override
        public String getString() {
            return value.toString();
        }

        @Override
        public Num negate(ParserRuleContext ctx) {
            value = value.negate();
            return this;
        }
    }
}
