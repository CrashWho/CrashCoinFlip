package org.crashwho.crashCoinFlip.Utils.Manager;

import org.bukkit.OfflinePlayer;
import org.crashwho.crashCoinFlip.CrashCoinFlip;
import org.crashwho.crashCoinFlip.Utils.Inventory.InvManager;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class FlipsManager {

    private static final HashMap<UUID, Double> pendingFlips = new HashMap<>();

    public static void addPendingFlip(CrashCoinFlip crashCoinFlip, OfflinePlayer player, double amount) {
        pendingFlips.put(player.getUniqueId(), amount);
        InvManager.addGuiItem(crashCoinFlip);
        CrashCoinFlip.getEconomy().withdrawPlayer(player, amount);
    }

    public static void removePendingFlip(CrashCoinFlip crashCoinFlip, OfflinePlayer player) {
        pendingFlips.remove(player.getUniqueId());
        InvManager.removeGuiItem(crashCoinFlip);
    }

    public static void cancelFlip(CrashCoinFlip crashCoinFlip, OfflinePlayer player) {

        double amount = getPendingFlipAmount(player.getUniqueId());
        CrashCoinFlip.getEconomy().depositPlayer(player, amount);
        removePendingFlip(crashCoinFlip, player);
    }

    public static boolean hasPendingFlip(UUID playerId) {
        return pendingFlips.containsKey(playerId);
    }

    public static double getPendingFlipAmount(UUID playerId) {
        return  pendingFlips.get(playerId);
    }

    public static Set<UUID> getAllPendingFlip() {
        return pendingFlips.keySet();
    }


}
