package com.fredboat.autofred.event;

import com.fredboat.autofred.TokenResetter;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;

@Controller
public class EventHandler extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(EventHandler.class);
    private static final String PREFIX = "::";
    private static final long CONFIRM_TIMEOUT = 10000;

    private final TokenResetter resetter;
    private final EventHandlerProperties props;
    private long lastConfirm = 0;
    private String lastConfirmKey = "";
    private final Object sync = new Object();

    @Autowired
    public EventHandler(EventHandlerProperties props, TokenResetter resetter) {
        this.props = props;
        this.resetter = resetter;

        log.info(props.toString());
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        String raw = event.getMessage().getRawContent();

        if (event.getAuthor().getId().equals(event.getJDA().getSelfUser().getId())
                || event.getMessage().getRawContent().startsWith(PREFIX)) {
            log.info(event.getAuthor().getName() + "\t" + event.getMessage().getContent());
        }

        if (event.getAuthor().isBot()) return;

        for (String key : props.getBots().keySet()) {
            if (!raw.startsWith(PREFIX + key + " ") && !raw.startsWith(PREFIX + key) || !event.getChannel().getId().equals(props.getListenChannel())) {
                continue;
            }

            if (!props.getTrusted().contains(event.getAuthor().getId())) {
                event.getChannel().sendMessage("You do not have permission to use this command").queue();
                return;
            }

            if (lastConfirm - CONFIRM_TIMEOUT > System.currentTimeMillis() || !lastConfirmKey.equals(key)) {
                event.getChannel().sendMessage("Run that command again to confirm").queue();
                lastConfirm = System.currentTimeMillis();
                lastConfirmKey = key;
                return;
            }

            event.getChannel().sendMessage("Sending new token to " + event.getMember().getEffectiveName()).queue();
            event.getAuthor().openPrivateChannel().queue(privateChannel -> {
                try {
                    log.info("Reset token for key " + key);
                    privateChannel.sendMessage(resetter.resetToken(props.getBots().get(key)) + "\n\nThis message will delete in 60 seconds").queue(message -> {
                        synchronized (sync) {
                            try {
                                sync.wait(60000);
                                message.delete().queue();
                            } catch (InterruptedException e) {
                                log.error("Got interrupted", e);
                            }
                        }
                    });
                } catch (IOException e) {
                    privateChannel.sendMessage(e.getMessage()).queue();
                    log.error("Failed to reset token!", e);
                }
            });
        }
    }

    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        log.info(event.getAuthor().getName() + "\t" + event.getMessage().getContent());

        event.getAuthor().openPrivateChannel().queue(privateChannel ->
                privateChannel.sendMessage("More info about this bot: https://github.com/FredBoat/AutoFred").queue());
    }

}
