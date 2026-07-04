package me.sha425.ourpleasure.bot.commands.nsfw;

import me.sha425.ourpleasure.dto.HentaiEntity;
import me.sha425.ourpleasure.db.repository.HentaiRepository;
import me.sha425.ourpleasure.utils.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Component
@SuppressWarnings("unused")
public class Hentai extends ListenerAdapter implements SlashCommand {
    private static final Random random = new Random();
    private final HentaiRepository repository;

    @Value("${bot.commands.lang:ru}")
    private String lang;

    public Hentai(HentaiRepository repository) {
        this.repository = repository;
    }

    @Override
    public String getName() {
        return "en".equalsIgnoreCase(lang) ? "hentai" : "хентай";
    }

    @Override
    public String getDescription() {
        return "en".equalsIgnoreCase(lang)
                ? "NSFW content command, only available in 18+ channels"
                : "Команда NSFW контента доступна только в 18+ каналах";
    }

    @Override
    public List<OptionData> getOptions() {
        if ("en".equalsIgnoreCase(lang)) {
            return List.of(
                new OptionData(OptionType.INTEGER, "id", "enter title ID")
                        .setRequired(false)
                        .setMinValue(1),
                new OptionData(OptionType.BOOLEAN, "ephemeral", "make ephemeral")
                        .setRequired(false)
            );
        } else {
            return List.of(
                new OptionData(OptionType.INTEGER, "id", "введите ID тайтла")
                        .setRequired(false)
                        .setMinValue(1),
                new OptionData(OptionType.BOOLEAN, "ephemeral", "сделать эфемерным")
                        .setRequired(false)
            );
        }
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!event.getName().equals(getName())) return;

        if (!event.getChannel().asTextChannel().isNSFW()) {
            sendError(event, "en".equalsIgnoreCase(lang) 
                    ? "This command can only be used in an NSFW channel." 
                    : "Эту команду можно использовать только в NSFW-канале.");
            return;
        }

        OptionMapping idOption = event.getOption("id");
        long id;
        long totalCount = repository.count();
        String ownerId = Objects.requireNonNull(event.getMember()).getId();
        String mode = "computer";

        if (totalCount == 0) {
            sendError(event, "en".equalsIgnoreCase(lang) 
                    ? "There are no hentai entries in the database yet. Add one using </add_hentai:0>!" 
                    : "В базе данных пока нет ни одного хентая. Добавьте его с помощью </добавить_хентай:0>!");
            return;
        }

        if (idOption != null) {
            id = idOption.getAsInt();
            if (id < 1 || id > totalCount) {
                sendError(event, "en".equalsIgnoreCase(lang) 
                        ? String.format("Hentai with the specified ID was not found, total in database: %d id.", totalCount) 
                        : String.format("Хентай с указанным номером не найден, всего в базе: %d id.", totalCount));
                return;
            }
        } else {
            id = random.nextInt((int) totalCount) + 1;
        }

        HentaiEntity hentaiEntity = repository.findById((int) id).orElse(null);
        if (hentaiEntity == null) {
            sendError(event, "en".equalsIgnoreCase(lang) 
                    ? "Could not find hentai with this ID." 
                    : "Не удалось найти хентай с таким номером.");
            return;
        }

        OptionMapping ephemeralOption = event.getOption("ephemeral");
        boolean isEphemeral = ephemeralOption != null && ephemeralOption.getAsBoolean();

        EmbedBuilder embedBuilder = createHentaiEmbed(hentaiEntity, id, totalCount, mode);

        Button prevButton = Button.secondary("hentai:prev:" + id + ":" + ownerId + ":" + mode  + ":" + isEphemeral, "en".equalsIgnoreCase(lang) ? "Back" : "Назад");
        Button nextButton = Button.secondary("hentai:next:" + id + ":" + ownerId + ":" + mode  + ":" + isEphemeral, "en".equalsIgnoreCase(lang) ? "Next" : "Вперёд");
        Button currentModeButton = Button.secondary("hentai:mode:" + id + ":" + ownerId + ":" + mode  + ":" + isEphemeral, "🖥️");

