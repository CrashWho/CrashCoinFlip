package org.crashwho.crashCoinFlip;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.crashwho.crashCoinFlip.Commands.cf;
import org.crashwho.crashCoinFlip.Events.FlipsEvents;
import org.crashwho.crashCoinFlip.Events.InputListener;
import org.crashwho.crashCoinFlip.Utils.Files.FileManager;
import org.crashwho.crashCoinFlip.Utils.Inventory.InvManager;
import org.crashwho.crashCoinFlip.Utils.Manager.FlipsManager;
import org.crashwho.crashCoinFlip.Utils.Messages.ChatFormat;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CrashCoinFlip extends JavaPlugin {

    private static Economy econ = null;
    private FileManager messages;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        messages = new FileManager(this, "messages");
        ChatFormat.init(this);
        InvManager.init(this);

        if (getServer().getPluginManager().getPlugin("ProtocolLib") != null) {
            new InputListener(this);
        } else {
            getLogger().severe("ProtocolLib non trovato! Il sistema di input non funzionerà.");
            getServer().getPluginManager().disablePlugin(this);
        }


        if (!setupEconomy() ) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Lamp<BukkitCommandActor> lamp = BukkitLamp.builder(this).build();
        lamp.register(new cf(this));
        getServer().getPluginManager().registerEvents(new FlipsEvents(this), this);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    @Override
    public void onDisable() {

        List<UUID> Remove = new ArrayList<>(FlipsManager.getAllPendingFlip());

        if (Remove.isEmpty())
            return;

        Remove.forEach(uuid -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            FlipsManager.cancelFlip(this, player);


        });

    }

    public static Economy getEconomy() {
        return econ;
    }

    public FileManager getMessages() {
            return messages;
        }



}
