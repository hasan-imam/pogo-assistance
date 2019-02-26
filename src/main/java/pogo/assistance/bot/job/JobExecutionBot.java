package pogo.assistance.bot.job;

import com.google.common.base.Stopwatch;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JobExecutionBot implements Runnable {

    private final Collection<Runnable> runnables;

    @Inject
    public JobExecutionBot(@NonNull final Collection<Runnable> runnables) {
        this.runnables = runnables;
    }

    @Override
    public void run() {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        final ExecutorService executorService = getExecutorService();
        runnables.forEach(executorService::submit);
        executorService.shutdown();
        try {
            while (!executorService.awaitTermination(5, TimeUnit.MINUTES)) {
                log.info("Job(s) still running... Elapsed time: {}.", stopwatch.elapsed());
            }
        } catch (final InterruptedException e) {
            log.error("Interrupted during job execution", e);
            System.exit(1);
        }
        log.info("Job execution completed.");
        System.exit(0);
    }

    private static ExecutorService getExecutorService() {
        return Executors.newWorkStealingPool();
    }

}
