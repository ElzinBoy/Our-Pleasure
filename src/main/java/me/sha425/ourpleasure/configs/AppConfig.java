package me.sha425.ourpleasure.configs;

import me.sha425.ourpleasure.utils.CommandManager;
import me.sha425.ourpleasure.utils.SlashCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumSet;
import java.util.EventListener;
import java.util.List;

@Configuration
public class AppConfig {
    @Bean
    public JDA jda(
            @Value("${discord.bot.token}") String botToken,
            CommandManager commandManager,
            List<SlashCommand> allCommands
    ) throws InterruptedException {
        Object[] listeners = allCommands.stream()
                .filter(cmd -> cmd instanceof EventListener)
                .toArray();

        return JDABuilder.createLight(botToken, EnumSet.of(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_VOICE_STATES
        ))
                .enableCache(CacheFlag.VOICE_STATE)
                .addEventListeners(commandManager)
                .addEventListeners(listeners)
                .build().awaitReady();
    }
}
