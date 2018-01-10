package xyz.elmot.interpret

import org.junit.Test


class MinusTest : BaseTest() {
    @Test
    fun minusTest() {
        doTest("var a = 10\nvar b = 7.3\nout a-b", "2.7")
    }

    @Test
    fun unaryFailTest() {
        doTestException("out +1", "Operator + is not allowed here", 0, 4, 1)
    }

    @Test
    fun unaryMinus() {
        doTest("out - 2+(-10)+(-(-100))+1000", "1088")
    }

    @Test
    fun plusUnaryMinus() {
        doTest("out 10+ -1", "9")
    }

}
