package xyz.elmot.interpret;

import org.junit.Test;

public class SeqReuseTest extends BaseTest{
    @Test
    public void testSeqReuse() {
        doTest("var n = 5\n" +
                        "var sequence = map({0, n}, i -> (-1)^i / (2.0 * i + 1))\n" +
                        "out sequence\n" +
                        "…\n" +
                        "var pi = 4 * reduce(sequence, 0, x y -> x + y)\n" +
                        "print \"pi = \"\n" +
                        "out pi ",
                "[1,-0.333333333333333333333333,0.2,-0.142857142857142857142857,0.111111111111111111111111,-0.0909090909090909090909091]" +
                        "pi = 2.97604617604617604617605");
    }
}
