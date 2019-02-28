/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.commandlogger.listeners;

import com.google.common.collect.ImmutableSet;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.interfaces.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.commandlogger.config.CommandLoggerConfig;
import io.github.nucleuspowered.nucleus.modules.commandlogger.config.CommandLoggerConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.commandlogger.services.CommandLoggerHandler;
import io.github.nucleuspowered.nucleus.util.CommandNameCache;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.CommandBlockSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class CommandLoggingListener implements Reloadable, ListenerBase {

    private final CommandLoggerHandler handler = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(CommandLoggerHandler.class);
    private CommandLoggerConfig c = new CommandLoggerConfig();
    private Set<String> commandsToFilter = new HashSet<>();

    @Listener(order = Order.LAST)
    public void onCommand(SendCommandEvent event, @First CommandSource source) {
        // Check source.
        boolean accept;
        if (source instanceof Player) {
            accept = this.c.getLoggerTarget().isLogPlayer();
        } else if (source instanceof CommandBlockSource) {
            accept = this.c.getLoggerTarget().isLogCommandBlock();
        } else if (source instanceof ConsoleSource) {
            accept = this.c.getLoggerTarget().isLogConsole();
        } else {
            accept = this.c.getLoggerTarget().isLogOther();
        }

        if (!accept) {
            // We're not logging this!
            return;
        }

        String command = event.getCommand().toLowerCase();
        Set<String> commands = CommandNameCache.INSTANCE.getFromCommandAndSource(command, source);
        commands.retainAll(this.commandsToFilter);

        // If whitelist, and we have the command, or if not blacklist, and we do not have the command.
        if (this.c.isWhitelist() == !commands.isEmpty()) {
            String message = Nucleus.getNucleus().getMessageProvider().getMessageWithFormat("commandlog.message", source.getName(), event.getCommand(), event.getArguments());
            Nucleus.getNucleus().getLogger().info(message);
            this.handler.queueEntry(message);
        }
    }

    @Override
    public void onReload() {
        this.c = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(CommandLoggerConfigAdapter.class).getNodeOrDefault();
        this.commandsToFilter = this.c.getCommandsToFilter().stream().map(String::toLowerCase).collect(ImmutableSet.toImmutableSet());
    }

    @Listener
    public void onShutdown(GameStoppedServerEvent event) {
        try {
            this.handler.onServerShutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
