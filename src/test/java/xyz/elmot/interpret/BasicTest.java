package xyz.elmot.interpret;

import org.junit.Test;
import xyz.elmot.interpret.eval.ErrorInfo;
import xyz.elmot.interpret.ui.AtorGui;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void powTest3() {
        doTestException("out 1^99999999999999999999999999999", "Invalid operation");
    }

    @Test
    public void powTest4() {
        doTestException("out -2.5^2.5", "NaN");
    }

    @Test
    public void powTest5() {
        doTest("out 10^(-2)", "0.01");
    }

    @Test
    public void visibilityTest1() {
        doTest("var i = 1000\n var t = map({1,2}, i ->i+1)\nout i\n out t", "1000[2,3]");
    }

    @Test
    public void visibilityTest2() {
        doTestException("var i = 1000\nout i\n out t", "t is not defined");
    }

    @Test
    public void testPi() {
        doTest(AtorGui.PI_EXAMPLE, "pi = 3.14358865958578723458865");
    }

    @Test
    public void testTypeCastFailure1() {
        doTestException("out {1,2}*1", "Not a number");
    }

    @Test
    public void testTypeCastFailure2() {
        doTestException("out 1+{1,2}", "Not a number");
    }

    @Test
    public void testTypeCastFailure3() {
        doTestException("out map(1,i->i*2)", "Not a sequence");
    }

    @Test
    public void testTypeCastFailure4() {
        doTestException("out reduce(1,1,i j->i*j)", "Not a sequence");
    }

    @Test
    public void testTypeCastFailure5() {
        doTestException("out reduce({1,2},{1,2},i j->i*j)", "Not a number");
    }

    @Test
    public void testTypeCastFailure6() {
        doTestException("out reduce({10,20},1,i j->{i,j})", "Not a number");
    }

    @Test
    public void testTypeCast1() {
        doTest("out 1+{1,1}", "2");
    }

    private void doTest(String input, String expectedOutput) {
        StringBuilder stringBuilder = new StringBuilder();
        assertTrue(Ator.runScript(input, stringBuilder::append).isEmpty());
        assertEquals(expectedOutput, stringBuilder.toString());
    }

    private void doTestException(String input, String firstMessage) {
        List<ErrorInfo> errors = Ator.runScript(input, s -> {
        });
        assertEquals(firstMessage, errors.get(0).getMsg());
    }

}
