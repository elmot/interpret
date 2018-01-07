package xyz.elmot.interpret

import org.junit.Test


class ParallelTest : BaseTest() {
    @Test
    fun testParallel1() {
        doTest("out reduce(map({1,1000}, i-> i*2),1, x y-> x+y)", "1001001")
    }

    @Test
    fun testParallel2() {
        doTest("out reduce(map({1,1000}, i-> i*2),100000, x y -> x - y)", "-901000")
    }

    @Test
    fun testParallel3() {
        doTest("out reduce(map({1,1000}, i-> i*2),1e-2868, x y-> x*y)", "4.31161412594705058816552")
    }

    @Test
    fun testParallel4() {
        doTest("out reduce(map({1,1000}, i-> i*2),1e2869, x y-> x/y)", "2.31931701397408550742578")
    }


}
