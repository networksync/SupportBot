package uk.co.netbans.supportbot.Support.Listeners;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.json.simple.parser.ParseException;
import uk.co.netbans.supportbot.NetBansBot;
import uk.co.netbans.supportbot.Utils.Util;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class SuggestionListener extends ListenerAdapter {

    private NetBansBot bot;
    private final ScheduledExecutorService executorService;
    final AtomicBoolean hasSent = new AtomicBoolean(false);
    public SuggestionListener(NetBansBot bot) {
        this.bot = bot;
        this.executorService = Executors.newScheduledThreadPool(2);
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getMessage().getContentRaw().startsWith(bot.getCommandPrefix()))
            return;

        List<String[]> tips = new ArrayList<>();
        try {
            tips = bot.getTips();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        for (String[] sugg : tips) {
            AtomicLong messageId = new AtomicLong();
            if (event.getMessage().getContentRaw().toLowerCase().contains(sugg[0])) {
                if (hasSent.get())
                    return;
                if (Util.containsLink(event.getMessage().getContentRaw()))
                    return;
                event.getChannel().sendMessage(new EmbedBuilder()
                        .setColor(Color.CYAN)
                        .setDescription(sugg[1]
                                .replaceAll("<tag>", event.getAuthor().getAsMention())
                                .replaceAll("<prefix>", bot.getCommandPrefix())
                                .replaceAll("<githubissues>", "https://github.com/NetBans/NetBans/issues/new")
                                .replaceAll("<github>", "https://github.com/NetBans")
                                .replaceAll("<download>", "TODO") //TODO: download link!
                                .replaceAll("<sponge>", "TODO")
                                .replaceAll("<spigot>", "TODO")
                                .replaceAll("<velocity>", "TODO")
                                .replaceAll("<bungee>", "TODO")
                        ).build()).queue(x -> {
                            messageId.set(x.getIdLong());
                            hasSent.set(true);
                    executorService.schedule(()-> {
                        event.getChannel().getMessageById(messageId.get()).complete().delete().complete();
                        hasSent.set(false);
                        messageId.set(0L);
                    },30, TimeUnit.SECONDS);
                });
            }
        }
    }
}
