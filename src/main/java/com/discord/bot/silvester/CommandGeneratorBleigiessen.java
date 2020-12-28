package com.discord.bot.silvester;

import com.discord.command.Command;
import com.discord.command.CommandGenerator;
import com.google.gson.Gson;
import discord4j.core.GatewayDiscordClient;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Component
public class CommandGeneratorBleigiessen implements CommandGenerator {
    
    @Value( "${discord.bot.silvester.command.bleigiessen}" )
    private String commandName;
    
    @Value( "${discord.bot.silvester.file.bleigiessen}" )
    private String filePath;
    
    @Value( "${discord.bot.silvester.detail.bleigiessen}" )
    private String detailUrl;
    
    @Value( "${discord.bot.silvester.icon.bleigiessen}" )
    private String bleigiessenIcon;
    
    @Value( "${discord.bot.silvester.image.bleigiessen}" )
    private String threadImage;
    
    @Value( "${discord.bot.silvester.footer-image.bleigiessen}" )
    private String footerImage;
    
    private Command command;

    @Override
    public Command getCommand() {
        return command;
    }
    
    @PostConstruct
    public void init() throws IOException {
        log.info("init: loading json with content...");
        
        Path file = Path.of(filePath);
        String fileInput = Files.readString(file);
        
        Gson gson = new Gson();
        String[] data = gson.fromJson(fileInput, String[].class);
        
        List<String> dataList = Arrays.asList(data);
        
        Map<String, String> shortDescription = generateShortDescription(dataList);
        Map<String, String> urlToDetail = generateUrlToDetail(dataList);
        
        command = new CommandImplBleigiessen(commandName, bleigiessenIcon, threadImage, footerImage, shortDescription, urlToDetail);
        
        log.info("init: loading json with content done");
    }
    
    private Map<String, String> generateShortDescription(List<String> dataList) {
        Map<String, String> shortDescription = new HashMap<>();
        
        for (String data : dataList) {
            String name = data.split(" ")[0];
            String description = data.substring(name.length()+1);
            
            log.debug("generateShortDescription: adding {} - {}", name, description);
            shortDescription.put(name, description);
        }
        
        return shortDescription;
    }

    private Map<String, String> generateUrlToDetail(List<String> dataList) {
        Map<String, String> urlToDetail = new HashMap<>();
        
        for (String data : dataList) {
            String name = data.split(" ")[0];
            String url = getUrl(name);
            
            log.debug("generateUrlToDetail: adding {} - {}", name, url);
            urlToDetail.put(name, url);
        }
        
        return urlToDetail;
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
