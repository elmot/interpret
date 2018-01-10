package xyz.elmot.interpret;

import org.junit.Test;


public class ParallelTest extends BaseTest{
    @Test
    public void testParallel1() {
        doTest("out reduce(map({1,1000}, i-> i*2),0, x y-> x+y)","1001000");
    }

    @Test
    public void testParallel2() {
        doTest("out reduce(map({1,1000}, i-> i*2),100000, x y -> x - y)","-901000");
    }

    @Test
    public void testParallel3() {
        doTest("out reduce(map({1,1000}, i-> i*2),1e-2868, x y-> x*y)","4.31161412594705058816552");
    }

    @Test
    public void testParallel4() {
        doTest("out reduce(map({1,1000}, i-> i*2),1e2869, x y-> x/y)","2.31931701397408550742578");
    }


}
