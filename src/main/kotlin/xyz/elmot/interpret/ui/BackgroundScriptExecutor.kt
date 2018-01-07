package xyz.elmot.interpret.ui

import xyz.elmot.interpret.Ator
import xyz.elmot.interpret.eval.ErrorInfo
import java.util.*
import java.util.Collections.singletonList
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class BackgroundScriptExecutor {
    data class Result(val id: Long, var errors:List<ErrorInfo> = emptyList(), val output:StringBuilder = StringBuilder())

    val KDB_DEBOUNCE_MSEC = 300L

    @Volatile
    private var pool = Executors.newSingleThreadScheduledExecutor()

    /**
     * Tries to kill already running background execution by shutting down and recreating the
     */
    private fun killRunningBackground() {
        pool.shutdownNow()
        pool = Executors.newSingleThreadScheduledExecutor()
    }

    fun runBackgroundScript(runId:Long, scriptText:String, whenDone: (Result)->Unit)
    {
        killRunningBackground()
        val result = Result(runId);
        pool.schedule({
            try {
                result.errors =Ator.runScript(scriptText, {s ->result.output.append(s);});
            } catch (e:RuntimeException ) {
                val msg = e.javaClass.simpleName + ": " + e.message;
                result.errors = singletonList(ErrorInfo(msg, 0, 0, 100));
            }
            whenDone.invoke(result);
        }, KDB_DEBOUNCE_MSEC, TimeUnit.MILLISECONDS);
    }
}