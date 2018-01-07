package xyz.elmot.interpret;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

public class TimeTest {


    private static final String LONG_RUNNING_SCRIPT = "out reduce({1,10e10}, 1e3,a b ->a*b)";

    @Test(timeout = 1000)
    public void testCancelProto() throws InterruptedException {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        AtomicBoolean finished = new AtomicBoolean(false);
        executor.submit(() -> {
            Ator.runScript(LONG_RUNNING_SCRIPT,
                    result -> {});
            finished.set(true);
        });
        Thread.sleep(50);
        Assert.assertEquals(1,executor.getActiveCount());
        executor.shutdownNow();
        Thread.sleep(200);
        Assert.assertEquals(0,executor.getActiveCount());
    }
}
