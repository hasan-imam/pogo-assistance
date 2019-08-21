package pogo.assistance.bot.job.scraper.cooldown;

import com.google.common.base.Stopwatch;
import com.google.common.base.Verify;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.TextChannel;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.data.extraction.source.discord.pokedex100.CooldownMessageProcessor;

/**
 * @implSpec
 *      Puts a null in {@link #recordQueue} to signal end of data. {@link CooldownRecordConsumer Consumer} stops waiting
 *      for data once it encounters null.
 */
@Slf4j
@RequiredArgsConstructor
public class CooldownRecordScraper implements Runnable {

    private static final Duration PROGRESS_REPORT_INTERVAL = Duration.ofMinutes(1);

    private final BlockingQueue<JsonElement> recordQueue;
    private final CooldownRecordProducer cooldownRecordProducer;
    private final CooldownRecordConsumer cooldownRecordConsumer;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final Stopwatch stopwatch = Stopwatch.createUnstarted();

    @Inject
    public CooldownRecordScraper(
            @Named(DiscordEntityConstants.NAME_PDEX100_BOT_COMMAND_CHANNEL) final TextChannel botCommandChannel,
            final Gson gson) {
        recordQueue = new LinkedBlockingQueue<>();
        cooldownRecordProducer = new CooldownRecordProducer(botCommandChannel, recordQueue, gson, new CooldownMessageProcessor());
        cooldownRecordConsumer = new CooldownRecordConsumer(recordQueue, gson);

        // Since we don't impose any limit on the message history iterator, it blocks endlessly when there is no more
        // message to retrieve. This kills off consumer if we haven't got any message to write for certain duration.
        cooldownRecordConsumer.setConsumerTimeout(Duration.ofMinutes(5));
    }

    @Override
    public void run() {
        Verify.verify(isRunning.compareAndSet(false, true), "Attempted to run scraper more than once");
        stopwatch.start();

        final ExecutorService executorService = Executors.newWorkStealingPool(4);
        final Future<Long> futureProducedRecordCount = executorService.submit(cooldownRecordProducer);
        final Future<Long> futureConsumedRecordCount = executorService.submit(cooldownRecordConsumer);
        executorService.shutdown();

        try {
            while (!executorService.awaitTermination(PROGRESS_REPORT_INTERVAL.toMillis(), TimeUnit.MILLISECONDS)) {
                log.info("Cooldown record scraper still running...");
                logProgress();
            }
            Verify.verify(futureProducedRecordCount.get().equals(futureConsumedRecordCount.get()),
                    "Produced and consumed record counts should match.");
        } catch (final InterruptedException e) {
            log.warn("Interrupted while waiting for producer/consumer to complete.");
            executorService.shutdownNow(); // Interrupt all worker threads
            Thread.currentThread().interrupt();
        } catch (final ExecutionException e) {
            if (futureProducedRecordCount.isDone() && !futureConsumedRecordCount.isDone()) {
                log.error("Producer failed. Killing consumer.", e);
                futureConsumedRecordCount.cancel(true);
            } else if (futureConsumedRecordCount.isDone() && !futureProducedRecordCount.isDone()) {
                log.error("Consumer failed. Killing producer.", e);
                futureProducedRecordCount.cancel(true);
            } else {
                log.error("Producer/consumer failed", e);
                Verify.verify(futureProducedRecordCount.isDone() && futureConsumedRecordCount.isDone());
            }
        } finally {
            log.info("Cooldown record scraper exiting...");
            logProgress();
        }
    }

    private void logProgress() {
        final long processedMessageCount = cooldownRecordProducer.getProcessedMessageCount();
        final long producedRecordCount = cooldownRecordProducer.getProducedRecordCount();
        final long consumedRecordCount = cooldownRecordConsumer.getConsumedRecordCount();
        final Duration elapsed = stopwatch.elapsed();
        log.info("Encountered {} messages ({}/minute)." +
                        " Produced {} ({}/minute) and consumed {} ({}/minute) records." +
                        " {} records in queue. Elapsed time: {}.",
                processedMessageCount, processedMessageCount/elapsed.toMinutes(),
                producedRecordCount, producedRecordCount/elapsed.toMinutes(),
                consumedRecordCount, consumedRecordCount/elapsed.toMinutes(),
                recordQueue.size(),
                elapsed);
    }
}
