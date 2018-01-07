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
    operator fun unaryMinus() {
        doTest("out - 2+(-10)+(-(-100))+1000", "1088")
    }

}
