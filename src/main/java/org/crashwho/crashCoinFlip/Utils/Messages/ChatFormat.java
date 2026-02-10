package org.crashwho.crashCoinFlip.Utils.Messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;
import org.crashwho.crashCoinFlip.CrashCoinFlip;

import java.io.File;

public class ChatFormat {

    private static String prefix = "";

    public static void init(CrashCoinFlip plugin) {
        File file = new File(plugin.getDataFolder(), "messages.yml");

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        prefix = config.getString("messages.prefix", "");
    }


    public static Component prefixFormat(String message) {

        return MiniMessage.miniMessage().deserialize(prefix + message);
    }

    public static Component format(String message) {

        return MiniMessage.miniMessage().deserialize(message);
    }

    public static double parseAmount(String input) {
        if (input == null) return -1;

        input = MiniMessage.miniMessage().stripTags(input).toLowerCase().trim().replace(",", "");

        if (input.matches("^[0-9]+(\\.[0-9]+)?$")) {
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        double multiplier = 1.0;
        if (input.endsWith("k")) multiplier = 1_000.0;
        else if (input.endsWith("m")) multiplier = 1_000_000.0;
        else if (input.endsWith("b")) multiplier = 1_000_000_000.0;
        else if (input.endsWith("q")) multiplier = 1_000_000_000_000.0;

        if (multiplier > 1.0) {
            try {
                String numericPart = input.substring(0, input.length() - 1);
                return Double.parseDouble(numericPart) * multiplier;
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        return -1;
    }
}
