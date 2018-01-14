package xyz.elmot.interpret;

import org.junit.Test;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CancelTest {

    private static final String LONG_RUNNING_SCRIPT = "out reduce({1,10e10}, 1e3,a b ->a*b)";

    @Test(timeout = 1000)
    public void testCancelProto() throws InterruptedException {
        System.out.println("ForkJoinPool.commonPool().getActiveThreadCount() = " + ForkJoinPool.commonPool().getActiveThreadCount());
        AtomicBoolean finished = new AtomicBoolean(false);
        ForkJoinPool forkJoinPool = new ForkJoinPool(7);
        forkJoinPool.submit(() -> {
            Ator.runScript(LONG_RUNNING_SCRIPT,
                    result -> {});
            finished.set(true);
        });
        Thread.sleep(300);
        assertTrue("Multiple thread are running",forkJoinPool.getActiveThreadCount() > 1);
        forkJoinPool.shutdownNow();
        Thread.sleep(200);
        assertEquals(0,forkJoinPool.getActiveThreadCount());
    }
}
