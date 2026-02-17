package org.crashwho.crashCoinFlip.Commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.crashwho.crashCoinFlip.CrashCoinFlip;
import org.crashwho.crashCoinFlip.Utils.Inventory.InvManager;
import org.crashwho.crashCoinFlip.Utils.Manager.FlipsManager;
import org.crashwho.crashCoinFlip.Utils.Messages.ChatFormat;


public class cf {

    private final CrashCoinFlip crashCoinFlip;


    public cf(CrashCoinFlip crashCoinFlip) {
        this.crashCoinFlip = crashCoinFlip;
    }

    public LiteralCommandNode<CommandSourceStack> coinFlipCommand() {
        return Commands.literal("coinflip")
                .requires(sender -> sender.getSender().hasPermission("crashcoinflip.use"))
                .executes(this::guiOpen)

                .then(Commands.argument("amount", StringArgumentType.word())
                        .executes(this::createCoinFlip))

                .then(Commands.literal("cancel")
                        .requires(sender -> sender.getSender().hasPermission("crashcoinflip.cancel"))
                        .executes(this::cancelCoinFlip))

                .then(Commands.literal("reload")
                        .requires(sender -> sender.getSender().hasPermission("crashcoinflip.reload"))
                        .executes(this::reloadCoinFlip))

                .build();
    }

    private int guiOpen(CommandContext<CommandSourceStack> ctx) {

        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
            ctx.getSource().getSender().sendMessage(ChatFormat.prefixFormat(crashCoinFlip.getMessages().getData().getString("messages.console")));
            return Command.SINGLE_SUCCESS;
        }

        InvManager.openGui(player);
        return Command.SINGLE_SUCCESS;
    }

    private int createCoinFlip(CommandContext<CommandSourceStack> ctx) {

        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
            ctx.getSource().getSender().sendMessage(ChatFormat.prefixFormat(crashCoinFlip.getMessages().getData().getString("messages.console")));
            return Command.SINGLE_SUCCESS;
        }

        String input = StringArgumentType.getString(ctx, "amount");
        double amt = ChatFormat.parseAmount(input);

        if (FlipsManager.hasPendingFlip(player.getUniqueId())) {
            player.sendMessage(ChatFormat.prefixFormat(crashCoinFlip.getMessages().getData().getString("messages.already")));
            return Command.SINGLE_SUCCESS;
        }

        if (amt <= 0) {
            player.sendMessage(ChatFormat.prefixFormat(crashCoinFlip.getMessages().getData().getString("messages.invalid-amount")));
            return Command.SINGLE_SUCCESS;
        }

        if (amt >= crashCoinFlip.getConfig().getInt("settings.max-bet") ) {
            String max = CrashCoinFlip.getEconomy().format(crashCoinFlip.getConfig().getInt("settings.max-bet"));
            player.sendMessage(ChatFormat.prefixFormat(crashCoinFlip.getMessages().getData().getString("messages.max-bet").replace("{max}", max)));
            return Command.SINGLE_SUCCESS;
        }

        if (!CrashCoinFlip.getEconomy().has(player, amt)) {
            player.sendMessage(ChatFormat.prefixFormat(crashCoinFlip.getMessages().getData().getString("messages.not-enough-money")));
            return Command.SINGLE_SUCCESS;
        }

        FlipsManager.addPendingFlip(crashCoinFlip, player, amt);

        String amount_fixed = CrashCoinFlip.getEconomy().format(amt);
        player.sendMessage(ChatFormat.prefixFormat(crashCoinFlip.getMessages().getData().getString("messages.create").replace("{amount}", amount_fixed)));

        return Command.SINGLE_SUCCESS;
    }

    private int cancelCoinFlip(CommandContext<CommandSourceStack> ctx) {

        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
            ctx.getSource().getSender().sendMessage(ChatFormat.prefixFormat(crashCoinFlip.getMessages().getData().getString("messages.console")));
            return Command.SINGLE_SUCCESS;
        }

        if (FlipsManager.hasPendingFlip(player.getUniqueId())) {
            FlipsManager.cancelFlip(crashCoinFlip, player);
            player.sendMessage(ChatFormat.prefixFormat(crashCoinFlip.getMessages().getData().getString("messages.cancel")));
            return Command.SINGLE_SUCCESS;
        } else {
            player.sendMessage(ChatFormat.prefixFormat(crashCoinFlip.getMessages().getData().getString("messages.error-cancel")));
            return Command.SINGLE_SUCCESS;
        }
    }

    private int reloadCoinFlip(CommandContext<CommandSourceStack> ctx) {
        crashCoinFlip.reloadConfig();
        crashCoinFlip.getMessages().reloadFile();
        ChatFormat.init(crashCoinFlip);
        InvManager.init(crashCoinFlip);

        ctx.getSource().getSender().sendMessage(ChatFormat.prefixFormat(crashCoinFlip.getMessages().getData().getString("messages.reload")));
        return Command.SINGLE_SUCCESS;
    }

}
