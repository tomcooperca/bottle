package com.github.tomcooperca.bottle.config;

import com.github.tomcooperca.bottle.message.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class AppConfig {

    private final MessageService messageService;

    @Scheduled(cron = "0 0 0 * * SUN")
    public void flushMessages() {
        messageService.deleteAllMessages();
    }
}
