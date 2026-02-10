package org.crashwho.crashCoinFlip.Events;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.crashwho.crashCoinFlip.CrashCoinFlip;
import org.crashwho.crashCoinFlip.Utils.Messages.ChatFormat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class InputListener {

    final CrashCoinFlip crashCoinFlip;
    private static final Set<UUID> awaitingInput = new HashSet<>();

    public InputListener(CrashCoinFlip crashCoinFlip) {
        this.crashCoinFlip = crashCoinFlip;
        registerPacketListener();
    }

    public static void addPlayer(Player player) {
        awaitingInput.add(player.getUniqueId());
    }

    public static void removePlayer(Player player) {
        awaitingInput.remove(player.getUniqueId());
    }

    public static boolean hasPlayer(Player player) {
        return awaitingInput.contains(player.getUniqueId());
    }

    private void registerPacketListener() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(
                crashCoinFlip,
                PacketType.Play.Client.CHAT) {

            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();

                if (!awaitingInput.contains(player.getUniqueId())) return;

                event.setCancelled(true);

                String message = event.getPacket().getStrings().read(0);

                Bukkit.getScheduler().runTask(crashCoinFlip, () -> handleInput(player, message));


            }

        });

    }

    private void handleInput(Player player, String message) {

        String cleanMessage = message.replace(",", "").trim();

        if (cleanMessage.equalsIgnoreCase(crashCoinFlip.getConfig().getString("settings.cancel-word"))) {
            removePlayer(player);
            player.sendMessage(ChatFormat.prefixFormat("&cOperazione annullata."));
            return;
        }

        // Parsing numero
        double amount = ChatFormat.parseAmount(cleanMessage);

        if (amount <= 0) {
            player.sendMessage(ChatFormat.prefixFormat("&cImporto non valido! Riprova o scrivi 'annulla'."));
            return;
        }

        removePlayer(player);
        long fixedAmount = (long) amount;
        player.performCommand("cf " + fixedAmount);
    }
}
