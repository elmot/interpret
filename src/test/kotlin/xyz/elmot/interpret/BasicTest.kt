package xyz.elmot.interpret

import org.junit.Test
import xyz.elmot.interpret.ui.AtorGui

class BasicTest: BaseTest() {

    @Test
    fun basicTest() {
        doTest("var a = 1.0\nprint\"a=\"\n out a+2", "a=3.0")
    }

    @Test
    fun bracesTest() {
        doTest("out 3*(2+2)", "12")
    }

    @Test
    fun seqBaseTest() {
        doTest("out {1,3}", "[1,2,3]")
    }

    @Test
    fun mapBaseTest() {
        doTest("out map({1,3}, i-> i * i)", "[1,4,9]")
    }

    @Test
    fun reduceBaseTest() {
        doTest("out reduce({1,3}, 100, x y -> x * y)", "600")
    }

    @Test
    fun priorityTest1() {
        doTest("out 2+2*2^2", "10")
    }

    @Test
    fun priorityTest2() {
        doTest("out 2^2*2+2", "10")
    }

    @Test
    fun priorityTest3() {
        doTest("out 2^2*(2+2)", "16")
    }

    @Test
    fun priorityTest4() {
        doTest("out 2+2^(2*2)", "18")
    }

    @Test
    fun priorityTest5() {
        doTest("out 2+2*2^2 + 3+4*3^3", "121")
    }

    @Test
    fun visibilityTest1() {
        doTest("var i = 1000\n var t = map({1,2}, i ->i+1)\nout i\n out t", "1000[2,3]")
    }

    @Test
    fun visibilityTest2() {
        doTestException("var i = 1000\nout i\n out tt", "tt is not defined", 2, 5, 2)
    }

    @Test
    fun testPi() {
        doTest(AtorGui.PI_EXAMPLE, "pi = 3.14358865958578723458865")
    }
}