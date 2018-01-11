package xyz.elmot.interpret

import org.junit.Assert
import org.junit.Test
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean

class CancelTest {

    private val LONG_RUNNING_SCRIPT = "out reduce({1,10e10}, 1e3,a b ->a*b)"
    @Test(timeout = 1000)
    @Throws(InterruptedException::class)
    fun testCancelProto() {
        val executor = ScheduledThreadPoolExecutor(1)
        val finished = AtomicBoolean(false)
        executor.submit {
            Ator.runScript(LONG_RUNNING_SCRIPT) { _ -> }
            finished.set(true)
        }
        Thread.sleep(50)
        Assert.assertEquals(1, executor.activeCount.toLong())
        executor.shutdownNow()
        Thread.sleep(200)
        Assert.assertEquals(0, executor.activeCount.toLong())
    }
}