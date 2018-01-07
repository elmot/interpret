package xyz.elmot.interpret.eval;

import org.antlr.v4.runtime.ParserRuleContext;

import java.math.BigDecimal;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Value {
    Value ZERO = new Num(BigDecimal.ZERO);

    default BigDecimal getNumber(ParserRuleContext ctx) {
        throw new EvalException("Not a number", ctx);
    }

    default Stream<BigDecimal> getSeq(ParserRuleContext ctx) {
        throw new EvalException("Not a sequence", ctx);
    }

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
    }
}
