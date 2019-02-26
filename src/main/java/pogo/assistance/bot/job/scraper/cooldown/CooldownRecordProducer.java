package pogo.assistance.bot.job.scraper.cooldown;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.google.common.base.Verify;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.entities.TextChannel;
import pogo.assistance.data.extraction.source.discord.MessageStream;
import pogo.assistance.data.extraction.source.discord.pokedex100.CooldownMessageProcessor;

@Slf4j
@RequiredArgsConstructor
class CooldownRecordProducer implements Callable<Long> {

    private final TextChannel botCommandChannel;
    private final BlockingQueue<JsonElement> recordQueue;
    private final Gson gson;
    private final CooldownMessageProcessor processor;

    // Additional control variables that can be set to tweak behavior
    @Setter private Duration queueOfferingTimeout = null;
    @Setter private long maxMessageProcessedLimit = Long.MAX_VALUE;
    @Setter private long maxRecordProducedLimit = Long.MAX_VALUE;

    private AtomicLong producedRecordCount;
    private AtomicLong processedMessageCount;

    @Override
    public Long call() {
        Verify.verify(producedRecordCount == null && processedMessageCount == null,
                "Attempted to start producer more than once");
        producedRecordCount = new AtomicLong();
        processedMessageCount = new AtomicLong();

        final boolean successful = produce();
        finishProducing(!successful); // Assumption: produce only fails for interruptions so we should finish immediately
        log.info("Cooldown record producer is done.");

        return producedRecordCount.get();
    }

    /**
     * @return
     *      True if successfully produced and enqueued elements in {@link #recordQueue}. False if
     */
    private boolean produce() {
        try {
            MessageStream.lookbackMessageStream(botCommandChannel)
                    .limit(maxMessageProcessedLimit) // applying this limit is important
                    .peek(__ -> processedMessageCount.incrementAndGet())
                    .parallel()
                    .unordered() // to speedup the limit stage of this parallel stream
                    .map(processor::processWithoutThrowing)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .limit(maxRecordProducedLimit) // limits on parallel streams can be expensive
                    .map(gson::toJsonTree)
                    .peek(__ -> producedRecordCount.incrementAndGet())
                    .forEach(jsonElement -> {
                        // Throw if we fail to enqueue so we exit this producer stream
                        Verify.verify(enqueue(jsonElement), "Failed to enqueue produced element");
                    });
            return true;
        } catch (final RuntimeException e) {
            log.error("Producing didn't complete successfully", e);
            return false;
        }
    }

    /**
     * @param jsonElement
     *      Element to be placed in {@link #recordQueue}
     * @return
     *      Returns true if it successfully enqueues the {@code jsonElement} within {@link #queueOfferingTimeout},
     *      without getting interrupted. Returns false if the timeout has passed or {@link InterruptedException
     *      interruption} occurred while waiting for space.
     */
    private boolean enqueue(final JsonElement jsonElement) {
        try {
            if (queueOfferingTimeout != null) {
                final boolean successful = recordQueue.offer(jsonElement, queueOfferingTimeout.toMillis(), MILLISECONDS);
                if (!successful) {
                    log.error("Timed out while waiting for capacity to place new produced element.");
                }
                return successful;
            } else {
                recordQueue.put(jsonElement);
                return true;
            }
        } catch (final InterruptedException e) {
            log.error("Interrupted while putting produced record in queue.", e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Indicates the end of production by putting a {@code null} at the end of {@link #recordQueue}.
     *
     * @param finishImmediately
     *      If true, removes an element from {@link #recordQueue} if there is no space left on it so that it can place
     *      the {@code null}. If false, waits for space to become available to be able to place {@code null}.
     */
    private void finishProducing(final boolean finishImmediately) {
        if (finishImmediately) {
            if (recordQueue.remainingCapacity() == 0) {
                log.error("Queue ran out of capacity. Removed this element to make space: {}", recordQueue.poll());
            }
            if (!recordQueue.offer(JsonNull.INSTANCE)) {
                log.error("Failed to add null to queue to indicate end of queue.");
            }
        } else {
            enqueue(JsonNull.INSTANCE);
        }
    }

    public long getProducedRecordCount() {
        return Optional.ofNullable(producedRecordCount).map(AtomicLong::get)
                .orElseThrow(() -> new IllegalStateException("Producer should be started before calling this."));
    }

    public long getProcessedMessageCount() {
        return Optional.ofNullable(processedMessageCount).map(AtomicLong::get)
                .orElseThrow(() -> new IllegalStateException("Producer should be started before calling this."));
    }
}
