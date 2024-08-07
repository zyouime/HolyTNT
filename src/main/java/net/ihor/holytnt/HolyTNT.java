package net.ihor.holytnt;

import com.destroystokyo.paper.event.block.TNTPrimeEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public final class HolyTNT extends JavaPlugin implements Listener {
    private Map<String, String> coordsC4 = new HashMap<>();

    private Map<String, String> coordsA = new HashMap<>();

    private Map<String, String> coordsB = new HashMap<>();

    private Map<String, String> coordsRV = new HashMap<>();

    private Map<String, String> coordsLV = new HashMap<>();

    private Map<String, String> regions = new HashMap<>();

    private Map<String, String> coordsB2 = new HashMap<>();

    private static File file;

    private static Gson gson;

    private static ConfigData configData;

    private Map<String, String> armorStands = new HashMap<>();

    private Map<String, Integer> durabilityMap = new HashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("tnt").setExecutor(new GiveCommand());
        gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
        file = new File(getDataFolder(), "config.json");;
        configData = new ConfigData();
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try (FileWriter fileWriter = new FileWriter(file)) {
                ConfigData configData1 = new ConfigData();
                gson.toJson(configData1, fileWriter);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            loadTNTConfig();
            regions.putAll(configData.getRegions());
            armorStands.putAll(configData.getArmorStands());
            durabilityMap.putAll(configData.getDurabilityMap());
            coordsC4.putAll(configData.getCoordsC4());
            coordsA.putAll(configData.getCoordsA());
            coordsLV.putAll(configData.getCoordsLV());
            coordsRV.putAll(configData.getCoordsRV());
            coordsB.putAll(configData.getCoordsB());
            coordsB2.putAll(configData.getCoordsB2());
        }
    }

    public void loadTNTConfig() {
        try (FileReader fileReader = new FileReader(file)) {
            configData = gson.fromJson(fileReader, ConfigData.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveTNTConfig() {
        try (FileWriter fileWriter = new FileWriter(file)) {
            gson.toJson(configData, fileWriter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void BlockPlaceEvent(BlockPlaceEvent event) {

        ItemStack nbt = event.getItemInHand();
        String id = UUID.randomUUID().toString();
        if (nbt.getItemMeta().getPersistentDataContainer().has(NamespacedKey.minecraft("customtntc4"), PersistentDataType.INTEGER)) {
            coordsC4.put(event.getBlock().getLocation().toString(), id);
        }
        if (nbt.getItemMeta().getPersistentDataContainer().has(NamespacedKey.minecraft("customtntrv"), PersistentDataType.INTEGER)) {
            coordsRV.put(event.getBlock().getLocation().toString(), id);
        }
        if (nbt.getItemMeta().getPersistentDataContainer().has(NamespacedKey.minecraft("customtntlv"), PersistentDataType.INTEGER)) {
            coordsLV.put(event.getBlock().getLocation().toString(), id);
        }
        if (nbt.getItemMeta().getPersistentDataContainer().has(NamespacedKey.minecraft("customtnta"), PersistentDataType.INTEGER)) {
            coordsA.put(event.getBlock().getLocation().toString(), id);
        }
        if (nbt.getItemMeta().getPersistentDataContainer().has(NamespacedKey.minecraft("customtntb"), PersistentDataType.INTEGER)) {
            coordsB.put(event.getBlock().getLocation().toString(), id);
        }
        if (nbt.getItemMeta().getPersistentDataContainer().has(NamespacedKey.minecraft("customtntb2"), PersistentDataType.INTEGER)) {
            coordsB2.put(event.getBlock().getLocation().toString(), id);
        }
        if (event.getBlock().getType() == Material.ANCIENT_DEBRIS && event.getPlayer().isSneaking()) {
            int radius = 15;
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager manager = container.get(BukkitAdapter.adapt(event.getPlayer().getWorld()));
            BlockVector3 min = BlockVector3.at(event.getBlock().getX() - radius, event.getBlock().getY() - radius, event.getBlock().getZ() - radius);
            BlockVector3 max = BlockVector3.at(event.getBlock().getX() + radius, event.getBlock().getY() + radius, event.getBlock().getZ() + radius);
            ProtectedCuboidRegion region = new ProtectedCuboidRegion(id, min, max);
            boolean intersects = false;
            for (ProtectedRegion region1 : manager.getRegions().values()) {
                if (regionsIntersect(region1, region)) {
                    intersects = true;
                    break;
                }
            }

            if (intersects) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("Ты регион пересекаешь, еблан");
                return;
            }
            region.setFlag(Flags.TNT, StateFlag.State.ALLOW);
            DefaultDomain domain = new DefaultDomain();
            domain.addPlayer(event.getPlayer().getName());
            region.setOwners(domain);
            manager.addRegion(region);
            ArmorStand armorStand = (ArmorStand) event.getBlock().getLocation().getWorld().spawnEntity(event.getBlockPlaced().getLocation().add(0.5, 1, 0.5), EntityType.ARMOR_STAND);
            armorStand.setGravity(false);
            armorStand.setMarker(true);
            armorStand.setCustomName(ColorUtil.msg("&a&lГЛЕБИЩЕ " + "&f&l4/4"));
            armorStand.setCustomNameVisible(true);
            armorStand.setVisible(false);
            event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1f);
            regions.put(event.getBlock().getLocation().toString(), id);
            System.out.println(regions);
            armorStands.put(event.getBlock().getLocation().toString(), String.valueOf(armorStand.getUniqueId()));
            durabilityMap.put(id, 4);
        }
    }
    @EventHandler
    public void BlockBreakEvent(BlockBreakEvent event) {
        String id = regions.get(event.getBlock().getLocation().toString());
        if (event.getBlock().getType() == Material.ANCIENT_DEBRIS && id != null) {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager manager = container.get(BukkitAdapter.adapt(event.getPlayer().getWorld()));
            if (event.getPlayer().isOp() || manager.getRegion(id).getOwners().contains(event.getPlayer().getName())) {
                String uuid = armorStands.get(event.getBlock().getLocation().toString());
                event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1f);
                ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(UUID.fromString(uuid));
                manager.removeRegion(id);
                regions.remove(event.getBlock().getLocation().toString());
                configData.regions.remove(event.getBlock().getLocation().toString());
                armorStand.remove();
                armorStands.remove(event.getBlock().getLocation().toString());
                durabilityMap.remove(id);
                configData.durabilityMap.remove(id);
                configData.armorStands.remove(event.getBlock().getLocation().toString());
            }
        } else if (event.getBlock().getType() == Material.TNT) {
            String idA = coordsA.get(event.getBlock().getLocation().toString());
            String idB = coordsB.get(event.getBlock().getLocation().toString());
            String idRV = coordsRV.get(event.getBlock().getLocation().toString());
            String idLV = coordsLV.get(event.getBlock().getLocation().toString());
            String idC4 = coordsC4.get(event.getBlock().getLocation().toString());
            String idB2 = coordsB2.get(event.getBlock().getLocation().toString());
            if (idA != null) {
                if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
                    event.setDropItems(false);
                    ItemStack tntA = new ItemStack(Material.TNT);
                    ItemMeta tntmetaA = tntA.getItemMeta();
                    tntmetaA.setDisplayName(ColorUtil.msg("&x&f&f&9&1&0&0Д&x&f&8&8&a&0&0и&x&f&2&8&2&0&0н&x&e&b&7&b&0&0а&x&e&4&7&4&0&0м&x&d&d&6&d&0&0и&x&d&7&6&5&0&0т &x&d&0&5&e&0&0А"));
                    tntmetaA.getPersistentDataContainer().set(NamespacedKey.minecraft("customtnta"), PersistentDataType.INTEGER, 123);
                    tntA.setItemMeta(tntmetaA);
                    event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), tntA);
                }
                coordsA.remove(event.getBlock().getLocation().toString());
                configData.coordsA.remove(event.getBlock().getLocation().toString());
            }
            if (idB != null) {
                if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
                    event.setDropItems(false);
                    ItemStack tntB = new ItemStack(Material.TNT);
                    ItemMeta tntmetaB = tntB.getItemMeta();
                    tntmetaB.setDisplayName(ColorUtil.msg("&x&f&f&0&0&f&cД&x&f&7&0&0&f&3и&x&e&f&0&0&e&9н&x&e&7&0&0&e&0а&x&e&0&0&0&d&6м&x&d&8&0&0&c&dи&x&d&0&0&0&c&3т &x&c&8&0&0&b&aB"));
                    tntmetaB.getPersistentDataContainer().set(NamespacedKey.minecraft("customtntb"), PersistentDataType.INTEGER, 123);
                    tntB.setItemMeta(tntmetaB);
                    event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), tntB);
                }
                coordsB.remove(event.getBlock().getLocation().toString());
                configData.coordsB.remove(event.getBlock().getLocation().toString());
            }
            if (idRV != null) {
                if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
                    event.setDropItems(false);
                    ItemStack tntRV = new ItemStack(Material.TNT);
                    ItemMeta tntmetaRV = tntRV.getItemMeta();
                    tntmetaRV.setDisplayName(ColorUtil.msg("&x&f&f&0&0&0&0Р&x&f&e&0&0&1&1а&x&f&d&0&0&2&2з&x&f&b&0&0&3&3р&x&f&a&0&0&4&4ы&x&f&9&0&0&5&5в&x&f&8&0&0&6&6н&x&f&6&0&0&7&8а&x&f&5&0&0&8&9я &x&f&4&0&0&9&aв&x&f&3&0&0&a&bо&x&f&1&0&0&b&cл&x&f&0&0&0&c&dн&x&e&f&0&0&d&eа"));
                    tntmetaRV.getPersistentDataContainer().set(NamespacedKey.minecraft("customtntrv"), PersistentDataType.INTEGER, 123);
                    tntRV.setItemMeta(tntmetaRV);
                    event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), tntRV);
                }
                coordsRV.remove(event.getBlock().getLocation().toString());
                configData.coordsRV.remove(event.getBlock().getLocation().toString());
            }
            if (idLV != null) {
                if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
                    event.setDropItems(false);
                    ItemStack tntLV = new ItemStack(Material.TNT);
                    ItemMeta tntmetaLV = tntLV.getItemMeta();
                    tntmetaLV.setDisplayName(ColorUtil.msg("&x&0&1&e&8&f&fЛ&x&0&1&e&1&f&9е&x&0&1&d&b&f&3д&x&0&1&d&4&e&dя&x&0&1&c&e&e&7н&x&0&1&c&7&e&1а&x&0&0&c&1&d&aя &x&0&0&b&a&d&4в&x&0&0&b&4&c&eо&x&0&0&a&d&c&8л&x&0&0&a&7&c&2н&x&0&0&a&0&b&cа"));
                    tntmetaLV.getPersistentDataContainer().set(NamespacedKey.minecraft("customtntlv"), PersistentDataType.INTEGER, 123);
                    tntLV.setItemMeta(tntmetaLV);
                    event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), tntLV);
                }
                coordsLV.remove(event.getBlock().getLocation().toString());
                configData.coordsLV.remove(event.getBlock().getLocation().toString());
            }
            if (idC4 != null) {
                if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
                    event.setDropItems(false);
                    ItemStack tntC4 = new ItemStack(Material.TNT);
                    ItemMeta tntmetaC4 = tntC4.getItemMeta();
                    tntmetaC4.setDisplayName(ColorUtil.msg("&x&f&f&0&0&b&fC&x&e&e&1&4&c&44 &x&d&d&2&9&c&aВ&x&c&b&3&d&c&fз&x&b&a&5&1&d&5Р&x&a&9&6&5&d&aы&x&9&8&7&a&e&0В&x&8&7&8&e&e&5ч&x&7&6&a&2&e&bА&x&6&4&b&6&f&0т&x&5&3&c&b&f&6К&x&4&2&d&f&f&bа"));
                    tntmetaC4.getPersistentDataContainer().set(NamespacedKey.minecraft("customtntc4"), PersistentDataType.INTEGER, 123);
                    tntC4.setItemMeta(tntmetaC4);
                    event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), tntC4);
                }
                coordsC4.remove(event.getBlock().getLocation().toString());
                configData.coordsC4.remove(event.getBlock().getLocation().toString());
            }
            if (idB2 != null) {
                if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
                    event.setDropItems(false);
                    ItemStack tntB2 = new ItemStack(Material.TNT);
                    ItemMeta tntmetaB2 = tntB2.getItemMeta();
                    tntmetaB2.setDisplayName(ColorUtil.msg("&x&f&f&0&0&0&0Д&x&f&4&0&0&0&0и&x&e&9&0&0&0&0н&x&d&e&0&0&0&0а&x&d&3&0&0&0&0м&x&c&8&0&0&0&0и&x&b&d&0&0&0&0т &x&b&2&0&0&0&0Б&x&a&7&0&0&0&02"));
                    tntmetaB2.getPersistentDataContainer().set(NamespacedKey.minecraft("customtntb2"), PersistentDataType.INTEGER, 123);
                    tntB2.setItemMeta(tntmetaB2);
                    event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), tntB2);
                }
                coordsB2.remove(event.getBlock().getLocation().toString());
                configData.coordsB2.remove(event.getBlock().getLocation().toString());
            }
        }
    }

    @EventHandler
    public void BlockPistonExtendEvent(BlockPistonExtendEvent event) {
        Map<String, String> tntPistonLV = new HashMap<>();
        Map<String, String> tntPistonRV = new HashMap<>();
        Map<String, String> tntPistonA = new HashMap<>();
        Map<String, String> tntPistonB = new HashMap<>();
        Map<String, String> tntPistonB2 = new HashMap<>();
        Map<String, String> tntPistonC4 = new HashMap<>();
        BlockFace blockFace = event.getDirection();
        for (Block block : event.getBlocks()) {
            Location location = block.getLocation();
            if (location.getBlock().getType() == Material.TNT) {
                if (coordsLV.containsKey(location.toString())) {
                    coordsLV.remove(location.toString());
                    tntPistonLV.put(block.getLocation().add(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ()).toString(), UUID.randomUUID().toString());
                }
                if (coordsRV.containsKey(location.toString())) {
                    coordsRV.remove(location.toString());
                    tntPistonRV.put(block.getLocation().add(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ()).toString(), UUID.randomUUID().toString());
                }
                if (coordsA.containsKey(location.toString())) {
                    coordsA.remove(location.toString());
                    tntPistonA.put(block.getLocation().add(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ()).toString(), UUID.randomUUID().toString());
                }
                if (coordsB.containsKey(location.toString())) {
                    coordsB.remove(location.toString());
                    tntPistonB.put(block.getLocation().add(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ()).toString(), UUID.randomUUID().toString());
                }
                if (coordsB2.containsKey(location.toString())) {
                    coordsB2.remove(location.toString());
                    tntPistonB2.put(block.getLocation().add(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ()).toString(), UUID.randomUUID().toString());
                }
                if (coordsC4.containsKey(location.toString())) {
                    coordsC4.remove(location.toString());
                    tntPistonC4.put(block.getLocation().add(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ()).toString(), UUID.randomUUID().toString());
                }
            }
        }
        coordsLV.putAll(tntPistonLV);
        coordsRV.putAll(tntPistonRV);
        coordsC4.putAll(tntPistonC4);
        coordsA.putAll(tntPistonA);
        coordsB2.putAll(tntPistonB2);
        coordsB.putAll(tntPistonB);
    }

    @EventHandler
    public void BlockPistonRetractEvent(BlockPistonRetractEvent event) {
        Map<String, String> tntPistonLV = new HashMap<>();
        Map<String, String> tntPistonRV = new HashMap<>();
        Map<String, String> tntPistonA = new HashMap<>();
        Map<String, String> tntPistonB = new HashMap<>();
        Map<String, String> tntPistonB2 = new HashMap<>();
        Map<String, String> tntPistonC4 = new HashMap<>();
        BlockFace blockFace = event.getDirection();
        for (Block block : event.getBlocks()) {
            Location location = block.getLocation();
            if (location.getBlock().getType() == Material.TNT) {
                if (coordsLV.containsKey(location.toString())) {
                    coordsLV.remove(location.toString());
                    tntPistonLV.put(block.getLocation().add(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ()).toString(), UUID.randomUUID().toString());
                }
                if (coordsRV.containsKey(location.toString())) {
                    coordsRV.remove(location.toString());
                    tntPistonRV.put(block.getLocation().add(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ()).toString(), UUID.randomUUID().toString());
                }
                if (coordsA.containsKey(location.toString())) {
                    coordsA.remove(location.toString());
                    tntPistonA.put(block.getLocation().add(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ()).toString(), UUID.randomUUID().toString());
                }
                if (coordsB.containsKey(location.toString())) {
                    coordsB.remove(location.toString());
                    tntPistonB.put(block.getLocation().add(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ()).toString(), UUID.randomUUID().toString());
                }
                if (coordsB2.containsKey(location.toString())) {
                    coordsB2.remove(location.toString());
                    tntPistonB2.put(block.getLocation().add(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ()).toString(), UUID.randomUUID().toString());
                }
                if (coordsC4.containsKey(location.toString())) {
                    coordsC4.remove(location.toString());
                    tntPistonC4.put(block.getLocation().add(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ()).toString(), UUID.randomUUID().toString());
                }
            }
        }
        coordsLV.putAll(tntPistonLV);
        coordsRV.putAll(tntPistonRV);
        coordsC4.putAll(tntPistonC4);
        coordsA.putAll(tntPistonA);
        coordsB2.putAll(tntPistonB2);
        coordsB.putAll(tntPistonB);
    }
    @EventHandler
    public void BlockDispenseEvent(BlockDispenseEvent event) {
        if (event.getItem().getItemMeta() != null) {
            Dispenser dispenser = (Dispenser) event.getBlock().getState();
            Inventory inventory = dispenser.getInventory();
            if (event.getItem().getItemMeta().getPersistentDataContainer().has(NamespacedKey.minecraft("customtntc4"), PersistentDataType.INTEGER) && event.getBlock().getType() == Material.DISPENSER) {
                Location location = event.getVelocity().toLocation(event.getBlock().getWorld()).add(-0.5, 0, -0.5);
                SpawnCustomTNT.spawnC4TNT(location);
                event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
                inventory.removeItem(event.getItem());
                event.setCancelled(true);
            }
            if (event.getItem().getItemMeta().getPersistentDataContainer().has(NamespacedKey.minecraft("customtnta"), PersistentDataType.INTEGER) && event.getBlock().getType() == Material.DISPENSER) {
                Location location = event.getVelocity().toLocation(event.getBlock().getWorld()).add(-0.5, 0, -0.5);
                SpawnCustomTNT.spawnATNT(location);
                event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
                inventory.removeItem(event.getItem());
                event.setCancelled(true);
            }
            if (event.getItem().getItemMeta().getPersistentDataContainer().has(NamespacedKey.minecraft("customtntb"), PersistentDataType.INTEGER) && event.getBlock().getType() == Material.DISPENSER) {
                Location location = event.getVelocity().toLocation(event.getBlock().getWorld()).add(-0.5, 0, -0.5);
                SpawnCustomTNT.spawnBTNT(location);
                event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
                inventory.removeItem(event.getItem());
                event.setCancelled(true);

            }
            if (event.getItem().getItemMeta().getPersistentDataContainer().has(NamespacedKey.minecraft("customtntrv"), PersistentDataType.INTEGER) && event.getBlock().getType() == Material.DISPENSER) {
                Location location = event.getVelocity().toLocation(event.getBlock().getWorld()).add(-0.5, 0, -0.5);
                SpawnCustomTNT.spawnRVTNT(location);
                event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
                inventory.removeItem(event.getItem());
                event.setCancelled(true);
            }
            if (event.getItem().getItemMeta().getPersistentDataContainer().has(NamespacedKey.minecraft("customtntlv"), PersistentDataType.INTEGER) && event.getBlock().getType() == Material.DISPENSER) {
                Location location = event.getVelocity().toLocation(event.getBlock().getWorld()).add(-0.5, 0, -0.5);
                SpawnCustomTNT.spawnLVTNT(location);
                event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
                inventory.removeItem(event.getItem());
                event.setCancelled(true);
            }
            if (event.getItem().getItemMeta().getPersistentDataContainer().has(NamespacedKey.minecraft("customtntb2"), PersistentDataType.INTEGER) && event.getBlock().getType() == Material.DISPENSER) {
                Location location = event.getVelocity().toLocation(event.getBlock().getWorld()).add(-0.5, 0, -0.5);
                SpawnCustomTNT.spawnB2TNT(location);
                event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
                inventory.removeItem(event.getItem());
                event.setCancelled(true);
            }
        }
    }


    @EventHandler
    public void TNTPrimeEvent(TNTPrimeEvent event) {
        if (coordsC4.containsKey(event.getBlock().getLocation().toString())) {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            SpawnCustomTNT.spawnC4TNT(event.getBlock().getLocation());
            event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
            coordsC4.remove(event.getBlock().getLocation().toString());
            configData.coordsC4.remove(event.getBlock().getLocation().toString());
        }
        if (coordsB.containsKey(event.getBlock().getLocation().toString())) {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            SpawnCustomTNT.spawnBTNT(event.getBlock().getLocation());
            event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
            coordsB.remove(event.getBlock().getLocation().toString());
            configData.coordsB.remove(event.getBlock().getLocation().toString());
        }
        if (coordsA.containsKey(event.getBlock().getLocation().toString())) {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            SpawnCustomTNT.spawnATNT(event.getBlock().getLocation());
            event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
            coordsA.remove(event.getBlock().getLocation().toString());
            configData.coordsA.remove(event.getBlock().getLocation().toString());
        }
        if (coordsRV.containsKey(event.getBlock().getLocation().toString())) {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            SpawnCustomTNT.spawnRVTNT(event.getBlock().getLocation());
            event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
            coordsRV.remove(event.getBlock().getLocation().toString());
            configData.coordsRV.remove(event.getBlock().getLocation().toString());
        }
        if (coordsLV.containsKey(event.getBlock().getLocation().toString())) {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            SpawnCustomTNT.spawnLVTNT(event.getBlock().getLocation());
            event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
            coordsLV.remove(event.getBlock().getLocation().toString());
            configData.coordsLV.remove(event.getBlock().getLocation().toString());
        }
        if (coordsB2.containsKey(event.getBlock().getLocation().toString())) {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            SpawnCustomTNT.spawnB2TNT(event.getBlock().getLocation());
            event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
            coordsB2.remove(event.getBlock().getLocation().toString());
            configData.coordsB2.remove(event.getBlock().getLocation().toString());
        }
    }
    @EventHandler
    public void PlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getItem() != null) {
            if (event.getItem().getType() == Material.FLINT_AND_STEEL && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                if (coordsA.containsKey(event.getClickedBlock().getLocation().toString())) {
                    event.setUseInteractedBlock(Event.Result.DENY);
                    if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
                        event.getItem().setDurability((short) (event.getItem().getDurability() + 1));
                    }
                    event.getClickedBlock().setType(Material.AIR);
                    SpawnCustomTNT.spawnATNT(event.getClickedBlock().getLocation());
                    event.getClickedBlock().getWorld().playSound(event.getClickedBlock().getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
                    coordsA.remove(event.getClickedBlock().getLocation().toString());
                    configData.coordsA.remove(event.getClickedBlock().getLocation().toString());
                }
                if (coordsB2.containsKey(event.getClickedBlock().getLocation().toString())) {
                    event.setUseInteractedBlock(Event.Result.DENY);
                    if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
                        event.getItem().setDurability((short) (event.getItem().getDurability() + 1));
                    }
                    event.getClickedBlock().setType(Material.AIR);
                    SpawnCustomTNT.spawnB2TNT(event.getClickedBlock().getLocation());
                    event.getClickedBlock().getWorld().playSound(event.getClickedBlock().getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
                    coordsB2.remove(event.getClickedBlock().getLocation().toString());
                    configData.coordsB2.remove(event.getClickedBlock().getLocation().toString());
                }
                if (coordsLV.containsKey(event.getClickedBlock().getLocation().toString())) {
                    event.setUseInteractedBlock(Event.Result.DENY);
                    if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
                        event.getItem().setDurability((short) (event.getItem().getDurability() + 1));
                    }
                    event.getClickedBlock().setType(Material.AIR);
                    SpawnCustomTNT.spawnLVTNT(event.getClickedBlock().getLocation());
                    event.getClickedBlock().getWorld().playSound(event.getClickedBlock().getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
                    coordsLV.remove(event.getClickedBlock().getLocation().toString());
                    configData.coordsLV.remove(event.getClickedBlock().getLocation().toString());
                }
                if (coordsRV.containsKey(event.getClickedBlock().getLocation().toString())) {
                    event.setUseInteractedBlock(Event.Result.DENY);
                    if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
                        event.getItem().setDurability((short) (event.getItem().getDurability() + 1));
                    }
                    event.getClickedBlock().setType(Material.AIR);
                    SpawnCustomTNT.spawnRVTNT(event.getClickedBlock().getLocation());
                    event.getClickedBlock().getWorld().playSound(event.getClickedBlock().getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
                    coordsRV.remove(event.getClickedBlock().getLocation().toString());
                    configData.coordsRV.remove(event.getClickedBlock().getLocation().toString());
                }
                if (coordsB.containsKey(event.getClickedBlock().getLocation().toString())) {
                    event.setUseInteractedBlock(Event.Result.DENY);
                    if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
                        event.getItem().setDurability((short) (event.getItem().getDurability() + 1));
                    }
                    event.getClickedBlock().setType(Material.AIR);
                    SpawnCustomTNT.spawnBTNT(event.getClickedBlock().getLocation());
                    event.getClickedBlock().getWorld().playSound(event.getClickedBlock().getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
                    coordsB.remove(event.getClickedBlock().getLocation().toString());
                    configData.coordsB.remove(event.getClickedBlock().getLocation().toString());
                }
                if (coordsC4.containsKey(event.getClickedBlock().getLocation().toString())) {
                    event.setUseInteractedBlock(Event.Result.DENY);
                    if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
                        event.getItem().setDurability((short) (event.getItem().getDurability() + 1));
                    }
                    event.getClickedBlock().setType(Material.AIR);
                    SpawnCustomTNT.spawnC4TNT(event.getClickedBlock().getLocation());
                    event.getClickedBlock().getWorld().playSound(event.getClickedBlock().getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
                    coordsC4.remove(event.getClickedBlock().getLocation().toString());
                    configData.coordsC4.remove(event.getClickedBlock().getLocation().toString());
                }
            }
        }
    }
    @EventHandler
    public void EntityExplodeEvent(EntityExplodeEvent event) {
        if (event.getEntity().getType() == EntityType.PRIMED_TNT && event.getEntity().getCustomName() != null && event.getEntity().getCustomName().equals("C4") && event.getLocation().getBlock().getType() != Material.WATER && event.getLocation().getBlock().getType() != Material.LAVA) {
            Location location = event.getLocation();
            int radius = 2;
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        Location loc = location.clone().add(x, y, z);
                        String region = regions.get(loc.getBlock().getLocation().toString());
                        if (loc.getBlock().getType() == Material.OBSIDIAN) {
                            loc.getBlock().setType(Material.AIR);
                            loc.getBlock().getWorld().dropItem(loc, new ItemStack(Material.OBSIDIAN));
                        } else if (loc.getBlock().getType() == Material.CRYING_OBSIDIAN) {
                            loc.getBlock().setType(Material.AIR);
                            loc.getBlock().getWorld().dropItem(loc, new ItemStack(Material.CRYING_OBSIDIAN));
                        }
                        for (String s : regions.values()) {
                            if (s.equals(regions.get(loc.getBlock().getLocation().toString()))) {
                                region = s;
                            }
                        }
                        if (region == null) {
                            if (loc.getBlock().getType() == Material.ANCIENT_DEBRIS) {
                                loc.getBlock().setType(Material.AIR);
                                loc.getBlock().getWorld().dropItem(loc, new ItemStack(Material.ANCIENT_DEBRIS));
                            }
                        }
                    }
                }
            }
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        Location loc = location.clone().add(x, y, z);
                        String id = String.valueOf(regions.get(loc.getBlock().getLocation().toString()));
                            if (loc.getBlock().getType() == Material.ANCIENT_DEBRIS && id != null && durabilityMap.containsKey(id)) {
                                int durability = durabilityMap.get(id) - 1;
                                durabilityMap.put(id, durability);
                                if (durability == 0) {
                                    String uuid = armorStands.get(loc.getBlock().getLocation().toString());
                                    uuid = uuid.replaceAll("\\s", "").replaceAll("\\p{C}", "");
                                    ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(UUID.fromString(uuid));
                                    armorStand.remove();
                                    armorStands.remove(loc.getBlock().getLocation().toString());
                                    configData.armorStands.remove(loc.getBlock().getLocation().toString());
                                    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                                    RegionManager manager = container.get(BukkitAdapter.adapt(event.getEntity().getWorld()));
                                    loc.getBlock().setType(Material.AIR);
                                    loc.getBlock().getWorld().playSound(loc.getBlock().getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1f);
                                    loc.getBlock().getWorld().dropItem(loc, new ItemStack(Material.ANCIENT_DEBRIS));
                                    manager.removeRegion(id);
                                    regions.remove(loc.getBlock().getLocation().toString());
                                    configData.regions.remove(loc.getBlock().getLocation().toString());
                                    durabilityMap.remove(id);
                                    configData.durabilityMap.remove(id);
                                } else if (durability == 3) {
                                    String uuid = armorStands.get(loc.getBlock().getLocation().toString());
                                    uuid = uuid.replaceAll("\\s", "").replaceAll("\\p{C}", "");
                                    ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(UUID.fromString(uuid));
                                    armorStand.remove();
                                    armorStands.remove(loc.getBlock().getLocation().toString());
                                    ArmorStand armorStand1 = (ArmorStand) loc.getBlock().getLocation().getWorld().spawnEntity(loc.getBlock().getLocation().add(0.5, 1, 0.5), EntityType.ARMOR_STAND);
                                    armorStand1.setGravity(false);
                                    armorStand1.setMarker(true);
                                    armorStand1.setCustomName(ColorUtil.msg("&e&lГЛЕБИЩЕ " + "&f&l3/4"));
                                    armorStand1.setCustomNameVisible(true);
                                    armorStand1.setVisible(false);
                                    armorStands.put(loc.getBlock().getLocation().toString(), String.valueOf(armorStand1.getUniqueId()));
                                } else if (durability == 2) {
                                    String uuid = armorStands.get(loc.getBlock().getLocation().toString());
                                    uuid = uuid.replaceAll("\\s", "").replaceAll("\\p{C}", "");
                                    ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(UUID.fromString(uuid));
                                    armorStand.remove();
                                    armorStands.remove(loc.getBlock().getLocation().toString());
                                    ArmorStand armorStand1 = (ArmorStand) loc.getBlock().getLocation().getWorld().spawnEntity(loc.getBlock().getLocation().add(0.5, 1, 0.5), EntityType.ARMOR_STAND);
                                    armorStand1.setGravity(false);
                                    armorStand1.setMarker(true);
                                    armorStand1.setCustomName(ColorUtil.msg("&6&lГЛЕБИЩЕ " + "&f&l2/4"));
                                    armorStand1.setCustomNameVisible(true);
                                    armorStand1.setVisible(false);
                                    armorStands.put(loc.getBlock().getLocation().toString(), String.valueOf(armorStand1.getUniqueId()));
                                } else if (durability == 1) {
                                    String uuid = armorStands.get(loc.getBlock().getLocation().toString());
                                    uuid = uuid.replaceAll("\\s", "").replaceAll("\\p{C}", "");
                                    ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(UUID.fromString(uuid));
                                    armorStand.remove();
                                    armorStands.remove(loc.getBlock().getLocation().toString());
                                    ArmorStand armorStand1 = (ArmorStand) loc.getBlock().getLocation().getWorld().spawnEntity(loc.getBlock().getLocation().add(0.5, 1, 0.5), EntityType.ARMOR_STAND);
                                    armorStand1.setGravity(false);
                                    armorStand1.setMarker(true);
                                    armorStand1.setCustomName(ColorUtil.msg("&c&lГЛЕБИЩЕ " + "&f&l1/4"));
                                    armorStand1.setCustomNameVisible(true);
                                    armorStand1.setVisible(false);
                                    armorStands.put(loc.getBlock().getLocation().toString(), String.valueOf(armorStand1.getUniqueId()));
                                }
                        }
                    }
                }
            }
        }
        if (event.getEntity().getType() == EntityType.PRIMED_TNT && event.getEntity().getCustomName() != null && event.getEntity().getCustomName().equals("RV")) {
            Location location = event.getLocation();
            int radius = 2;
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        Location loc = location.clone().add(x, y, z);
                        String region = regions.get(loc.getBlock().getLocation().toString());
                        if (loc.getBlock().getType() == Material.OBSIDIAN) {
                            loc.getBlock().setType(Material.AIR);
                            loc.getBlock().getWorld().dropItem(loc, new ItemStack(Material.OBSIDIAN));
                        } else if (loc.getBlock().getType() == Material.CRYING_OBSIDIAN) {
                            loc.getBlock().setType(Material.AIR);
                            loc.getBlock().getWorld().dropItem(loc, new ItemStack(Material.CRYING_OBSIDIAN));
                        }
                        for (String s : regions.values()) {
                            if (s.equals(regions.get(loc.getBlock().getLocation().toString()))) {
                                region = s;
                            }
                        }
                        if (region == null) {
                            if (loc.getBlock().getType() == Material.ANCIENT_DEBRIS) {
                                loc.getBlock().setType(Material.AIR);
                                loc.getBlock().getWorld().dropItem(loc, new ItemStack(Material.ANCIENT_DEBRIS));
                            }
                        }
                    }
                }
            }
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        Location loc = location.clone().add(x, y, z);
                        String id = String.valueOf(regions.get(loc.getBlock().getLocation().toString()));
                        if (loc.getBlock().getType() == Material.ANCIENT_DEBRIS && id != null && durabilityMap.containsKey(id)) {
                            int durability = durabilityMap.get(id) - 1;
                            durabilityMap.put(id, durability);
                            if (durability == 0) {
                                String uuid = armorStands.get(loc.getBlock().getLocation().toString());
                                uuid = uuid.replaceAll("\\s", "").replaceAll("\\p{C}", "");
                                ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(UUID.fromString(uuid));
                                armorStand.remove();
                                armorStands.remove(loc.getBlock().getLocation().toString());
                                configData.armorStands.remove(loc.getBlock().getLocation().toString());
                                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                                RegionManager manager = container.get(BukkitAdapter.adapt(event.getEntity().getWorld()));
                                loc.getBlock().setType(Material.AIR);
                                loc.getBlock().getWorld().playSound(loc.getBlock().getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1f);
                                loc.getBlock().getWorld().dropItem(loc, new ItemStack(Material.ANCIENT_DEBRIS));
                                manager.removeRegion(id);
                                regions.remove(loc.getBlock().getLocation().toString());
                                configData.regions.remove(loc.getBlock().getLocation().toString());
                                durabilityMap.remove(id);
                                configData.durabilityMap.remove(id);
                            } else if (durability == 3) {
                                String uuid = armorStands.get(loc.getBlock().getLocation().toString());
                                uuid = uuid.replaceAll("\\s", "").replaceAll("\\p{C}", "");
                                ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(UUID.fromString(uuid));
                                armorStand.remove();
                                armorStands.remove(loc.getBlock().getLocation().toString());
                                ArmorStand armorStand1 = (ArmorStand) loc.getBlock().getLocation().getWorld().spawnEntity(loc.getBlock().getLocation().add(0.5, 1, 0.5), EntityType.ARMOR_STAND);
                                armorStand1.setGravity(false);
                                armorStand1.setMarker(true);
                                armorStand1.setCustomName(ColorUtil.msg("&e&lГЛЕБИЩЕ " + "&f&l3/4"));
                                armorStand1.setCustomNameVisible(true);
                                armorStand1.setVisible(false);
                                armorStands.put(loc.getBlock().getLocation().toString(), String.valueOf(armorStand1.getUniqueId()));
                            } else if (durability == 2) {
                                String uuid = armorStands.get(loc.getBlock().getLocation().toString());
                                uuid = uuid.replaceAll("\\s", "").replaceAll("\\p{C}", "");
                                ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(UUID.fromString(uuid));
                                armorStand.remove();
                                armorStands.remove(loc.getBlock().getLocation().toString());
                                ArmorStand armorStand1 = (ArmorStand) loc.getBlock().getLocation().getWorld().spawnEntity(loc.getBlock().getLocation().add(0.5, 1, 0.5), EntityType.ARMOR_STAND);
                                armorStand1.setGravity(false);
                                armorStand1.setMarker(true);
                                armorStand1.setCustomName(ColorUtil.msg("&6&lГЛЕБИЩЕ " + "&f&l2/4"));
                                armorStand1.setCustomNameVisible(true);
                                armorStand1.setVisible(false);
                                armorStands.put(loc.getBlock().getLocation().toString(), String.valueOf(armorStand1.getUniqueId()));
                            } else if (durability == 1) {
                                String uuid = armorStands.get(loc.getBlock().getLocation().toString());
                                uuid = uuid.replaceAll("\\s", "").replaceAll("\\p{C}", "");
                                ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(UUID.fromString(uuid));
                                armorStand.remove();
                                armorStands.remove(loc.getBlock().getLocation().toString());
                                ArmorStand armorStand1 = (ArmorStand) loc.getBlock().getLocation().getWorld().spawnEntity(loc.getBlock().getLocation().add(0.5, 1, 0.5), EntityType.ARMOR_STAND);
                                armorStand1.setGravity(false);
                                armorStand1.setMarker(true);
                                armorStand1.setCustomName(ColorUtil.msg("&c&lГЛЕБИЩЕ " + "&f&l1/4"));
                                armorStand1.setCustomNameVisible(true);
                                armorStand1.setVisible(false);
                                armorStands.put(loc.getBlock().getLocation().toString(), String.valueOf(armorStand1.getUniqueId()));
                            }
                        }
                    }
                }
            }
        }
        if (event.getEntity().getType() == EntityType.PRIMED_TNT && event.getEntity().getCustomName() != null && event.getEntity().getCustomName().equals("LV")) {
            spawnSphere(event.getLocation());
            event.getLocation().getBlock().getWorld().playSound(event.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);
            event.setCancelled(true);
        }
        if (event.getEntity().getType() == EntityType.PRIMED_TNT && event.getEntity().getCustomName() != null && event.getEntity().getCustomName().equals("B2")) {
            event.setCancelled(true);
            event.getLocation().getBlock().getWorld().playSound(event.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);
            int radius = 12;
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        Location location = event.getLocation().clone().add(x, y, z);
                        Map<Location, Material> ignoredBlocks = new HashMap<>();
                        ignoredBlocks.put(location.clone(), location.getBlock().getType());
                        for (Map.Entry<Location, Material> entry : ignoredBlocks.entrySet()) {
                            if (entry.getKey().getBlock().getType() == Material.BEDROCK || entry.getKey().getBlock().getType() == Material.ANCIENT_DEBRIS || entry.getKey().getBlock().getType() == Material.OBSIDIAN) {
                                entry.getKey().getBlock().setType(entry.getValue());
                            } else {
                                entry.getKey().getBlock().setType(Material.AIR);
                            }
                            ignoredBlocks.clear();
                        }
                    }
                }
            }
        }
    }


    private void spawnSphere(Location location) {
        HashMap<Location, Material> blocks = new HashMap<>();

        location.setY(location.getY() - 2);
        blocks.put(location.clone(), location.getBlock().getState().getType());
        location.setY(location.getY() + 1);
        blocks.put(location.clone(), location.getBlock().getState().getType());
        location.setX(location.getX() + 1);
        location.setZ(location.getZ() + 1);
        blocks.put(location.clone(), location.getBlock().getState().getType());
        location.setX(location.getX() - 1);
        blocks.put(location.clone(), location.getBlock().getState().getType());
        location.setX(location.getX() - 1);
        blocks.put(location.clone(), location.getBlock().getState().getType());
        location.setZ(location.getZ() - 1);
        blocks.put(location.clone(), location.getBlock().getState().getType());
        location.setZ(location.getZ() - 1);
        blocks.put(location.clone(), location.getBlock().getState().getType());
        location.setX(location.getX() + 1);
        blocks.put(location.clone(), location.getBlock().getState().getType());
        location.setX(location.getX() + 1);
        blocks.put(location.clone(), location.getBlock().getState().getType());
        location.setZ(location.getZ() + 1);
        blocks.put(location.clone(), location.getBlock().getState().getType());
        location.setY(location.getY() + 1);
        location.setX(location.getX() + 1);
        blocks.put(location.clone(), location.getBlock().getState().getType());
        location.setX(location.getX() - 1);
        blocks.put(location.clone(), location.getBlock().getState().getType());
        location.setX(location.getX() - 1);
        blocks.put(location.clone(), location.getBlock().getState().getType());
        location.setX(location.getX() - 1);
        blocks.put(location.clone(), location.getBlock().getState().getType());
        location.setZ(location.getZ() + 1);
        blocks.put(location.clone(), location.getBlock().getState().getType());
        location.setX(location.getX() + 1);
        blocks.put(location.clone(), location.getBlock().getState().getType());
        location.setZ(location.getZ() + 1);
        blocks.put(location.clone(), location.getBlock().getState().getType());
        location.setZ(location.getZ() - 1);
        location.setX(location.getX() + 1);
        blocks.put(location.clone(), location.getBlock().getState().getType());
        location.setZ(location.getZ() - 2);
        blocks.put(location.clone(), location.getBlock().getState().getType());
        location.setX(location.getX() - 1);
        blocks.put(location.clone(), location.getBlock().getState().getType());
        location.setZ(location.getZ() - 1);
        blocks.put(location.clone(), location.getBlock().getState().getType());
        location.setZ(location.getZ() + 1);
        location.setX(location.getX() - 1);
        blocks.put(location.clone(), location.getBlock().getState().getType());
        location.setZ(location.getZ() + 1);
        location.setX(location.getX() - 1);
        blocks.put(location.clone(), location.getBlock().getState().getType());
        location.setX(location.getX() + 1);
        location.setY(location.getY() + 1);
        blocks.put(location.clone(), location.getBlock().getState().getType());
        location.setZ(location.getZ() - 1);
        blocks.put(location.clone(), location.getBlock().getState().getType());
        location.setZ(location.getZ() + 2);
        blocks.put(location.clone(), location.getBlock().getState().getType());
        location.setX(location.getX() + 1);
        blocks.put(location.clone(), location.getBlock().getState().getType());
        location.setX(location.getX() + 1);
        blocks.put(location.clone(), location.getBlock().getState().getType());
        location.setZ(location.getZ() - 1);
        blocks.put(location.clone(), location.getBlock().getState().getType());
        location.setZ(location.getZ() - 1);
        blocks.put(location.clone(), location.getBlock().getState().getType());
        location.setX(location.getX() - 1);
        blocks.put(location.clone(), location.getBlock().getState().getType());
        location.setZ(location.getZ() + 1);
        blocks.put(location.clone(), location.getBlock().getState().getType());
        location.setY(location.getY() + 1);
        blocks.put(location.clone(), location.getBlock().getState().getType());

        HashMap<Location, Material> iceBlocks = new HashMap<>();


        for (Map.Entry<Location, Material> entry : blocks.entrySet()) {
            if (entry.getKey().getBlock().getType() == Material.WATER || entry.getKey().getBlock().getType() == Material.AIR || entry.getKey().getBlock().getType() == Material.PACKED_ICE || entry.getKey().getBlock().getType() == Material.LAVA) {
                location.getBlock().getWorld().playSound(location, Sound.BLOCK_STONE_PLACE, 1f, 1f);
                entry.getKey().getBlock().setType(Material.PACKED_ICE);
                iceBlocks.put(entry.getKey().getBlock().getLocation(), entry.getKey().getBlock().getType());
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<Location, Material> entry : blocks.entrySet()) {
                    entry.getKey().getBlock().getState().setType(entry.getValue());
                    location.getBlock().getWorld().playSound(location, Sound.BLOCK_STONE_BREAK, 1f, 1f);
                    if (entry.getKey().getBlock().getType() == iceBlocks.get(entry.getKey().getBlock().getLocation())) {
                        iceBlocks.put(entry.getKey().getBlock().getLocation(), Material.AIR);
                        entry.getKey().getBlock().setType(iceBlocks.get(entry.getKey().getBlock().getLocation()));
                    }

                }
                blocks.clear();
                iceBlocks.clear();
            }
        }.runTaskLater(this, 100);
    }

    private boolean regionsIntersect(ProtectedRegion region1, ProtectedRegion region2) {
        BlockVector3 min1 = region1.getMinimumPoint();
        BlockVector3 max1 = region1.getMaximumPoint();
        BlockVector3 min2 = region2.getMinimumPoint();
        BlockVector3 max2 = region2.getMaximumPoint();

        return min1.getBlockX() <= max2.getBlockX() && max1.getBlockX() >= min2.getBlockX() &&
                min1.getBlockY() <= max2.getBlockY() && max1.getBlockY() >= min2.getBlockY() &&
                min1.getBlockZ() <= max2.getBlockZ() && max1.getBlockZ() >= min2.getBlockZ();
    }

    @Override
    public void onDisable() {
        configData.setRegions(regions);
        configData.setArmorStands(armorStands);
        configData.setDurabilityMap(durabilityMap);
        configData.setCoordsC4(coordsC4);
        configData.setCoordsA(coordsA);
        configData.setCoordsB(coordsB);
        configData.setCoordsRV(coordsRV);
        configData.setCoordsLV(coordsLV);
        configData.setCoordsB2(coordsB2);
        saveTNTConfig();
    }
}