package xyz.elmot.interpret;

import org.junit.Test;


public class CastTest extends BaseTest{
    @Test
    public void testTypeCastFailure1() {
        doTestException("out {1,2}*1", "Not a number", 0, 4, 1);
    }

    @Test
    public void testTypeCastFailure2() {
        doTestException("out 1+{1,2}", "Not a number", 0, 4, 1);
    }

    @Test
    public void testTypeCastFailure3() {
        doTestException("out map(1,i->i*2)", "Not a sequence", 0, 8, 1);
    }

    @Test
    public void testTypeCastFailure4() {
        doTestException("out reduce(1,1,i j->i*j)", "Not a sequence", 0, 11, 1);
    }

    @Test
    public void testTypeCastFailure5() {
        doTestException("out reduce({1,2},{1,2},i j->i*j)", "Not a number", 0, 17, 1);
    }

    @Test
    public void testTypeCastFailure6() {
        doTestException("out reduce({10,20},1,i j->{i,j})", "Not a number", 0, 26, 1);
    }

    @Test
    public void testTypeCast1() {
        doTest("out 1+{1,1}", "2");
    }

    @Test
    public void testTypeCast2() {
        doTest("out {1,1} + 10", "11");
    }

}
