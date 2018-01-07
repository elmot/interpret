package xyz.elmot.interpret

import org.junit.Test


class PowTest : BaseTest() {

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
        doTestException("out 1^99999999999999999999999999999", "Invalid operation", 0, 5, 1)
    }

    @Test
    fun powTest4() {
        doTestException("out (-2.5)^2.5", "NaN", 0, 10, 1)
    }

    @Test
    fun powTest5() {
        doTest("out 10^(-2)", "0.01")
    }

}
