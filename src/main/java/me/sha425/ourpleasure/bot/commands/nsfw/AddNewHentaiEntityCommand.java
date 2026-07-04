package me.sha425.ourpleasure.bot.commands.nsfw;

import me.sha425.ourpleasure.dto.HentaiEntity;
import me.sha425.ourpleasure.service.HentaiService;
import me.sha425.ourpleasure.utils.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AddNewHentaiEntityCommand implements SlashCommand {
    private final HentaiService hentaiService;
    private final Hentai hentaiCommand;

    @Value("${bot.commands.lang:ru}")
    private String lang;

    public AddNewHentaiEntityCommand(HentaiService hentaiService, Hentai hentaiCommand) {
        this.hentaiService = hentaiService;
        this.hentaiCommand = hentaiCommand;
    }

    @Override
    public String getName() {
        return "en".equalsIgnoreCase(lang) ? "add_hentai" : "добавить_хентай";
    }

    @Override
    public String getDescription() {
        return "en".equalsIgnoreCase(lang) ? "Adds a new hentai entry" : "Добавляет новый хентай";
    }

    private final Map<String, HentaiEntity> pendingAdditions = new ConcurrentHashMap<>();

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        HentaiEntity entity = new HentaiEntity();

        entity.setTitle(Objects.requireNonNull(event.getOption("title")).getAsString());
        entity.setUrl(Objects.requireNonNull(event.getOption("url")).getAsString());
        entity.setImageUrl(Objects.requireNonNull(event.getOption("image_url")).getAsString());
        entity.setColor(Objects.requireNonNull(event.getOption("color")).getAsString());
        entity.setYear(Objects.requireNonNull(event.getOption("year")).getAsString());
        entity.setSeries(Objects.requireNonNull(event.getOption("series")).getAsString());
        entity.setCensorship(Objects.requireNonNull(event.getOption("censorship")).getAsBoolean());
        entity.setAverageScore(Objects.requireNonNull(event.getOption("average_score")).getAsDouble());
        entity.setGenres(Objects.requireNonNull(event.getOption("genres")).getAsString());
        entity.setVoiceOver(Objects.requireNonNull(event.getOption("voice_over")).getAsString());
        entity.setStory(Objects.requireNonNull(event.getOption("story")).getAsString());

        OptionMapping optionMapping = event.getOption("blot");
        if (optionMapping != null) {
            entity.setBlot(optionMapping.getAsString());
        }

        entity.setThumbnail("новое");

        LocalDateTime localDateTime = LocalDateTime.now();
        entity.setDate(localDateTime.toLocalDate());

        String color = colorValidator(event);
        if (color == null) {
            event.reply("en".equalsIgnoreCase(lang) 
                    ? "Invalid color format, please use HEX (e.g. #FF0000)." 
                    : "Цвет указан неверно, используйте HEX.").queue();
            return;
        }
        entity.setColor(color);

        String transactionId = event.getUser().getIdLong() + "_" + System.currentTimeMillis();
        pendingAdditions.put(transactionId, entity);

        Button successBnt = Button.success("hentai_add:confirm:" + transactionId, "en".equalsIgnoreCase(lang) ? "Confirm" : "Подтвердить");
        Button cancelBnt = Button.danger("hentai_add:cancel:" + transactionId, "en".equalsIgnoreCase(lang) ? "Cancel" : "Отменить");

        EmbedBuilder confirmEmbed = hentaiCommand.createHentaiEmbed(entity, null, null, null);

        event.replyEmbeds(confirmEmbed.build())
                .addComponents(ActionRow.of(successBnt, cancelBnt))
                .queue();
    }

    private String colorValidator(SlashCommandInteractionEvent event) {
        String color = Objects.requireNonNull(event.getOption("color")).getAsString();
        if (color.startsWith("#") && color.length() == 7 ) {
            return color;
        }
        return null;
    }

    @Override
    public List<OptionData> getOptions() {
        if ("en".equalsIgnoreCase(lang)) {
            return List.of(
                    req(OptionType.STRING, "title", "hentai title"),
                    req(OptionType.STRING, "url", "hentai link"),
                    req(OptionType.STRING, "image_url", "image link"),
                    req(OptionType.STRING, "year", "release year"),
                    req(OptionType.STRING, "series", "number of series"),
                    req(OptionType.BOOLEAN, "censorship", "censorship"),
                    req(OptionType.NUMBER, "average_score", "average score"),
                    req(OptionType.STRING, "genres", "genres"),
                    req(OptionType.STRING, "voice_over", "voice acting"),
                    req(OptionType.STRING, "color", "hex color code"),
                    req(OptionType.STRING, "story", "story description"),
                    new OptionData(OptionType.STRING, "blot", "blot").setRequired(false)
            );
        } else {
            return List.of(
                    req(OptionType.STRING, "title", "название хентая"),
                    req(OptionType.STRING, "url", "ссылка на хентай"),
                    req(OptionType.STRING, "image_url", "ссылка на изображение"),
                    req(OptionType.STRING, "year", "год выпуска"),
                    req(OptionType.STRING, "series", "количество серий"),
                    req(OptionType.BOOLEAN, "censorship", "цензура"),
                    req(OptionType.NUMBER, "average_score", "средняя оценка"),
                    req(OptionType.STRING, "genres", "жанры"),
                    req(OptionType.STRING, "voice_over", "озвучка"),
                    req(OptionType.STRING, "color", "цвет на английском языке"),
                    req(OptionType.STRING, "story", "история"),
                    new OptionData(OptionType.STRING, "blot", "пометка").setRequired(false)
            );
        }
    }

    private OptionData req(OptionType type, String name, String description) {
        return new OptionData(type, name, description).setRequired(true);
    }

    @Override
    public Set<String> getGuildIds() {
        return Set.of("1182397718757384323");
    }

    @Override
    public DefaultMemberPermissions getPermissions() {
        return DefaultMemberPermissions.DISABLED;
    }

    @Override
    public String getId() {
        return "hentai_add";
    }

    @Override
    public void handleButton(ButtonInteractionEvent event) {
        String[] parts = event.getComponentId().split(":");

        String action = parts[1];
        String transactionId = parts[2];

        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (action.equals("confirm")) {
            HentaiEntity entity = pendingAdditions.get(transactionId);
            hentaiService.addNewEntry(entity);
            pendingAdditions.remove(transactionId);

            String header = "en".equalsIgnoreCase(lang) ? "OBJECT ADDED" : "ОБЪЕКТ ДОБАВЛЕН";
            String descText = "en".equalsIgnoreCase(lang) 
                    ? "Object successfully added with ID `%s`." 
                    : "Объект успешно добавлен с идентификатором `%s`.";

            String description = String.format("""
                    ```ansi
                     [2;40m [0m [2;32m%s [0m
                    ```
                    %s
                    """, header, String.format(descText, entity.getId()));

            embedBuilder.setDescription(description)
                    .setColor(Color.GREEN)
                    .setAuthor(event.getUser().getEffectiveName(), null, event.getUser().getEffectiveAvatarUrl());
            
            event.editMessageEmbeds(embedBuilder.build()).setComponents().queue();

        } else if (action.equals("cancel")) {
            String header = "en".equalsIgnoreCase(lang) ? "ACTION CANCELLED" : "ДЕЙСТВИЕ ОТМЕНЕНО";
            String description = String.format("""
                            ```ansi
                             [2;40m [0m [2;31m%s [0m
                            ```
                            """, header);

            embedBuilder.setDescription(description)
                    .setColor(Color.RED)
                    .setAuthor(event.getUser().getEffectiveName(), null, event.getUser().getEffectiveAvatarUrl());

            pendingAdditions.remove(transactionId);
            event.editMessageEmbeds(embedBuilder.build()).setComponents().queue();
        }
    }
}
