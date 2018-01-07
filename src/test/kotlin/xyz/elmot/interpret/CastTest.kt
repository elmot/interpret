package xyz.elmot.interpret

import org.junit.Test

class CastTest : BaseTest() {
    @Test
    fun testTypeCastFailure1() {
        doTestException("out {1,2}*1", "Not a number", 0, 9, 1)
    }

    @Test
    fun testTypeCastFailure2() {
        doTestException("out 1+{1,2}", "Not a number", 0, 5, 1)
    }

    @Test
    fun testTypeCastFailure3() {
        doTestException("out map(1,i->i*2)", "Not a sequence", 0, 8, 1)
    }

    @Test
    fun testTypeCastFailure4() {
        doTestException("out reduce(1,1,i j->i*j)", "Not a sequence", 0, 11, 1)
    }

    @Test
    fun testTypeCastFailure5() {
        doTestException("out reduce({1,2},{1,2},i j->i*j)", "Not a number", 0, 17, 5)
    }

    @Test
    fun testTypeCastFailure6() {
        doTestException("out reduce({10,20},1,i j->{i,j})", "Not a number", 0, 26, 5)
    }

    @Test
    fun testTypeCast1() {
        doTest("out 1+{1,1}", "2")
    }

    @Test
    fun testTypeCast2() {
        doTest("out {1,1} + 10", "11")
    }

}
