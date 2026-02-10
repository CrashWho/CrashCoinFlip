package org.crashwho.crashCoinFlip.Utils.Inventory;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.crashwho.crashCoinFlip.CrashCoinFlip;
import org.crashwho.crashCoinFlip.Events.InputListener;
import org.crashwho.crashCoinFlip.Utils.Manager.FlipsManager;
import org.crashwho.crashCoinFlip.Utils.Messages.ChatFormat;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class InvManager {

    protected static ChestGui gui;
    protected static PaginatedPane pane;

    public static void init(CrashCoinFlip crashCoinFlip) {
        String title = LegacyComponentSerializer.legacySection().serialize(ChatFormat.format(crashCoinFlip.getMessages().getData().getString("gui.title")));
        int rows = crashCoinFlip.getConfig().getInt("settings.gui.rows");
        gui = new ChestGui(rows, title);
        gui.setOnGlobalClick(event -> event.setCancelled(true));

        int start_row = crashCoinFlip.getConfig().getInt("settings.gui.start-row");
        int start_column = crashCoinFlip.getConfig().getInt("settings.gui.start-column");
        int lenght = crashCoinFlip.getConfig().getInt("settings.gui.length");
        int use_row = crashCoinFlip.getConfig().getInt("settings.gui.items-row");

        pane = new PaginatedPane(start_column, start_row, lenght, use_row);


        gui.addPane(pane);
        addNavigation(crashCoinFlip);
        repopulate(crashCoinFlip);
        updateGui();
    }

    private static void addNavigation(CrashCoinFlip crashCoinFlip) {
        int rows = crashCoinFlip.getConfig().getInt("settings.gui.rows");
        StaticPane navPane = new StaticPane(0, rows - 1, 9, 1);

        Material back_material = Material.valueOf(crashCoinFlip.getMessages().getData().getString("gui.items.back.material"));
        Material next_material = Material.valueOf(crashCoinFlip.getMessages().getData().getString("gui.items.next.material"));

        ItemStack backItem = new ItemStack(back_material);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.displayName(ChatFormat.format("<italic:false>" + crashCoinFlip.getMessages().getData().getString("gui.items.back.displayname")));
        backMeta.setItemModel(NamespacedKey.fromString(crashCoinFlip.getMessages().getData().getString("gui.items.back.item-model")));
        backItem.setItemMeta(backMeta);

        navPane.addItem(new GuiItem(backItem, event -> {
            if (pane.getPage() > 0) {
                pane.setPage(pane.getPage() - 1);
                gui.update();
            }
        }), 0, 0);

        int createSlot = crashCoinFlip.getConfig().getInt("gui.create-item.slot", 4);

        navPane.addItem(new GuiItem(getCreationItem(crashCoinFlip), event -> {
            Player player = (Player) event.getWhoClicked();
            player.closeInventory();

            InputListener.addPlayer(player);
            player.getPlayer().sendMessage(ChatFormat.prefixFormat(crashCoinFlip.getMessages().getData().getString("msg.cf")));


        }), createSlot, 0);


        ItemStack nextItem = new ItemStack(next_material);
        ItemMeta nextMeta = nextItem.getItemMeta();
        nextMeta.displayName(ChatFormat.format("<italic:false>" + crashCoinFlip.getMessages().getData().getString("gui.items.next.displayname")));
        nextMeta.setItemModel(NamespacedKey.fromString(crashCoinFlip.getMessages().getData().getString("gui.items.next.item-model")));
        nextItem.setItemMeta(nextMeta);

        navPane.addItem(new GuiItem(nextItem, event -> {
            if (pane.getPage() < pane.getPages() - 1) {
                pane.setPage(pane.getPage() + 1);
                gui.update();
            }
        }), 8, 0);

        gui.addPane(navPane);
    }

    private static GuiItem createAddButton(long amount, Material mat, AtomicLong currentBet, Runnable updateTask) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(ChatFormat.format("&a&l+" + CrashCoinFlip.getEconomy().format(amount)));
        item.setItemMeta(meta);

        return new GuiItem(item, event -> {
            Player p = (Player) event.getWhoClicked();
            currentBet.addAndGet(amount); // Aggiunge i soldi
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            updateTask.run(); // Aggiorna la GUI
        });
    }

    public static void openGui(Player player) {
        getGui().show(player);
    }

    public static void updateGui() {
        getGui().update();
    }


    public static ChestGui getGui() {
        return gui;
    }

    public static void addGuiItem(CrashCoinFlip crashCoinFlip) {

        repopulate(crashCoinFlip);
        updateGui();

    }

    private static ItemStack getCreationItem(CrashCoinFlip crashCoinFlip) {

        String matName = crashCoinFlip.getMessages().getData().getString("gui.items.create-item.material");
        String name = crashCoinFlip.getMessages().getData().getString("gui.items.create-item.displayname");
        String item_model = crashCoinFlip.getMessages().getData().getString("gui.items.create-item.item-model");
        List<String> lore = crashCoinFlip.getMessages().getData().getStringList("gui.items.create-item.lore");

        Material material = Material.getMaterial(matName);
        if (material == null) material = Material.EMERALD; // Fallback

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(ChatFormat.format("<italic:false>" + name));

        List<Component> loreComponents = new ArrayList<>();
        for (String line : lore) {
            loreComponents.add(ChatFormat.format("<italic:false>" + line));
        }
        meta.lore(loreComponents);
        meta.setItemModel(NamespacedKey.fromString(item_model));

        item.setItemMeta(meta);
        return item;
    }

    private static GuiItem createGuiItem(CrashCoinFlip crashCoinFlip, OfflinePlayer player) {
        return new GuiItem(setupItem(crashCoinFlip, player), event -> {
            if (event.getWhoClicked() == player) {
                if (event.isLeftClick()) {
                    if(player.isOnline()) player.getPlayer().sendMessage(ChatFormat.prefixFormat(crashCoinFlip.getMessages().getData().getString("messages.self")));
                } else {
                    FlipsManager.cancelFlip(crashCoinFlip, player);
                    if(player.isOnline()) player.getPlayer().sendMessage(ChatFormat.prefixFormat(crashCoinFlip.getMessages().getData().getString("messages.cancel")));
                }
            } else {
                Player target = Bukkit.getPlayer(event.getWhoClicked().getUniqueId());
                int flip = new Random().nextInt(2);
                double amount = FlipsManager.getPendingFlipAmount(player.getUniqueId());

                if (!CrashCoinFlip.getEconomy().has(target, amount)) {
                    target.getPlayer().sendMessage(ChatFormat.prefixFormat(crashCoinFlip.getMessages().getData().getString("messages.not-enough-money")));
                    return;
                }

                CrashCoinFlip.getEconomy().withdrawPlayer(target, amount);

                if (flip == 0) {
                    // Win Logic
                    if(player.isOnline()) player.getPlayer().sendMessage(ChatFormat.prefixFormat(crashCoinFlip.getMessages().getData().getString("messages.win")));
                    target.sendMessage(ChatFormat.prefixFormat(crashCoinFlip.getMessages().getData().getString("messages.lose")));
                    CrashCoinFlip.getEconomy().depositPlayer(player, amount * 2);
                } else {
                    // Lose Logic
                    target.sendMessage(ChatFormat.prefixFormat(crashCoinFlip.getMessages().getData().getString("messages.win")));
                    if(player.isOnline()) player.getPlayer().sendMessage(ChatFormat.prefixFormat(crashCoinFlip.getMessages().getData().getString("messages.lose")));
                    CrashCoinFlip.getEconomy().depositPlayer(target, amount * 2);
                }

                FlipsManager.removePendingFlip(crashCoinFlip, player);
                target.closeInventory();
            }
        });
    }


    public static void removeGuiItem(CrashCoinFlip crashCoinFlip) {

        repopulate(crashCoinFlip);
        updateGui();

    }

    private static void repopulate(CrashCoinFlip crashCoinFlip) {
        pane.clear();

        List<GuiItem> items = new ArrayList<>();

        for (UUID uuid : FlipsManager.getAllPendingFlip()) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            items.add(createGuiItem(crashCoinFlip, player));
        }

        pane.populateWithGuiItems(items);
        updateGui();
    }

    private static ItemStack setupItem(CrashCoinFlip crashCoinFlip, OfflinePlayer player) {
        Material material = Material.valueOf(crashCoinFlip.getMessages().getData().getString("gui.items.coinflip.material"));
        ItemStack item = new ItemStack(material, 1);

        String display_name = crashCoinFlip.getMessages().getData().getString("gui.items.coinflip.displayname", player.getName())
                .replace("{player}", player.getName());
        String item_model = crashCoinFlip.getMessages().getData().getString("gui.items.coinflip.item-model", "");
        List<String> lore = crashCoinFlip.getMessages().getData().getStringList("gui.items.coinflip.lore");

        String amount = CrashCoinFlip.getEconomy().format(FlipsManager.getPendingFlipAmount(player.getUniqueId()));
        List<Component> lore_components = lore.stream()
                    .map(line -> ChatFormat.format("<italic:false>" + line
                            .replace("{player}", player.getName())
                            .replace("{amount}", amount)))
                    .toList();


        if (item.getItemMeta() instanceof SkullMeta meta) {
            meta.setOwningPlayer(player);
            meta.displayName(ChatFormat.format("<italic:false>" + display_name));
            meta.setItemModel(NamespacedKey.fromString(item_model));
            meta.lore(lore_components);
            item.setItemMeta(meta);

            return item;
        } else {
            ItemMeta meta = item.getItemMeta();
            meta.displayName(ChatFormat.format("<italic:false>" + display_name));
            meta.setItemModel(NamespacedKey.fromString(item_model));
            meta.lore(lore_components);
            item.setItemMeta(meta);

            return item;
        }

    }

}
