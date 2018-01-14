package xyz.elmot.interpret.ui

import xyz.elmot.interpret.Ator
import xyz.elmot.interpret.eval.ErrorInfo
import java.util.Collections.singletonList
import java.util.concurrent.ForkJoinPool

class BackgroundScriptExecutor {
    data class Result(val id: Long, var errors: List<ErrorInfo> = emptyList(), val output: StringBuilder = StringBuilder())

    val KDB_DEBOUNCE_MSEC = 300L

    private fun newForkJoinPool(): ForkJoinPool {
        return ForkJoinPool(Math.max(2, Runtime.getRuntime().availableProcessors() - 1))
    }

    @Volatile
    private var pool = newForkJoinPool()

    /**
     * Tries to kill already running background execution by shutting down and recreating the
     */
    private fun killRunningBackground() {
        pool.shutdownNow()
        pool = newForkJoinPool()
    }

    fun runBackgroundScript(runId: Long, scriptText: String, whenDone: (Result) -> Unit) {
        killRunningBackground()
        val result = Result(runId)
        pool.submit({
            try {
                Thread.sleep(KDB_DEBOUNCE_MSEC)
                result.errors = Ator.runScript(scriptText, { s -> result.output.append(s); })
            } catch (e: RuntimeException) {
                val msg = e.javaClass.simpleName + ": " + e.message
                result.errors = singletonList(ErrorInfo(msg, 0, 0, 100))
            }
            whenDone.invoke(result)
        })
    }
}