        event.replyEmbeds(embedBuilder.build())
                .setComponents(ActionRow.of(prevButton, nextButton, currentModeButton))
                .setEphemeral(isEphemeral)
                .queue();
    }

    @Override
    public String getId() {
        return "hentai";
    }

    @Override
    public void handleButton(ButtonInteractionEvent event) {
        String[] parts = event.getComponentId().split(":");
        if (!parts[0].equals("hentai")) return;

        String action = parts[1];
        int currentId = Integer.parseInt(parts[2]);
        String ownerId = parts[3];
        String mode = parts[4];
        boolean isEphemeral = Boolean.parseBoolean(parts[5]);

        if (!Objects.requireNonNull(event.getMember()).getId().equals(ownerId)) {
            sendError(event, "en".equalsIgnoreCase(lang) 
                    ? "These are not your buttons! Run the command yourself." 
                    : "Это не твои кнопки! Вызови команду сам.");
            return;
        }

        long countPages = repository.count();
        long targetId = getTargetId(currentId, countPages, action);

        HentaiEntity targetHentai = repository.findById((int) targetId).orElse(null);
        if (targetHentai == null) {
            event.reply("en".equalsIgnoreCase(lang) 
                    ? "Error: data not found in the database." 
                    : "Ошибка: данные не найдены в базе.").setEphemeral(true).queue();
            return;
        }

        if (action.equals("mode")) {
            mode = mode.equals("computer") ? "phone" : "computer";
        }

        String labelModeButton = mode.equals("computer") ? "🖥️" : "📱";

        EmbedBuilder embedBuilder = createHentaiEmbed(targetHentai, targetId, countPages, mode);

        Button prevButton = Button.secondary("hentai:prev:" + targetId + ":" + ownerId + ":" + mode  + ":" + isEphemeral, "en".equalsIgnoreCase(lang) ? "Back" : "Назад");
        Button nextButton = Button.secondary("hentai:next:" + targetId + ":" + ownerId + ":" + mode  + ":" + isEphemeral, "en".equalsIgnoreCase(lang) ? "Next" : "Вперёд");
        Button currentModeButton = Button.secondary("hentai:mode:" +  targetId + ":" + ownerId + ":" + mode  + ":" + isEphemeral, labelModeButton);

        event.editMessageEmbeds(embedBuilder.build())
                .setComponents(ActionRow.of(prevButton, nextButton, currentModeButton))
                .queue();
    }

    private void sendError(IReplyCallback event, String message) {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.RED)
                .setAuthor(event.getUser().getEffectiveName(), null, event.getUser().getEffectiveAvatarUrl())
                .setDescription("> " + message);
        event.replyEmbeds(embedBuilder.build())
                .setEphemeral(true)
                .queue();
    }

    private long getTargetId(int currentId, long totalCount, String action) {
        if (action.equals("prev")) {
            return (currentId <= 1) ? totalCount : currentId - 1;
        } else if (action.equals("next")) {
            return (currentId >= totalCount) ? 1 : currentId + 1;
        }
        return currentId;
    }

    /**
     * Creates an {@link EmbedBuilder} to display hentai information.
     * Formats the entity data into a visually appealing Discord Embed, considering the display mode (computer/phone).
     *
     * @param hentaiEntity the {@link HentaiEntity} whose data needs to be displayed.
     * @param id the ID of the current hentai entry.
     * @param countPages the total number of pages (entries) in the database.
     * @param mode the display mode ("computer" or "phone"), which affects text formatting.
     * @return a configured {@link EmbedBuilder}.
     */
    public EmbedBuilder createHentaiEmbed(HentaiEntity hentaiEntity, Long id, Long countPages, String mode) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(hentaiEntity.getTitle() + " " + hentaiEntity.getYear());

        String currentMode = (mode != null) ? mode : "computer";

        String genres = currentMode.equals("phone") ? truncate(hentaiEntity.getGenres(), 30) : hentaiEntity.getGenres();
        String story = currentMode.equals("phone") ? truncate(hentaiEntity.getStory(), 100) : hentaiEntity.getStory();
        String blot = currentMode.equals("phone") ? truncate(hentaiEntity.getBlot(), 0) : ("en".equalsIgnoreCase(lang) ? "🖊️ **Note:** " : "️🖊️ **Помарка:** ") + hentaiEntity.getBlot();

        String censorship = hentaiEntity.isCensorship() 
                ? ("en".equalsIgnoreCase(lang) ? "yes" : "присутствует") 
                : ("en".equalsIgnoreCase(lang) ? "no" : "отсутствует");

        if ("en".equalsIgnoreCase(lang)) {
            embedBuilder.setDescription(
                    ">>> 📺 **Series:** " + hentaiEntity.getSeries() + "\n" +
                            "⭐ **Score:** " + hentaiEntity.getAverageScore() + "\n" +
                            "🔞 **Censorship:** " + censorship + "\n" +
                            "🎙️ **Voice acting:** " + hentaiEntity.getVoiceOver() + "\n" +
                            "🏷️ **Genres:** " + genres + "\n" +
                            "📝 **Story:** " + story +
                            (hentaiEntity.getBlot() != null
                                    ? "\n" + blot
                                    : "")
            );
        } else {
            embedBuilder.setDescription(
                    ">>> 📺 **Серий:** " + hentaiEntity.getSeries() + "\n" +
                            "⭐ **Оценка:** " + hentaiEntity.getAverageScore() + "\n" +
                            "🔞 **Цензура:** " + censorship + "\n" +
                            "🎙️ **Озвучка:** " + hentaiEntity.getVoiceOver() + "\n" +
                            "🏷️ **Жанры:** " + genres + "\n" +
                            "📝 **Сюжет:** " + story +
                            (hentaiEntity.getBlot() != null
                                    ? "\n" + blot
                                    : "")
            );
        }

        Color color = Color.WHITE;

        if (hentaiEntity.getColor() != null) {
            color = Color.decode(hentaiEntity.getColor());
        }

        embedBuilder.setColor(color);
        embedBuilder.setImage(hentaiEntity.getImageUrl());
        embedBuilder.addField(
                "en".equalsIgnoreCase(lang) ? "Link: " : "Ссылка: ", 
                "en".equalsIgnoreCase(lang) ? "[Click to open](" + hentaiEntity.getUrl() + ")" : "[Нажать, чтобы перейти](" + hentaiEntity.getUrl() + ")", 
                false
        );
        embedBuilder.setFooter("en".equalsIgnoreCase(lang) 
                ? "🌸 | Page " + id + " of " + countPages 
                : "🌸 | Номер страницы " + id + " из " + countPages);

        String rawThumbnail = hentaiEntity.getThumbnail();
        String finalThumbnail = null;

        if (hentaiEntity.getThumbnail() != null) {
            embedBuilder.setThumbnail(hentaiEntity.getThumbnail());
        }
        return embedBuilder;
    }

    private String truncate(String content, int maxLength) {
        if (content == null || content.length() <= maxLength) return content;
        if (maxLength == 0) return "";
        String result = content.replaceAll("(?sU)^(.{" + maxLength + "}.*?)\\b.*", "$1");
        return result + "...";
    }
}
