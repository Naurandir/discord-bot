package com.discord.command;

import discord4j.core.GatewayDiscordClient;

/**
 *
 * @author Naurandir
 */
public interface CommandGenerator {
    Command getCommand();
}
