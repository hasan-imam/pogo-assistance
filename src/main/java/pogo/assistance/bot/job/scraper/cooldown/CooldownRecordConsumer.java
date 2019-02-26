package pogo.assistance.bot.job.scraper.cooldown;

import com.google.common.base.Verify;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.stream.JsonWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Consumes {@link #recordQueue} by writing records in an output file.
 *
 * JSON writing happens in a streaming fashion so that...
 *   - if there's anything wrong with record serialization, it fails fast
 *   - there's low memory footprint due to the queue getting drain into the output file
 *   - even if the job is interrupted, we still get most records persisted in output file
 *
 * @implSpec
 *      Consumer kept as "light" as possible in terms of work that takes CPU. Producer does as much processing as
 *      possible, possibly using multi-threading, and leaving only the writing part to be done by consumer.
 *
 *      Stops consuming if finds null in {@link #recordQueue}. See {@link CooldownRecordProducer producer}.
 */
@Slf4j
@RequiredArgsConstructor
class CooldownRecordConsumer implements Callable<Long> {

    private final BlockingQueue<JsonElement> recordQueue;
    private final Gson gson;

    // Additional control variables that can be set to tweak behavior
    @Setter private Duration consumerTimeout = null;

    private AtomicLong consumedRecordCount;

    @Override
    public Long call() throws Exception {
        Verify.verify(consumedRecordCount == null, "Attempted to start consumer more than once");
        consumedRecordCount = new AtomicLong();

        // Prepare output channel
        final File dataOutputFile = createCooldownDataOutputFile();
        log.info("Writing output to file: {}", dataOutputFile.getAbsolutePath());

        // Consume queue
        try (final JsonWriter jsonWriter = gson.newJsonWriter(new FileWriter(dataOutputFile))) {
            jsonWriter.beginArray();
            consume(jsonWriter);
            jsonWriter.endArray();
        }

        log.info("Cooldown record consumer is done.");
        return consumedRecordCount.get();
    }

    public long getConsumedRecordCount() {
        return Optional.ofNullable(consumedRecordCount).map(AtomicLong::get)
                .orElseThrow(() -> new IllegalStateException("Consumer should be started before calling this."));
    }

    private void consume(final JsonWriter jsonWriter) {
        Verify.verify(consumedRecordCount.get() == 0,
                "Nothing else should be modifying the counter and this method should be called only once");
        // Loop until interrupted or producer signals 'end' by putting a null in the queue
        while (true) {
            final JsonElement jsonElement;
            try {
                if (consumerTimeout != null) {
                    // Returns null if timed-out or produced actually placed a null in queue, which mean's there's no
                    // more record to come
                    jsonElement = recordQueue.poll(consumerTimeout.toMillis(), TimeUnit.MILLISECONDS);
                } else {
                    // Returns null only if there's a null in the queue
                    jsonElement = recordQueue.take();
                }
            } catch (final InterruptedException e) {
                log.warn("Interrupted while waiting for new records to write. Stopping writing.", e);
                Thread.currentThread().interrupt();
                break;
            }

            if (jsonElement == null) {
                log.warn("Timed out waiting for record. Stopping writing.");
                break;
            } else if (JsonNull.INSTANCE.equals(jsonElement)) {
                log.info("Reached the end of record queue.");
                break;
            } else {
                gson.toJson(jsonElement, jsonWriter);
                consumedRecordCount.incrementAndGet();
            }
        }
    }

    private static File createCooldownDataOutputFile() {
        final String filePrefix = CooldownRecordConsumer.class.getSimpleName() + "-output-";
        final String fileSuffix = "-" + new SimpleDateFormat("yyyy-MM-dd").format(Date.from(Instant.now())) + ".json";
        try {
            return File.createTempFile(filePrefix, fileSuffix);
        } catch (final IOException e) {
            throw new RuntimeException("Failed to create temporary file to output results.");
        }
    }
}
