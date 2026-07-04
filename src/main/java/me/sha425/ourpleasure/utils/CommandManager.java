package me.sha425.ourpleasure.utils;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CommandManager extends ListenerAdapter {
    private final Map<String, SlashCommand> byName = new HashMap<>();
    private final Map<String, SlashCommand> byId = new HashMap<>();

    public CommandManager(List<SlashCommand> allCommands) {
        for (SlashCommand cmd : allCommands) {
            byId.put(cmd.getId(), cmd);
            byName.put(cmd.getName(), cmd);
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        SlashCommand command = byName.get(event.getName());
        if (command != null) command.execute(event);
    }

    public List<SlashCommandData> getGlobalCommandData() {
        List<SlashCommandData> data = new ArrayList<>();
        for (SlashCommand cmd : byName.values()) {
            if (cmd.getGuildIds().isEmpty()) {
                data.add(buildSlashCommandData(cmd));
            }
        }
        return data;
    }

    public List<SlashCommandData> getGuildCommandData(String guildId) {
        List<SlashCommandData> data = new ArrayList<>();
        for (SlashCommand cmd : byName.values()) {
            if (!cmd.getGuildIds().isEmpty() && cmd.getGuildIds().contains(guildId)) {
                data.add(buildSlashCommandData(cmd));
            }
        }
        return data;
    }

    private SlashCommandData buildSlashCommandData(SlashCommand cmd) {
        SlashCommandData slashCommandData = Commands.slash(cmd.getName(), cmd.getDescription())
                .setDefaultPermissions(cmd.getPermissions());

        if (!cmd.getSubcommands().isEmpty()) {
            slashCommandData.addSubcommands(cmd.getSubcommands());
        } else if (!cmd.getOptions().isEmpty()) {
            slashCommandData.addOptions(cmd.getOptions());
        }
        return slashCommandData;
    }

    public Set<String> getAllGuildIds() {
        Set<String> ids = new HashSet<>();
        for (SlashCommand cmd : byName.values()) {
            ids.addAll(cmd.getGuildIds());
        }
        return ids;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String command = event.getComponentId().split(":")[0];
        SlashCommand slashCommand = byId.get(command);
        if (slashCommand == null) return;
        slashCommand.handleButton(event);
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        String command = event.getComponentId().split(":")[0];
        SlashCommand slashCommand = byId.get(command);
        if (slashCommand == null) return;
        slashCommand.handleSelectMenu(event);
    }
}
