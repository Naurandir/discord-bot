package com.discord.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

/**
 *
 * @author Naurandir
 */
public interface Command {
    Mono<Void> execute(MessageCreateEvent event);
}
