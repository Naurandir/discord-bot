package com.discord.bot.silvester;

import com.discord.command.Command;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import java.util.Map;
import java.util.Random;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Setter
public class CommandImplBleigiessen implements Command {
    
    private final String title = "{user} - **{result}**";
    private final String content = "<@{id}> hat **{result}** gegossen\nBedeutung (kurz): {description}\n\nMehr Details unter:\n{url}";
    private final String footer = "Powered by Naurandir";
    
    private final String command;
    private final String icon;
    private final String threadImage;
    private final String footerImage;
    
    private final Map<String, String> shortDescription;
    private final Map<String, String> urlToDetail;

    CommandImplBleigiessen(String command, String icon, String threadImage, String footerImage, 
            Map<String, String> shortDescription, Map<String, String> urlToDetail) {
        this.command = command;
        this.icon = icon;
        this.threadImage = threadImage;
        this.footerImage = footerImage;
        this.shortDescription = shortDescription;
        this.urlToDetail = urlToDetail;
    }

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        log.info("execute: received message [id:{}, guildId:{}, channelId:{}, content:{}]", 
                event.getMessage().getId(), event.getGuildId(), event.getMessage().getChannelId(), event.getMessage().getContent());
        return event.getMessage().getChannel()
                .flatMap(channel -> channel.createEmbed(spec -> generateEmbed(spec, event.getMessage())))
                .then();
    }

    @Override
    public String getCommandWord() {
        return command;
    }
    
    private void generateEmbed(EmbedCreateSpec spec, Message message) {
        Random rand = new Random();
        int upperbound = shortDescription.size();
        int randomEntryNumber = rand.nextInt(upperbound);
        
        String result = (String) shortDescription.keySet().toArray()[randomEntryNumber];
        String description = shortDescription.get(result);
        String url = urlToDetail.get(result);
        
        String id;
        String user;
        try {
            user = message.getAuthorAsMember().toFuture().get().getDisplayName();
            id = message.getAuthorAsMember().toFuture().get().getId().asString();
        } catch (Exception ex) {
            log.warn("generateEmbed: could not obtain author as member: [{}], fallback to simple author", ex.getMessage());
            user = message.getAuthor().get().getUsername();
            id = message.getAuthor().get().getUsername();
        }
        
        spec.setTitle(title.replace("{user}", user)
                           .replace("{result}", result));
        spec.setDescription(content.replace("{id}", id)
                                   .replace("{result}", result)
                                   .replace("{description}", description)
                                   .replace("{url}", url));
        spec.setFooter(footer, footerImage);
        
        spec.setColor(Color.GREEN);
        spec.setUrl(url);
        spec.setThumbnail(threadImage);
        spec.setImage(icon);
        
        log.info("generateEmbed: generated [{} for {}]", result, user);
    }
}
