package com.fredboat.autofred.event;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "autofred")
@Component
class EventHandlerProperties {

    private Map<String, String> bots;
    private List<String> trusted;
    private String listenChannel;

    public Map<String, String> getBots() {
        return bots;
    }

    public void setBots(Map<String, String> bots) {
        this.bots = bots;
    }

    public List<String> getTrusted() {
        return trusted;
    }

    public void setTrusted(List<String> trusted) {
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
                ", trusted=" + trusted +
                ", listenChannel='" + listenChannel + '\'' +
                '}';
    }
}
