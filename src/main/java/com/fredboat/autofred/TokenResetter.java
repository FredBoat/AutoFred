package com.fredboat.autofred;

import com.mashape.unirest.http.Unirest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import java.io.IOException;

@Controller
public class TokenResetter {

    private static final Logger log = LoggerFactory.getLogger(TokenResetter.class);
    private static final String USER_AGENT = "DiscordBot (https://github.com/FredBoat/AutoFred, 1.0)";
    private static final String RESET_ENDPOINT = "https://discordapp.com/api/oauth2/applications/{clientId}/bot/reset";

    private final String token;

    public TokenResetter(@Value("${autofred.userToken}") String token) {
        this.token = token;
    }

    public String resetToken(String clientId) throws IOException {
        try {
            return Unirest.post(RESET_ENDPOINT)
                    .header("Authorization", token)
                    .routeParam("clientId", clientId)
                    .header("User-Agent", USER_AGENT)
                    .asJson()
                    .getBody()
                    .getObject()
                    .getString("token");
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
