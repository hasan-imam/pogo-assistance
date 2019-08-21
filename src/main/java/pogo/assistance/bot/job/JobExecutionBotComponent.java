package pogo.assistance.bot.job;

import java.util.Set;
import javax.inject.Named;

import dagger.BindsInstance;
import dagger.Component;
import net.dv8tion.jda.api.AccountType;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.bot.di.DiscordEntityModule;
import pogo.assistance.data.serde.SerDeModule;

@Component(modules = { DiscordEntityModule.class, JobExecutionBotModule.class, SerDeModule.class })
public interface JobExecutionBotComponent {

    JobExecutionBot getJobExecutionBot();

    @Component.Builder
    interface Builder {
        /**
         * {@link net.dv8tion.jda.api.JDA} will use this token to login.
         */
        @BindsInstance
        JobExecutionBotComponent.Builder userToken(@Named(DiscordEntityConstants.NAME_USER_TOKEN_OWNER) final String userName);

        @BindsInstance
        JobExecutionBotComponent.Builder accountType(final AccountType accountType);

        @BindsInstance
        JobExecutionBotComponent.Builder workflowIds(final Set<WorkflowId> workflowIds);

        JobExecutionBotComponent build();
    }

}
