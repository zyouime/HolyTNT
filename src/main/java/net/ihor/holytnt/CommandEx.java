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
            lore.add(ColorUtil.msg("&6&lTNT"));
            tntmeta.setLore(lore);
            tnt.setItemMeta(tntmeta);
            player.getInventory().addItem(tnt);
        }
        return true;
    }

}
