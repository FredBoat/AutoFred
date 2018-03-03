package com.fredboat.autofred.event;

import com.fredboat.autofred.TokenResetter;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.jboss.aerogear.security.otp.Totp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;

@Controller
public class EventHandler extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(EventHandler.class);
    private static final String PREFIX = "::";
    private static final long CONFIRM_TIMEOUT = 20000;

    private final TokenResetter resetter;
    private final EventHandlerProperties props;
    private long lastConfirm = 0;
    private String lastConfirmKey = "";
    private final Object sync = new Object();
    private String userAwaitingFor = null;

    @Autowired
    public EventHandler(EventHandlerProperties props, TokenResetter resetter) {
        this.props = props;
        this.resetter = resetter;

        log.info(props.toString());
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        String raw = event.getMessage().getContentRaw();

        if (event.getAuthor().getId().equals(event.getJDA().getSelfUser().getId())
                || event.getMessage().getContentRaw().startsWith(PREFIX)) {
            log.info(event.getAuthor().getName() + "\t" + event.getMessage().getContentDisplay());
        }

        if (event.getAuthor().isBot()) return;

        for (String key : props.getBots().keySet()) {
            if (!raw.startsWith(PREFIX + key + " ") && !raw.startsWith(PREFIX + key) || !event.getChannel().getId().equals(props.getListenChannel())) {
                continue;
            }

            if (!props.getTrusted().containsKey(event.getAuthor().getId())) {
                event.getChannel().sendMessage("You do not have permission to use this command").queue();
                return;
            }

            if (lastConfirm - CONFIRM_TIMEOUT > System.currentTimeMillis() || !lastConfirmKey.equals(key)) {
                event.getChannel().sendMessage("Run that command again to confirm").queue();
                lastConfirm = System.currentTimeMillis();
                lastConfirmKey = key;
                return;
            }

            userAwaitingFor = event.getAuthor().getId();
            event.getChannel().sendMessage(event.getMember().getEffectiveName() + " please send temporary code by DMs").queue();
            lastConfirm = System.currentTimeMillis();
            lastConfirmKey = key;


        }
    }

    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        log.info(event.getAuthor().getName() + "\t" + event.getMessage().getContentDisplay());

        if (lastConfirm + CONFIRM_TIMEOUT > System.currentTimeMillis() && event.getAuthor().getId().equals(userAwaitingFor)) {
            // Verify the given token
            if (new Totp(props.getTrusted().get(event.getAuthor().getId())).verify(event.getMessage().getContentRaw())) {
                doReset(event);
            } else {
                event.getAuthor().openPrivateChannel().queue(privateChannel ->
                        privateChannel.sendMessage("Invalid TOTP code").queue());
            }
        } else {
            event.getAuthor().openPrivateChannel().queue(privateChannel ->
                    privateChannel.sendMessage("More info about this bot: https://github.com/FredBoat/AutoFred").queue());
        }


    }

    private void doReset(PrivateMessageReceivedEvent event) {
        event.getAuthor().openPrivateChannel().queue(privateChannel -> {
            try {
                log.info("Reset token for key " + lastConfirmKey);
                privateChannel.sendMessage(String.format("%s\n\nThis message will delete in 60 seconds", resetter.resetToken(props.getBots().get(lastConfirmKey))))
                        .queue(message -> {
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
