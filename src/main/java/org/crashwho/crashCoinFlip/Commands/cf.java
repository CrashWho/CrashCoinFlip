package org.crashwho.crashCoinFlip.Commands;

import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import org.bukkit.entity.Player;
import org.crashwho.crashCoinFlip.CrashCoinFlip;
import org.crashwho.crashCoinFlip.Utils.Inventory.InvManager;
import org.crashwho.crashCoinFlip.Utils.Manager.FlipsManager;
import org.crashwho.crashCoinFlip.Utils.Messages.ChatFormat;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Command({"cf", "coinflip"})
public class cf {

    private final CrashCoinFlip crashCoinFlip;


    public cf(CrashCoinFlip crashCoinFlip) {
        this.crashCoinFlip = crashCoinFlip;
    }

    @CommandPlaceholder
    @CommandPermission("crashcoinflip.use")
    public void onCoinFlip(BukkitCommandActor actor) {

        if (!actor.isPlayer()) {
            actor.reply(ChatFormat.prefixFormat(crashCoinFlip.getMessages().getData().getString("messages.console")));
            return;
        }

        Player player = actor.requirePlayer();
        InvManager.openGui(player);

    }

    @CommandPlaceholder
    @CommandPermission("crashcoinflip.use")
    public void onCoinFlip(BukkitCommandActor actor, String amount) {

        if (!actor.isPlayer()) {
            actor.reply(ChatFormat.prefixFormat(crashCoinFlip.getMessages().getData().getString("messages.console")));
            return;
        }

        Player player = actor.requirePlayer();
        double amt = ChatFormat.parseAmount(amount);

        if (FlipsManager.hasPendingFlip(player.getUniqueId())) {
            actor.reply(ChatFormat.prefixFormat(crashCoinFlip.getMessages().getData().getString("messages.already")));
            return;
        }

        if (amt <= 0) {
            actor.reply(ChatFormat.prefixFormat(crashCoinFlip.getMessages().getData().getString("messages.invalid-amount")));
            return;
        }

        if (amt >= crashCoinFlip.getConfig().getInt("settings.max-bet") ) {
            String max = CrashCoinFlip.getEconomy().format(crashCoinFlip.getConfig().getInt("settings.max-bet"));
            actor.reply(ChatFormat.prefixFormat(crashCoinFlip.getMessages().getData().getString("messages.max-bet").replace("{max}", max)));
            return;
        }

        if (!CrashCoinFlip.getEconomy().has(player, amt)) {
            actor.reply(ChatFormat.prefixFormat(crashCoinFlip.getMessages().getData().getString("messages.not-enough-money")));
            return;
        }

        FlipsManager.addPendingFlip(crashCoinFlip, player, amt);

        String amount_fixed = CrashCoinFlip.getEconomy().format(amt);
        player.sendMessage(ChatFormat.prefixFormat(crashCoinFlip.getMessages().getData().getString("messages.create").replace("{amount}", amount_fixed)));

    }

    @Subcommand("cancel")
    @CommandPermission("crashcoinflip.cancel")
    public void onCancel(BukkitCommandActor actor) {

        if (!actor.isPlayer()) {
            actor.reply(ChatFormat.prefixFormat(crashCoinFlip.getMessages().getData().getString("messages.console")));
            return;
        }
        Player player = actor.requirePlayer();

        if (FlipsManager.hasPendingFlip(player.getUniqueId())) {
            FlipsManager.cancelFlip(crashCoinFlip, player);
            actor.reply(ChatFormat.prefixFormat(crashCoinFlip.getMessages().getData().getString("messages.cancel")));

        } else
            actor.reply(ChatFormat.prefixFormat(crashCoinFlip.getMessages().getData().getString("messages.error-cancel")));

    }

    @Subcommand("reload")
    @CommandPermission("crashcoinflip.reload")
    public void onReload(BukkitCommandActor actor) {
        crashCoinFlip.reloadConfig();
        crashCoinFlip.getMessages().reloadFile();
        ChatFormat.init(crashCoinFlip);
        InvManager.init(crashCoinFlip);




        actor.reply(ChatFormat.prefixFormat(crashCoinFlip.getMessages().getData().getString("messages.reload")));

    }

}
