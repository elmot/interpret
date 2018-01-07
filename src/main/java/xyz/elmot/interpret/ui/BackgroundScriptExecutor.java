package xyz.elmot.interpret.ui;

import xyz.elmot.interpret.Ator;
import xyz.elmot.interpret.eval.ErrorInfo;

import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class BackgroundScriptExecutor {
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
        pool.shutdownNow();
        pool = Executors.newSingleThreadScheduledExecutor();
        Result result = new Result(runId);
        pool.schedule(() -> {
            Ator ator = new Ator();
            try {
                result.setErrors(ator.runScript(scriptText, s -> result.output.append(s)));
            } catch (RuntimeException e) {
                String msg = e.getClass().getSimpleName() + ": " + e.getMessage();
                result.setErrors(Collections.singletonList(new ErrorInfo(msg, 0, 0, 100)));
            }
            whenDone.accept(result);
        }, 300, TimeUnit.MILLISECONDS);
    }
}
