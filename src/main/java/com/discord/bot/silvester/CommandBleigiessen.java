package com.discord.bot.silvester;

import com.discord.command.Command;
import com.discord.command.CommandGenerator;
import com.google.gson.Gson;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.EmbedFooterData;
import discord4j.rest.util.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Component
public class CommandBleigiessen implements CommandGenerator {
    
    @Value( "${discord.bot.silvester.command.bleigiessen}" )
    private String command;
    
    @Value( "${discord.bot.silvester.file.bleigiessen}" )
    private String filePath;
    
    @Value( "${discord.bot.silvester.detail.bleigiessen}" )
    private String detailUrl;
    
    private String bleigiessenIcon = "https://image.winudf.com/v2/image/Y29tLnRiYXBwZGV2LmJsZWlnaWVzc2VuX2ljb25fMF81NDhiZDc0Yw/icon.png?w=170&fakeurl=1";
    private String threadImage = "https://img.icons8.com/cotton/2x/new-year-2021.png";
    private String footerImage = "https://pbs.twimg.com/profile_images/890398208681103360/JhKKwf5i_400x400.jpg";
    
    private final Map<String, String> shortDescription = new HashMap<>();
    private final Map<String, String> urlToDetail = new HashMap<>();
    
    @Override
    public String getCommandName() {
        return command;
    }

    @Override
    public Command getCommand(GatewayDiscordClient client) {
        return event -> event.getMessage().getChannel()
        .flatMap(channel -> channel.createEmbed(spec -> generateEmbed(spec, event.getMessage())))
        .then();
    }
    
    private void generateEmbed(EmbedCreateSpec spec, Message message) {
        Random rand = new Random();
        int upperbound = shortDescription.size();
        int randomEntryNumber = rand.nextInt(upperbound);
        
        String outcome = (String) shortDescription.keySet().toArray()[randomEntryNumber];
        String description = shortDescription.get(outcome);
        String url = urlToDetail.get(outcome);
        
        
        //message.getAuthorAsMember().toFuture().get().getDisplayName()
        //message.getAuthor().get().getUsername()
        //
        
        String user = null;
        try {
            user = message.getAuthorAsMember().toFuture().get().getDisplayName();
        } catch (Exception ex) {
            user = message.getAuthor().get().getUsername();
        }
        spec.setTitle(user + " - **" + outcome + "**");
        spec.setDescription(user + " hat **" + outcome + "** gegossen!\nBedeutung (kurz): " + description + "\n\nMehr Details unter:\n" + url);
        spec.setFooter("Powered by Naurandir", footerImage);
        spec.setColor(Color.GREEN);
        spec.setUrl(url);
        spec.setThumbnail(threadImage);
        spec.setImage(bleigiessenIcon);
    }
    
    @PostConstruct
    public void init() throws IOException {
        log.info("init: loading json with content...");
        
        Path file = Path.of(filePath);
        String fileInput = Files.readString(file);
        
        Gson gson = new Gson();
        String[] data = gson.fromJson(fileInput, String[].class);
        
        List<String> dataList = Arrays.asList(data);
        
        generateShortDescription(dataList);
        generateUrlToDetail(dataList);
        
        log.info("init: loading json with content done");
    }
    
    private void generateShortDescription(List<String> dataList) {
        for (String data : dataList) {
            String name = data.split(" ")[0];
            String description = data.substring(name.length()+1);
            
            log.debug("generateShortDescription: adding {} - {}", name, description);
            shortDescription.put(name, description);
        }
    }

    private void generateUrlToDetail(List<String> dataList) {
        for (String data : dataList) {
            String name = data.split(" ")[0];
            String url = getUrl(name);
            
            log.debug("generateUrlToDetail: adding {} - {}", name, url);
            urlToDetail.put(name, url);
        }
    }
    
    // Example: https://www.bleigiessen.de/f/fackel
    private String getUrl(String name) {
        return detailUrl + name.toLowerCase().charAt(0) + "/" +
                name.toLowerCase().replaceAll("ä", "ae")
                                  .replaceAll("ß", "ss")
                                  .replaceAll("ü", "ue")
                                  .replaceAll("ö", "oe");
    }
}
