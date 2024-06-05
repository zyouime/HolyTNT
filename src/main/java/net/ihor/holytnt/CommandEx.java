package net.ihor.holytnt;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class CommandEx implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            ItemStack tntC4 = new ItemStack(Material.TNT);
            ItemStack tntB = new ItemStack(Material.TNT);
            ItemStack tntA = new ItemStack(Material.TNT);
            ItemStack tntRV = new ItemStack(Material.TNT);
            ItemStack tntLV = new ItemStack(Material.TNT);
            tntC4.setAmount(64);
            tntB.setAmount(64);
            tntA.setAmount(64);
            tntLV.setAmount(64);
            tntRV.setAmount(64);
            ItemMeta tntmetaLV = tntLV.getItemMeta();
            ItemMeta tntmetaRV = tntRV.getItemMeta();
            ItemMeta tntmetaA = tntA.getItemMeta();
            ItemMeta tntmetaB = tntB.getItemMeta();
            ItemMeta tntmetaC4 = tntC4.getItemMeta();
            tntmetaC4.setDisplayName(ColorUtil.msg("&x&f&f&0&0&b&fC&x&e&e&1&4&c&44 &x&d&d&2&9&c&aВ&x&c&b&3&d&c&fз&x&b&a&5&1&d&5Р&x&a&9&6&5&d&aы&x&9&8&7&a&e&0В&x&8&7&8&e&e&5ч&x&7&6&a&2&e&bА&x&6&4&b&6&f&0т&x&5&3&c&b&f&6К&x&4&2&d&f&f&bа"));
            tntmetaC4.getPersistentDataContainer().set(NamespacedKey.minecraft("customtntc4"), PersistentDataType.INTEGER, 123);
            tntmetaA.setDisplayName(ColorUtil.msg("&x&f&f&9&1&0&0Д&x&f&8&8&a&0&0и&x&f&2&8&2&0&0н&x&e&b&7&b&0&0а&x&e&4&7&4&0&0м&x&d&d&6&d&0&0и&x&d&7&6&5&0&0т &x&d&0&5&e&0&0А"));
            tntmetaA.getPersistentDataContainer().set(NamespacedKey.minecraft("customtnta"), PersistentDataType.INTEGER, 123);
            tntmetaB.setDisplayName(ColorUtil.msg("&x&f&f&0&0&f&cД&x&f&7&0&0&f&3и&x&e&f&0&0&e&9н&x&e&7&0&0&e&0а&x&e&0&0&0&d&6м&x&d&8&0&0&c&dи&x&d&0&0&0&c&3т &x&c&8&0&0&b&aB"));
            tntmetaB.getPersistentDataContainer().set(NamespacedKey.minecraft("customtntb"), PersistentDataType.INTEGER, 123);
            tntmetaRV.setDisplayName(ColorUtil.msg("&x&f&f&0&0&0&0Р&x&f&e&0&0&1&1а&x&f&d&0&0&2&2з&x&f&b&0&0&3&3р&x&f&a&0&0&4&4ы&x&f&9&0&0&5&5в&x&f&8&0&0&6&6н&x&f&6&0&0&7&8а&x&f&5&0&0&8&9я &x&f&4&0&0&9&aв&x&f&3&0&0&a&bо&x&f&1&0&0&b&cл&x&f&0&0&0&c&dн&x&e&f&0&0&d&eа"));
            tntmetaRV.getPersistentDataContainer().set(NamespacedKey.minecraft("customtntrv"), PersistentDataType.INTEGER, 123);
            tntmetaLV.setDisplayName(ColorUtil.msg("&x&0&1&e&8&f&fЛ&x&0&1&e&1&f&9е&x&0&1&d&b&f&3д&x&0&1&d&4&e&dя&x&0&1&c&e&e&7н&x&0&1&c&7&e&1а&x&0&0&c&1&d&aя &x&0&0&b&a&d&4в&x&0&0&b&4&c&eо&x&0&0&a&d&c&8л&x&0&0&a&7&c&2н&x&0&0&a&0&b&cа"));
            tntmetaLV.getPersistentDataContainer().set(NamespacedKey.minecraft("customtntlv"), PersistentDataType.INTEGER, 123);
            tntC4.setItemMeta(tntmetaC4);
            tntA.setItemMeta(tntmetaA);
            tntB.setItemMeta(tntmetaB);
            tntLV.setItemMeta(tntmetaLV);
            tntRV.setItemMeta(tntmetaRV);
            player.getInventory().addItem(tntC4);
            player.getInventory().addItem(tntA);
            player.getInventory().addItem(tntRV);
            player.getInventory().addItem(tntB);
            player.getInventory().addItem(tntLV);
        }
        return true;
    }

}
