package com.fredboat.autofred.event;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@ConfigurationProperties(prefix = "autofred")
@Component
class EventHandlerProperties {

    private Map<String, String> bots;       // Command name & clientId
    private Map<String, String> trusted;    // User ID and secret key
    private String listenChannel;

    public Map<String, String> getBots() {
        return bots;
    }

    public void setBots(Map<String, String> bots) {
        this.bots = bots;
    }

    public Map<String, String> getTrusted() {
        return trusted;
    }

    public void setTrusted(Map<String, String> trusted) {
        this.trusted = trusted;
    }

    public String getListenChannel() {
        return listenChannel;
    }

    public void setListenChannel(String listenChannel) {
        this.listenChannel = listenChannel;
    }

    @Override
    public String toString() {
        return "EventHandlerProperties{" +
                "bots=" + bots +
                ", trusted=" + trusted.keySet() +
                ", listenChannel='" + listenChannel + '\'' +
                '}';
    }
}
