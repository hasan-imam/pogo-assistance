package pogo.assistance.bot.job;

import dagger.Module;
import dagger.Provides;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.inject.Provider;
import pogo.assistance.bot.job.scraper.cooldown.CooldownRecordScraper;

@Module
class JobExecutionBotModule {

    @Provides
    public static Collection<Runnable> providesRunnables(
            final Set<WorkflowId> workflowIds,
            final Provider<CooldownRecordScraper> cooldownRecordScraperProvider) {
        final Set<Runnable> runnables = new LinkedHashSet<>();
        workflowIds.forEach(workflowId -> {
            switch (workflowId) {
                case COOLDOWN_SCRAPER:
                    runnables.add(cooldownRecordScraperProvider.get());
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled workflow ID: " + workflowId);
            }
        });
        return runnables;
    }

}
