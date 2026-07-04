package me.sha425.ourpleasure.utils;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public interface SlashCommand {
    String getName();
    String getDescription();

    default Set<String> getGuildIds() {
        return Set.of();
    }

    void execute(SlashCommandInteractionEvent event);

    default List<OptionData> getOptions() {
        return Collections.emptyList();
    }

    default List<SubcommandData> getSubcommands() {
        return Collections.emptyList();
    }

    default DefaultMemberPermissions getPermissions() {
        return DefaultMemberPermissions.ENABLED;
    }

    default String getId() { return getName(); }

    default void handleButton(ButtonInteractionEvent event) {}

    default void handleSelectMenu(StringSelectInteractionEvent event) {}
}
