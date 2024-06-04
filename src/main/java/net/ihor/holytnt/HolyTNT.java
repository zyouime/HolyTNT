package net.ihor.holytnt;

import com.destroystokyo.paper.event.block.TNTPrimeEvent;
import com.google.gson.*;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public final class HolyTNT extends JavaPlugin implements Listener {
    private List<String> coords = new ArrayList<>();

    private Map<String, String> regions = new HashMap<>();

    private static File file;

    private static Gson gson;

    private static ConfigData configData;

    private Map<String, String> armorStands = new HashMap<>();

    private Map<String, Integer> durabilityMap = new HashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("tnt").setExecutor(new CommandEx());
        gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
        file = new File(getDataFolder(), "zalupa.json");;
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
                System.out.println(configData + "1");
                System.out.println(configData1 + "2");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            loadTNTConfig();
            regions.putAll(configData.regions);
            armorStands.putAll(configData.armorStands);
            durabilityMap.putAll(configData.durabilityMap);
            coords.addAll(configData.coords);
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
    public void customTNTPlace(BlockPlaceEvent event) {
        List<String> lore = event.getItemInHand().getItemMeta().getLore();
        if (lore != null && lore.contains(ColorUtil.msg("&6&lTNT"))) {
            coords.add(event.getBlock().getLocation().toString());
            System.out.println(coords + "123");
        }
        if (event.getBlock().getType() == Material.ANCIENT_DEBRIS) {
            int radius = 15;
            String id = String.valueOf(UUID.randomUUID());
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager manager = container.get(BukkitAdapter.adapt(event.getPlayer().getWorld()));
            BlockVector3 min = BlockVector3.at(event.getBlock().getX() - radius, event.getBlock().getY() - radius, event.getBlock().getZ() - radius);
            BlockVector3 max = BlockVector3.at(event.getBlock().getX() + radius, event.getBlock().getY() + radius, event.getBlock().getZ() + radius);
            ProtectedCuboidRegion region = new ProtectedCuboidRegion(id, min, max);
            region.setFlag(Flags.TNT, StateFlag.State.ALLOW);
            manager.addRegion(region);
            ArmorStand armorStand = (ArmorStand) event.getBlock().getLocation().getWorld().spawnEntity(event.getBlockPlaced().getLocation().add(0.5, 1, 0.5), EntityType.ARMOR_STAND);
            armorStand.setGravity(false);
            armorStand.setMarker(true);
            armorStand.setCustomName(ColorUtil.msg("&a&lГЛЕБИЩЕ " + "&f&l4/4"));
            armorStand.setCustomNameVisible(true);
            armorStand.setVisible(false);
            event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1f);
            regions.put(event.getBlock().getLocation().toString(), id);
            armorStands.put(event.getBlock().getLocation().toString(), String.valueOf(armorStand.getUniqueId()));
            durabilityMap.put(id, 4);
        }
    }

    @EventHandler
    public void regionRemove(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.ANCIENT_DEBRIS) {
            if (event.getPlayer().isOp()) {
                String id = regions.get(event.getBlock().getLocation().toString());
                String uuid = armorStands.get(event.getBlock().getLocation().toString());
                event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1f);
                ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(UUID.fromString(uuid));
                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                RegionManager manager = container.get(BukkitAdapter.adapt(event.getPlayer().getWorld()));
                if (id != null) {
                    manager.removeRegion(id);
                    regions.remove(event.getBlock().getLocation().toString());
                    configData.regions.remove(event.getBlock().getLocation().toString());
                    armorStand.remove();
                    armorStands.remove(event.getBlock().getLocation().toString());
                    configData.armorStands.remove(event.getBlock().getLocation().toString());
                }
            }
        }
    }


    @EventHandler
    public void customTNTDispanser(BlockDispenseEvent event) {
        List<String> lore = event.getItem().getItemMeta().getLore();
        if (lore != null && lore.contains(ColorUtil.msg("&6&lTNT"))) {
            event.setCancelled(true);
            Location location = event.getVelocity().toLocation(event.getBlock().getWorld());
            spawnС4TNT(location);
            event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
        }
    }

    @EventHandler
    public void customTNTUse(TNTPrimeEvent event) {
        if (coords.contains(event.getBlock().getLocation().toString())) {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            spawnС4TNT(event.getBlock().getLocation());
            event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
            coords.remove(event.getBlock().getLocation().toString());
        }
    }

    @EventHandler
    public void customTNTExplode(EntityExplodeEvent event) {
        if (event.getEntity().getType() == EntityType.PRIMED_TNT && event.getEntity().getCustomName()!= null && event.getEntity().getCustomName().equals("GLEBBARIO")) {
            Location location = event.getLocation();
            int radius = 2;
            int radius1 = 1;
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        Location loc = location.clone().add(x, y, z);
                        if (loc.getBlock().getType() == Material.OBSIDIAN) {
                            loc.getBlock().setType(Material.AIR);
                            loc.getBlock().getWorld().dropItem(loc, new ItemStack(Material.OBSIDIAN));
                        }
                    }
                }
            }
            for (int x = -radius1; x <= radius1; x++) {
                for (int y = -radius1; y <= radius1; y++) {
                    for (int z = -radius1; z <= radius1; z++) {
                        Location loc = location.clone().add(x, y, z);
                        if (loc.getBlock().getType() == Material.ANCIENT_DEBRIS) {
                            if (loc.getBlock().getType() == Material.ANCIENT_DEBRIS) {
                                String id = String.valueOf(regions.get(loc.getBlock().getLocation().toString()));
                                int durability = durabilityMap.get(id) - 1;
                                durabilityMap.put(id, durability);
                                if (durability == 0) {
                                    String uuid = armorStands.get(loc.getBlock().getLocation().toString());
                                    uuid = uuid.replaceAll("\\s", "").replaceAll("\\p{C}", "");
                                    ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(UUID.fromString(uuid));
                                    armorStand.remove();
                                    armorStands.remove(loc.getBlock().getLocation().toString());
                                    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                                    RegionManager manager = container.get(BukkitAdapter.adapt(event.getEntity().getWorld()));
                                    loc.getBlock().setType(Material.AIR);
                                    loc.getBlock().getWorld().playSound(loc.getBlock().getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1f);
                                    loc.getBlock().getWorld().dropItem(loc, new ItemStack(Material.ANCIENT_DEBRIS));
                                    manager.removeRegion(id);
                                    regions.remove(loc.getBlock().getLocation().toString());
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
                                    System.out.println(armorStands);
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
                                    System.out.println(armorStands);
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
                                    System.out.println(armorStands);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void spawnС4TNT(Location location) {
        TNTPrimed tntPrimed = location.getBlock().getWorld().spawn(location.add(0.5, 0, 0.5), TNTPrimed.class);
        tntPrimed.setFuseTicks(140);
        tntPrimed.setCustomName("GLEBBARIO");
    }

    @Override
    public void onDisable() {
        configData.regions.putAll(regions);
        configData.armorStands.putAll(armorStands);
        configData.durabilityMap.putAll(durabilityMap);
        configData.coords.addAll(coords);
        saveTNTConfig();
    }
}