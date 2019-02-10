package pogo.assistance.bot.responder;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Verify;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * Bot that give `!cookie` to the people who gives `!rep` to the bot running user.
 *
 * Assumptions used in this implementation:
 *  - Reputation command : `!rep`
 *  - Reputation attribution message format: {@link #FORMAT_REP_ATTRIBUTION_MSG}, {@link #PATTERN_REP_ATTRIBUTION_MSG}
 *  - Reputation command format:
 *  - {@link JDA#getSelfUser()} is always mentioned in the `!rep` command and its response
 *  - There are not limits for using `!cookie` command and we can give it to everyone who reps us
 *
 * Terminologies:
 *  - Requesting rep is the action/event where a member executes `!rep [some other member]`
 *  - Attribution event/message is the one replied by bot to confirm that rep has indeed been given to the recipient
 *
 * @implNote
 *      Uses output of {@link Message#getContentStripped()} for matching with patterns, understanding what type of
 *      message it is, etc.
 */
@RequiredArgsConstructor
@Slf4j
@Getter(value = AccessLevel.PACKAGE)
public class RepHandler extends ListenerAdapter {

    private static final Long POKEDEX_100_GUILD_ID = 252776251708801024L;
    private static final Long POKEDEX_100_TATSUMAKI_CHANNEL = 380275290741538817L;
    private static final Long POKEDEX_100_TATSUMAKI_ID = 172002275412279296L;

    private static final long ROUTER_GUILD_ID = 521260041500622848L;
    private static final long CHANNEL_TEST_LIST_ROUTE_PREVIEW = 543431296211484672L;
    private static final long H13M_ID = 471666614857629696L;

    private static final Pattern PATTERN_REP_ATTRIBUTION_MSG =
            Pattern.compile("(\uD83C\uDD99  \\|  )(.*)( has given )(.*)( a reputation point!)");

    @VisibleForTesting
    static final String FORMAT_REP_COMMAND_MSG = "!rep %s";
    @VisibleForTesting
    static final String FORMAT_REP_ATTRIBUTION_MSG = "\uD83C\uDD99  |  %s has given %s a reputation point!";

    /**
     * Stores the users who have attempted to `!rep` but their command hasn't been responded (yet).
     * The virtue of making this a set ensures that multiple `!rep` attempt results in the user being in this container
     * only once.
     *
     * Issue: Members get looked up in this set by matching the members' effective name in the guild. Problem is that
     * two member can have same effective name. That's the reason of using actual Member object as set elements instead
     * of a mapping between effective name to member object. Making the backing map ordered ensures that the first
     * member to match is the one who has given the rep first. Still doesn't mean cookie will be given to the right
     * member, but makes it a bit less likely.
     *
     * TODO: replace with a container that evicts after certain time?
     */
    private final Set<Member> usersWhoRequestedRep = Collections.synchronizedSet(Collections.newSetFromMap(new LinkedHashMap<>()));

    private final long targetGuildId;
    private final long repChannelId;
    private final long repAttributorId;

    @Inject
    public RepHandler() {
        // Production setup
        targetGuildId = POKEDEX_100_GUILD_ID;
        repChannelId = POKEDEX_100_TATSUMAKI_CHANNEL;
        repAttributorId = POKEDEX_100_TATSUMAKI_ID;

//        // Use for testing on some dummy channel
//        this(ROUTER_GUILD_ID, CHANNEL_TEST_LIST_ROUTE_PREVIEW, H13M_ID);
    }

    /**
     * @implNote
     *      This doesn't get called (and the validations aren't run) if the event listener is registered after the JDA
     *      is done loading. Also notable that there's no guarantee that this will get called before any event triggers
     *      {@link #onGuildMessageReceived(GuildMessageReceivedEvent)}.
     */
    @Override
    public void onReady(final ReadyEvent event) {
        final Guild targetGuild = event.getJDA().getGuildById(targetGuildId);
        Verify.verifyNotNull(targetGuild, "Expected to have access to target guild");

        final TextChannel repActivityChannel = targetGuild.getTextChannelById(repChannelId);
        Verify.verifyNotNull(repActivityChannel, "Expected to have access to rep activity channel");
        Verify.verify(repActivityChannel.canTalk(), "Expected to have write permission to rep activity channel");

        final Message rankMessage = repActivityChannel.sendMessage("!daily").complete();
        Verify.verifyNotNull(rankMessage, "Should be able to send message to rep activity channel");

        log.info("Rep responder online...");
    }

    @Override
    public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {
        final TextChannel eventChannel = event.getChannel();
        if (eventChannel.getIdLong() != repChannelId) {
            return;
        }

        logIfAttributionMessage(event);

        if (!mentionsSelfUser(event)) {
            // Both !rep command and response contains the user who's the target of the command
            // If user isn't mentioned, ignore event
            return;
        }

        if (isFromSelf(event)) {
            log.trace("Ignored message from self: {}", event.getMessage().getContentDisplay());
            return;
        }

        if (hasRequestedReputationPoint(event)) {
            usersWhoRequestedRep.add(event.getMember());
            log.trace("'{}' has attempted to give rep", event.getMember().getEffectiveName());
            return;
        }
        final Optional<String> givingMember = hasAttributedReputationPoint(event);
        if (givingMember.isPresent()) {
            synchronized (usersWhoRequestedRep) {
                final Optional<Member> matchedRepSender = usersWhoRequestedRep.stream()
                        .filter(member -> givingMember.get().equals(member.getEffectiveName()))
                        .findFirst();
                if (matchedRepSender.isPresent()) {
                    giveCookie(matchedRepSender.get(), event);
                } else {
                    log.error("Rep attribution didn't match any known rep sender. User: '{}'. Attribution message: {}",
                            givingMember.get(),
                            event.getMessage().getContentStripped());
                }
            }
            log.trace("Processed attribution: {}", event.getMessage().getContentDisplay());
        }
    }

    private boolean isFromSelf(final GuildMessageReceivedEvent event) {
        return event.getJDA().getSelfUser().equals(event.getAuthor());
    }

    private boolean mentionsSelfUser(final GuildMessageReceivedEvent event) {
        return event.getMessage().getMentionedUsers().stream()
                .anyMatch(user -> user.getIdLong() == event.getJDA().getSelfUser().getIdLong());
    }

    private boolean hasRequestedReputationPoint(final GuildMessageReceivedEvent event) {
        return event.getMessage().getContentStripped().trim().startsWith("!rep ");
    }

    /**
     * @return
     *      {@link Member#getEffectiveName()} of the member who has given the rep, if this is an attribution message.
     */
    @VisibleForTesting
    Optional<String> hasAttributedReputationPoint(final GuildMessageReceivedEvent event) {
        if (event.getAuthor().getIdLong() != repAttributorId) {
            // Only tatsumaki attributes reputation point
            return Optional.empty();
        }

        final Matcher matcher = PATTERN_REP_ATTRIBUTION_MSG.matcher(event.getMessage().getContentStripped());
        if (!matcher.find()) {
            return Optional.empty();
        }

        log.trace("Attribution: {} → {}", matcher.group(2), matcher.group(4));
        return Optional.of(matcher.group(2));
    }

    @VisibleForTesting
    void giveCookie(final Member member, final GuildMessageReceivedEvent event) {
        Verify.verify(event.getChannel().getIdLong() == repChannelId,
                "Expected to get attribution message only on channel: %s",
                repChannelId);
        final String memberName = member.getEffectiveName();
        Verify.verifyNotNull(member);
        final long variableDelay = 10 + Math.round(Math.random() * 10);
        log.trace("Queuing `!cookie` command for '{}' with delay of {}s", memberName, variableDelay);
        event.getChannel()
                .sendMessage(new MessageBuilder("!cookie ").append(member).build())
                .queueAfter(
                        variableDelay,
                        TimeUnit.SECONDS,
                        message -> {
                            // On success, forget the member
                            usersWhoRequestedRep.remove(memberName);
                            log.trace("Fired `!cookie` command for '{}'", memberName);
                        },
                        throwable -> {
                            // On failure, talk about it and forget the member nonetheless
                            log.error(
                                    String.format("Failed to fire `!cookie` command for '%s' (ID: %s)",
                                            memberName, member.getUser().getId()),
                                    throwable);
                            usersWhoRequestedRep.remove(memberName);
                        });
    }

    // Purely exists for tracing/debugging
    private static void logIfAttributionMessage(final GuildMessageReceivedEvent event) {
        // rep command responses seem to start with 🆙
        if (event.getMessage().getContentStripped().startsWith("\uD83C\uDD99")) {
            final Matcher matcher = PATTERN_REP_ATTRIBUTION_MSG.matcher(event.getMessage().getContentStripped());
            if (matcher.find()) {
                log.info("Attribution message: {}", event.getMessage().getContentStripped());
                log.info("{} → {}", matcher.group(2), matcher.group(4));
            } else {
                log.info("Mismatch case: {}", event.getMessage().getContentStripped());
            }
        }
    }

}
