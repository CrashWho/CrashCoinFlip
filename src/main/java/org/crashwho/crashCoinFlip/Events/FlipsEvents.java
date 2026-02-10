package org.crashwho.crashCoinFlip.Events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.crashwho.crashCoinFlip.CrashCoinFlip;
import org.crashwho.crashCoinFlip.Utils.Manager.FlipsManager;

public class FlipsEvents implements Listener {

    final CrashCoinFlip crashCoinFlip;

    public FlipsEvents(CrashCoinFlip crashCoinFlip) {
        this.crashCoinFlip = crashCoinFlip;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {

        if (FlipsManager.hasPendingFlip(e.getPlayer().getUniqueId())) {
            FlipsManager.cancelFlip(crashCoinFlip, e.getPlayer());

            if (crashCoinFlip.getConfig().getBoolean("info.console-log-on-quit"))
                crashCoinFlip.getLogger().info("Il giocatore " + e.getPlayer().getName() + " aveva una scommessa in corso, uscendo è stata rimossa.");
        }

        if (InputListener.hasPlayer(e.getPlayer()))
            InputListener.removePlayer(e.getPlayer());

    }
}
