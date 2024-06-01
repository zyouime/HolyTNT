package net.ihor.holytnt;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommandEx implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            List<String> lore = new ArrayList<>();
            ItemStack tnt = new ItemStack(Material.TNT);
            tnt.setAmount(64);
            ItemMeta tntmeta = tnt.getItemMeta();
            tntmeta.setDisplayName(ColorUtil.msg("&x&f&f&0&0&b&fC&x&e&e&1&4&c&44 &x&d&d&2&9&c&aВ&x&c&b&3&d&c&fз&x&b&a&5&1&d&5Р&x&a&9&6&5&d&aы&x&9&8&7&a&e&0В&x&8&7&8&e&e&5ч&x&7&6&a&2&e&bА&x&6&4&b&6&f&0т&x&5&3&c&b&f&6К&x&4&2&d&f&f&bа"));
            lore.add(ColorUtil.msg("&6&lTNT"));
            tntmeta.setLore(lore);
            tnt.setItemMeta(tntmeta);
            player.getInventory().addItem(tnt);
        }
        return true;
    }

}
