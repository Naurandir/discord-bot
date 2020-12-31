package com.discord.bot.silvester;

import com.discord.command.Command;
import com.discord.command.CommandGenerator;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.discordjson.json.ActivityUpdateRequest;
import discord4j.discordjson.json.gateway.StatusUpdate;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
/**
 *
 * @author Naurandir
 */
@Slf4j
@Component
public class SilvesterBot {
    
    @Value( "${discord.bot.silvester.token}" )
    private String discordToken;
    
    @Value( "${discord.bot.silvester.command.prefix}" )
    private String prefix;
    
    @Value( "${discord.bot.silvester.next-year}" )
    private Integer nextYear;
    
    @Autowired
    private List<CommandGenerator> commandGenerators;
    
    private GatewayDiscordClient client;
    private final Map<String, Command> commands = new HashMap<>();
    
    @PostConstruct
    public void init() throws InterruptedException, ExecutionException {
        log.info("init: starting up bot...");
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Vienna"));// set time zone for Vienna
        
        client = DiscordClientBuilder.create(discordToken)
                .build()
                .gateway()
                .setInitialStatus(shard -> Presence.online(Activity.playing("!bleigiessen")))
                .login()
                .toFuture()
                .get();
        
        generateCommands(client);
        log.info("init: starting up bot done");
    }
    
    @PreDestroy
    public void destroy () {
        log.info("destroy: destroying bot...");
        client.logout();
        log.info("destroy: destroying bot done");
    }

    /**
     * create commands that are used in this bot and add them
     */
    private void generateCommands(GatewayDiscordClient client) {
        commandGenerators.forEach(entry -> 
            commands.put(entry.getCommand().getCommandWord(), entry.getCommand()));
        commandGenerators.forEach(entry -> log.info("createCommands: created command [{}]", entry.getCommand().getCommandWord()));
        
        client.getEventDispatcher().on(MessageCreateEvent.class)
                .filter(event -> event.getMessage().getAuthor().isPresent() && !event.getMessage().getAuthor().get().isBot()) // ignore bots
                .flatMap(event -> Mono.just(event.getMessage().getContent())
                .flatMap(content -> Flux.fromIterable(commands.entrySet())
                .filter(entry -> content.startsWith(prefix + entry.getKey()))
                .flatMap(entry -> entry.getValue().execute(event))
                .next()))
                .subscribe();
    }
    
    @Scheduled(fixedRate = 15_000)
    public void updateStatusForSilvester() throws InterruptedException, ExecutionException {
        String statusBeforeSilvester = "!bleigiessen - {days}d {hours}h {minutes}m {seconds}s till new year.";
        String statusAfterSilvester = "!bleigiessen - Happy new Year!";
        LocalDateTime silvester = LocalDateTime.of(nextYear, Month.JANUARY, 1, 0, 0, 0);
        LocalDateTime now = LocalDateTime.now();
        
        String status;
        if (now.isBefore(silvester)) {
            // we only use days nothing above
            long daysBetween = ChronoUnit.DAYS.between(now, silvester);
            long hoursBetween = ChronoUnit.HOURS.between(now, silvester) - daysBetween*24;
            long minutesBetween = ChronoUnit.MINUTES.between(now, silvester) - daysBetween*24*60 - hoursBetween*60;
            long secondsBetween = ChronoUnit.SECONDS.between(now, silvester) - daysBetween*24*60*60 - hoursBetween*60*60 - minutesBetween*60;
            
            status = statusBeforeSilvester
                        .replace("{days}", Long.toString(daysBetween))
                        .replace("{hours}", Long.toString(hoursBetween))
                        .replace("{minutes}", Long.toString(minutesBetween))
                        .replace("{seconds}", Long.toString(secondsBetween));
        } else {
            status = statusAfterSilvester;
        }
        
        client.updatePresence(Presence.online(Activity.playing(status)))
                .toFuture()
                .get();
    }
}
