package xyz.elmot.interpret

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic.AtomicBoolean

class CancelTest {

    private val LONG_RUNNING_SCRIPT = "out reduce({1,10e10}, 1e3,a b ->a*b)"
    @Test(timeout = 1000)
    @Throws(InterruptedException::class)
    fun testCancelProto() {
        val finished = AtomicBoolean(false)
        var forkJoinPool = ForkJoinPool(7);
        forkJoinPool.submit {
            Ator.runScript(LONG_RUNNING_SCRIPT) { _ -> }
            finished.set(true)
        }
        Thread.sleep(300)
        assertTrue("Multiple thread are running",forkJoinPool.activeThreadCount > 1);
        forkJoinPool.shutdownNow()
        Thread.sleep(200)
        assertEquals(0, forkJoinPool.activeThreadCount)
    }
}