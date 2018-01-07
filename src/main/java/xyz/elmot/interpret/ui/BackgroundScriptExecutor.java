package xyz.elmot.interpret.ui;

import xyz.elmot.interpret.Ator;
import xyz.elmot.interpret.eval.ErrorInfo;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Performs parsing and execution of the script in background.
 */
public class BackgroundScriptExecutor {
    @SuppressWarnings("WeakerAccess")
    public static final int KDB_DEBOUNCE_MSEC = 300;
    private volatile ScheduledExecutorService pool = Executors.newSingleThreadScheduledExecutor();

    public static class Result {
        private final long id;
        private List<ErrorInfo> errors = Collections.emptyList();
        private StringBuilder output = new StringBuilder();

        public List<ErrorInfo> getErrors() {
            return errors;
        }

        void setErrors(List<ErrorInfo> errors) {
            this.errors = errors;
        }

        public String getOutput() {
            return output.toString();
        }

        Result(long id) {
            this.id = id;
        }

        public long getId() {
            return id;
        }
    }

    public void runBackgroundScript(long runId, String scriptText, Consumer<Result> whenDone) {
        killRunningBackground();
        Result result = new Result(runId);
        pool.schedule(() -> {
            try {
                result.setErrors(Ator.runScript(scriptText, s -> result.output.append(s)));
            } catch (RuntimeException e) {
                String msg = e.getClass().getSimpleName() + ": " + e.getMessage();
                result.setErrors(Collections.singletonList(new ErrorInfo(msg, 0, 0, 100)));
            }
            whenDone.accept(result);
        }, KDB_DEBOUNCE_MSEC, TimeUnit.MILLISECONDS);
    }

    /**
     * Tries to kill already running background execution by shutting down and recreating the
     */
    private void killRunningBackground() {
        pool.shutdownNow();
        pool = Executors.newSingleThreadScheduledExecutor();
    }
}
