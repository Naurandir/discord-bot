package com.discord.bot.silvester;

import com.discord.command.Command;
import com.discord.command.CommandGenerator;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    
    @Autowired
    private List<CommandGenerator> commandGenerators;
    
    private GatewayDiscordClient client;
    private final Map<String, Command> commands = new HashMap<>();
    
    @PostConstruct
    public void init() throws InterruptedException, ExecutionException {
        log.info("init: starting up bot...");
        client = DiscordClientBuilder.create(discordToken)
                .build()
                .login()
                .toFuture()
                .get();
        
        generatecommands(client);
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
    private void generatecommands(GatewayDiscordClient client) {
        commandGenerators.forEach(entry -> 
            commands.put(entry.getCommandName(), entry.getCommand(client)));
        commandGenerators.forEach(entry -> log.info("createCommands: created command [{}]", entry.getCommandName()));
        
        client.getEventDispatcher().on(MessageCreateEvent.class)
                .filter(event -> event.getMessage().getAuthor().isPresent() && !event.getMessage().getAuthor().get().isBot()) // ignore bots
                .flatMap(event -> Mono.just(event.getMessage().getContent())
                .flatMap(content -> Flux.fromIterable(commands.entrySet())
                .filter(entry -> content.startsWith(prefix + entry.getKey()))
                .flatMap(entry -> entry.getValue().execute(event))
                .next()))
                .subscribe();
    }
}
