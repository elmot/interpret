package xyz.elmot.interpret;

import org.junit.Test;


public class ParallelTest extends BaseTest {
    @Test
    public void testParallelAdd() {
        doTest("out reduce(map({1,1000}, i-> i*2),0, x y-> x+y)", "1001000");
    }

    @Test
    public void testParallelMul() {
        doTest("out reduce(map({1,1000}, i-> i*2),1, x y-> x*y)", "4.31161412594705058816563E+2868");
    }

}
