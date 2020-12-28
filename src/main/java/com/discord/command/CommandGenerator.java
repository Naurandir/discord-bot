package com.discord.command;

import discord4j.core.GatewayDiscordClient;

/**
 *
 * @author Naurandir
 */
public interface CommandGenerator {
    String getCommandName();
    Command getCommand(GatewayDiscordClient client);
}
