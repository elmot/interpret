package xyz.elmot.interpret

import org.junit.Test
import xyz.elmot.interpret.ui.AtorGui
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BasicTest {
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
    operator fun unaryMinus() {
        doTest("out - 2+(-10)+(-(-100))+1000", "1088")
    }

    @Test
    fun powTest1() {
        doTest("out 2.5^2", "6.25")
    }

    @Test
    fun powTest2() {
        doTest("out 2.5^2.5", "9.88211768802618628626533")
    }

    @Test
    fun powTest3() {
        doTestException("out 1^99999999999999999999999999999", "Invalid operation")
    }

    @Test
    fun powTest4() {
        doTestException("out -2.5^2.5", "NaN")
    }

    @Test
    fun powTest5() {
        doTest("out 10^(-2)", "0.01")
    }

    @Test
    fun visibilityTest1() {
        doTest("var i = 1000\n var t = map({1,2}, i ->i+1)\nout i\n out t", "1000[2,3]")
    }

    @Test
    fun visibilityTest2() {
        doTestException("var i = 1000\nout i\n out t", "t is not defined")
    }

    @Test
    fun testPi() {
        doTest(AtorGui.PI_EXAMPLE, "pi = 3.14358865958578723458865")
    }

    @Test
    fun testTypeCastFailure1() {
        doTestException("out {1,2}*1", "Not a number")
    }

    @Test
    fun testTypeCastFailure2() {
        doTestException("out 1+{1,2}", "Not a number")
    }

    @Test
    fun testTypeCastFailure3() {
        doTestException("out map(1,i->i*2)", "Not a sequence")
    }

    @Test
    fun testTypeCastFailure4() {
        doTestException("out reduce(1,1,i j->i*j)", "Not a sequence")
    }

    @Test
    fun testTypeCastFailure5() {
        doTestException("out reduce({1,2},{1,2},i j->i*j)", "Not a number")
    }

    @Test
    fun testTypeCastFailure6() {
        doTestException("out reduce({10,20},1,i j->{i,j})", "Not a number")
    }

    @Test
    fun testTypeCast1() {
        doTest("out 1+{1,1}", "2")
    }

    private fun doTest(input: String, expectedOutput: String) {
        val stringBuilder = StringBuilder()
        assertTrue(Ator.runScript(input, { stringBuilder.append(it) }).isEmpty())
        assertEquals(expectedOutput, stringBuilder.toString())
    }

    private fun doTestException(input: String, firstMessage: String) {
        val errors = Ator.runScript(input) { _ -> }
        assertEquals(firstMessage, errors[0].msg)
    }
}