package xyz.elmot.interpret.ui;

import xyz.elmot.interpret.Ator;
import xyz.elmot.interpret.eval.ErrorInfo;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;

/**
 * Performs parsing and execution of the script in background.
 */
public class BackgroundScriptExecutor {
    @SuppressWarnings("WeakerAccess")
    public static final int KDB_DEBOUNCE_MSEC = 300;
    private volatile ForkJoinPool pool = newForkJoinPool();

    private ForkJoinPool newForkJoinPool() {
        return new ForkJoinPool(Math.max(2, Runtime.getRuntime().availableProcessors() - 1));
    }

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
        pool.submit(() -> {
            try {
                Thread.sleep(KDB_DEBOUNCE_MSEC);
                result.setErrors(Ator.runScript(scriptText, s -> result.output.append(s)));
            } catch (RuntimeException e) {
                String msg = e.getClass().getSimpleName() + ": " + e.getMessage();
                result.setErrors(Collections.singletonList(new ErrorInfo(msg, 0, 0, 100)));
            } catch (InterruptedException ignored) {
            }
            whenDone.accept(result);
        });
    }

    /**
     * Tries to kill already running background execution by shutting down and recreating the
     */
    private void killRunningBackground() {
        pool.shutdownNow();
        pool = newForkJoinPool();
    }
}
