package xyz.elmot.interpret;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Assert;
import org.junit.Test;
import xyz.elmot.interpret.eval.EvalException;
import xyz.elmot.interpret.eval.ProgVisitor;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

public class BasicTest {
    @Test
    public void basicTest() {
        doTest("var a = 1.0\nprint\"a=\"\n out a+2", "a=3.0");
    }

    @Test
    public void bracesTest() {
        doTest("out 3*(2+2)", "12");
    }

    @Test
    public void seqBaseTest() {
        doTest("out {1,3}", "[1,2,3]");
    }

    @Test
    public void mapBaseTest() {
        doTest("out map({1,3}, i-> i * i)", "[1,4,9]");
    }

    @Test
    public void reduceBaseTest() {
        doTest("out reduce({1,3}, 100, x y -> x * y)", "600");
    }

    @Test
    public void priorityTest1() {
        doTest("out 2+2*2^2", "10");
    }

    @Test
    public void priorityTest2() {
        doTest("out 2^2*2+2", "10");
    }

    @Test
    public void priorityTest3() {
        doTest("out 2^2*(2+2)", "16");
    }

    @Test
    public void priorityTest4() {
        doTest("out 2+2^(2*2)", "18");
    }

    @Test
    public void priorityTest5() {
        doTest("out 2+2*2^2 + 3+4*3^3", "121");
    }

    @Test
    public void unaryMinus() {
        doTest("out - 2+(-10)+(-(-100))+1000", "1088");
    }

    @Test
    public void powTest1() {
        doTest("out 2.5^2", "6.25");
    }

    @Test
    public void powTest2() {
        doTest("out 2.5^2.5", "9.88211768802618628626533");
    }

    @Test(expected = EvalException.class)
    public void powTest3() {
        doTest("out 1^99999999999999999999999999999", "");
    }

    @Test(expected = EvalException.class)
    public void powTest4() {
        doTest("out -2.5^2.5", "");
    }

    @Test
    public void powTest5() {
        doTest("out 10^(-2)", "0.01");
    }

    @Test
    public void visibilityTest1() {
        doTest("var i = 1000\n var t = map({1,2}, i ->i+1)\nout i\n out t", "1000[2,3]");
    }

    @Test(expected = EvalException.class)
    public void visibilityTest2() {
        doTest("var i = 1000\nout i\n out t", "");
    }

    @Test
    public void testPi() {
        doTest("var n = 500\n" +
                "var sequence = map({0, n}, i -> (-1)^i / (2.0 * i + 1))\n" +
                "var pi = 4 * reduce(sequence, 0, x y -> x + y)\n" +
                "print \"pi = \"\n" +
                "out pi\n", "pi = 3.14358865958578723458865");
    }

    private void doTest(String input, String expectedOutput) {
        CodePointCharStream stream = CharStreams.fromString(input);
        AtorLexer lexer = new AtorLexer(stream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        AtorParser parser = new AtorParser(tokenStream);
        AtorParser.ProgramContext program = parser.program();
        StringBuilder stringBuilder = new StringBuilder();
        AtorBaseVisitor<Void> visitor = new ProgVisitor(stringBuilder::append);
        program.accept(visitor);
        Assert.assertEquals(expectedOutput, stringBuilder.toString());
    }

}
