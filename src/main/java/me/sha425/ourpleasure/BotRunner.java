package me.sha425.ourpleasure;

import me.sha425.ourpleasure.utils.CommandManager;
import me.sha425.ourpleasure.utils.SlashCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@SuppressWarnings("unused")
public class BotRunner implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(BotRunner.class);

    private final JDA jda;
    private final CommandManager commandManager;

    public BotRunner(JDA jda, CommandManager commandManager) {
        this.jda = jda;
        this.commandManager = commandManager;
    }

    @Override
    public void run(@NotNull String... args) throws Exception {
        jda.updateCommands().addCommands(commandManager.getGlobalCommandData()).queue();

        for (String guildId : commandManager.getAllGuildIds()) {
            Guild guild = jda.getGuildById(guildId);
            if (guild != null) {
                List<SlashCommandData> guildCmds = commandManager.getGuildCommandData(guildId);
                guild.updateCommands().addCommands(guildCmds).queue();
                logger.info("Bot successfully started and commands updated in guild: " + guildId);
            } else {
                logger.error("Guild with ID " + guildId + " does not exist or bot is not in the server.");
            }
        }
    }
}
