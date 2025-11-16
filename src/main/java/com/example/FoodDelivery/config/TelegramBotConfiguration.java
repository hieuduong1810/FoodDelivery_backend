package com.example.FoodDelivery.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import com.example.FoodDelivery.service.TelegramBotService;

import lombok.extern.slf4j.Slf4j;

@Configuration
@ConditionalOnProperty(prefix = "telegram.bot", name = "enabled", havingValue = "true")
@Slf4j
public class TelegramBotConfiguration {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBotService telegramBotService) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(telegramBotService);
            log.info("Telegram Bot registered successfully: {}", botUsername);
            return botsApi;
        } catch (TelegramApiException e) {
            log.error("Failed to register Telegram Bot: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize Telegram Bot", e);
        }
    }
}
