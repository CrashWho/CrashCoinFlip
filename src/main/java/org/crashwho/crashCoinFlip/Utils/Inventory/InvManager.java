package org.crashwho.crashCoinFlip.Utils.Inventory;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import io.papermc.paper.datacomponent.item.ItemLore;
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

        int rows = crashCoinFlip.getConfig().getInt("settings.navbar-gui.rows");
        int start_column = crashCoinFlip.getConfig().getInt("settings.navbar-gui.start-column");
        int lenght = crashCoinFlip.getConfig().getInt("settings.navbar-gui.length");
        int use_row = crashCoinFlip.getConfig().getInt("settings.navbar-gui.items-row");

        StaticPane navPane = new StaticPane(start_column, rows, lenght, use_row);

        Material back_material = Material.valueOf(crashCoinFlip.getMessages().getData().getString("gui.items.back.material"));
        Material next_material = Material.valueOf(crashCoinFlip.getMessages().getData().getString("gui.items.next.material"));
        String backDisplayName = crashCoinFlip.getMessages().getData().getString("gui.items.back.displayname");
        NamespacedKey backModel = NamespacedKey.fromString(crashCoinFlip.getMessages().getData().getString("gui.items.back.item-model", "coinflip"));
        float backCustomModelData = crashCoinFlip.getMessages().getData().getInt("gui.items.back.custom-model-data");
        List<String> backLore = crashCoinFlip.getMessages().getData().getStringList("gui.items.back.lore");


        ItemStack backItem = itemCreation(back_material, backDisplayName, createLore(backLore), backModel, backCustomModelData);

        int backSlot = crashCoinFlip.getMessages().getData().getInt("gui.items.back.slot");

        navPane.addItem(new GuiItem(backItem, event -> {
            if (pane.getPage() > 0) {
                pane.setPage(pane.getPage() - 1);
                gui.update();
            }
        }), Slot.fromIndex(backSlot));

        int createSlot = crashCoinFlip.getMessages().getData().getInt("gui.items.create-item.slot", 4);

        navPane.addItem(new GuiItem(getCreationItem(crashCoinFlip), event -> {
            Player player = (Player) event.getWhoClicked();
            player.closeInventory();

            InputListener.addPlayer(player);
            player.getPlayer().sendMessage(ChatFormat.prefixFormat(crashCoinFlip.getMessages().getData().getString("msg.cf")));


        }), Slot.fromIndex(createSlot));


        String nextDisplayName = crashCoinFlip.getMessages().getData().getString("gui.items.next.displayname");
        List<String> nextLore = crashCoinFlip.getMessages().getData().getStringList("gui.items.next.lore");
        NamespacedKey nextModel = NamespacedKey.fromString(crashCoinFlip.getMessages().getData().getString("gui.items.next.item-model", "coinflip"));
        float nextCustomModelData = crashCoinFlip.getMessages().getData().getInt("gui.items.next.custom-model-data");

        ItemStack nextItem = itemCreation(next_material, nextDisplayName, createLore(nextLore), nextModel, nextCustomModelData);


        int nextSlot = crashCoinFlip.getMessages().getData().getInt("gui.items.next.slot");


        navPane.addItem(new GuiItem(nextItem, event -> {
            if (pane.getPage() < pane.getPages() - 1) {
                pane.setPage(pane.getPage() + 1);
                gui.update();
            }
        }), Slot.fromIndex(nextSlot));

        gui.addPane(navPane);
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

    private static List<Component> createLore(List<String> lore) {

        List<Component> loreComponents = new ArrayList<>();
        lore.forEach(line -> loreComponents.add(ChatFormat.format("<italic:false>" + line)));


        return loreComponents;
    }

    private static ItemStack itemCreation(Material material, String displayname, List<Component> lore, NamespacedKey key, float data) {

        ItemStack item = new ItemStack(material);

        if (displayname != null)
            item.setData(DataComponentTypes.CUSTOM_NAME, ChatFormat.format("<italic:false>" + displayname));

        if (lore != null && !lore.isEmpty())
            item.setData(DataComponentTypes.LORE, ItemLore.lore(lore));

        if (key != null)
            item.setData(DataComponentTypes.ITEM_MODEL, key);

        if (key != null)
            item.setData(DataComponentTypes.ITEM_MODEL, key);

        if (data != 0) {
            var builder = CustomModelData.customModelData();
            if (builder != null)
                item.setData(DataComponentTypes.CUSTOM_MODEL_DATA, builder.addFloat(data).build());
        }


        return item;
    }

    private static ItemStack getCreationItem(CrashCoinFlip crashCoinFlip) {

        String matName = crashCoinFlip.getMessages().getData().getString("gui.items.create-item.material");
        String name = crashCoinFlip.getMessages().getData().getString("gui.items.create-item.displayname");
        String item_model = crashCoinFlip.getMessages().getData().getString("gui.items.create-item.item-model", "coinflip");
        List<String> lore = crashCoinFlip.getMessages().getData().getStringList("gui.items.create-item.lore");
        float data = crashCoinFlip.getMessages().getData().getInt("gui.items.custom-model-data");


        return itemCreation(Material.valueOf(matName), name, createLore(lore), NamespacedKey.fromString(item_model), data);
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

        String display_name = crashCoinFlip.getMessages().getData().getString("gui.items.coinflip.displayname", player.getName())
                .replace("{player}", player.getName());
        NamespacedKey key = NamespacedKey.fromString(crashCoinFlip.getMessages().getData().getString("gui.items.coinflip.item-model", "coinflip"));
        List<String> lore = crashCoinFlip.getMessages().getData().getStringList("gui.items.coinflip.lore");
        float modelData = crashCoinFlip.getMessages().getData().getInt("gui.items.coinflip.model-data");

        String amount = CrashCoinFlip.getEconomy().format(FlipsManager.getPendingFlipAmount(player.getUniqueId()));
        List<Component> lore_components = lore.stream()
                .map(line -> ChatFormat.format("<italic:false>" + line
                        .replace("{player}", player.getName())
                        .replace("{amount}", amount)))
                .toList();


        ItemStack item = itemCreation(material, display_name, lore_components, key, modelData);

        if (item.getItemMeta() instanceof SkullMeta meta) {
            meta.setOwningPlayer(player);
            meta.displayName(ChatFormat.format("<italic:false>" + display_name));
            meta.lore(lore_components);
            item.setItemMeta(meta);

            return item;
        } else {
            ItemMeta meta = item.getItemMeta();
            meta.displayName(ChatFormat.format("<italic:false>" + display_name));
            meta.lore(lore_components);
            item.setItemMeta(meta);

            return item;
        }

    }

}
