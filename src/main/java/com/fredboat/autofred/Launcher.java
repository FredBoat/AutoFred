package com.fredboat.autofred;

import com.fredboat.autofred.event.EventHandler;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;

import javax.security.auth.login.LoginException;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@Controller
public class Launcher {

    private static final Logger log = LoggerFactory.getLogger(Launcher.class);
    private final JDA jda;

    @Autowired
    public Launcher(JDA jda) {
        this.jda = jda;
    }

    public static void main(String[] args) {
        SpringApplication sa = new SpringApplication(Launcher.class);
        sa.setWebEnvironment(false);
        sa.run();
    }

    @Bean
    static JDA jda(@Value("${autofred.botToken}") String token,
                   EventHandler eventHandler) throws LoginException, InterruptedException, RateLimitedException {

        JDABuilder b = new JDABuilder(AccountType.BOT);

        return b.setToken(token)
                .setAudioEnabled(false)
                .addEventListener(eventHandler)
                .buildBlocking();

    }
}
