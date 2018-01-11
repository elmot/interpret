package xyz.elmot.interpret;

import org.junit.Test;
import xyz.elmot.interpret.ui.AtorGui;


public class BasicTest extends BaseTest{
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
        doTest("out reduce({1,3}, 1, x y -> x * y)", "6");
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
    public void visibilityTest1() {
        doTest("var i = 1000\n var t = map({1,2}, i ->i+1)\nout i\n out t", "1000[2,3]");
    }

    @Test
    public void visibilityTest2() {
        doTestException("var i = 1000\nout i\n out tt", "tt is not defined", 2, 5, 2);
    }

    @Test
    public void testPi() {
        doTest(AtorGui.PI_EXAMPLE, "pi = 3.14358865958578723458865");
    }

}
