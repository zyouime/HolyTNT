package net.ihor.holytnt;

import com.destroystokyo.paper.event.block.TNTPrimeEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.domains.PlayerDomain;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
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
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            loadTNTConfig();
            regions.putAll(configData.regions);
            armorStands.putAll(configData.armorStands);
            durabilityMap.putAll(configData.durabilityMap);
            coordsC4.putAll(configData.coordsC4);
            coordsA.putAll(configData.coordsA);
            coordsLV.putAll(configData.coordsLV);
            coordsRV.putAll(configData.coordsRV);
            coordsB.putAll(configData.coordsB);
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
        ItemStack nbt = event.getItemInHand();
        String id = String.valueOf(UUID.randomUUID());
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
        if (event.getBlock().getType() == Material.ANCIENT_DEBRIS) {
            int radius = 15;
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager manager = container.get(BukkitAdapter.adapt(event.getPlayer().getWorld()));
            BlockVector3 min = BlockVector3.at(event.getBlock().getX() - radius, event.getBlock().getY() - radius, event.getBlock().getZ() - radius);
            BlockVector3 max = BlockVector3.at(event.getBlock().getX() + radius, event.getBlock().getY() + radius, event.getBlock().getZ() + radius);
            ProtectedCuboidRegion region = new ProtectedCuboidRegion(id, min, max);
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
            armorStands.put(event.getBlock().getLocation().toString(), String.valueOf(armorStand.getUniqueId()));
            durabilityMap.put(id, 4);
        }
    }

    @EventHandler
    public void regionRemove(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.ANCIENT_DEBRIS) {
            String id = regions.get(event.getBlock().getLocation().toString());
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager manager = container.get(BukkitAdapter.adapt(event.getPlayer().getWorld()));
            if (event.getPlayer().isOp() || manager.getRegion(id).getOwners().contains(event.getPlayer().getName())) {
                String uuid = armorStands.get(event.getBlock().getLocation().toString());
                event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1f);
                ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(UUID.fromString(uuid));
                if (id != null) {
                    manager.removeRegion(id);
                    regions.remove(event.getBlock().getLocation().toString());
                    configData.regions.remove(event.getBlock().getLocation().toString());
                    armorStand.remove();
                    armorStands.remove(event.getBlock().getLocation().toString());
                    durabilityMap.remove(id);
                    configData.durabilityMap.remove(id);
                    configData.armorStands.remove(event.getBlock().getLocation().toString());
                }
            }
        } else if (event.getBlock().getType() == Material.TNT) {
            String idA = coordsA.get(event.getBlock().getLocation().toString());
            String idB = coordsB.get(event.getBlock().getLocation().toString());
            String idRV = coordsRV.get(event.getBlock().getLocation().toString());
            String idLV = coordsLV.get(event.getBlock().getLocation().toString());
            String idC4 = coordsC4.get(event.getBlock().getLocation().toString());
            if (idA != null) {
                coordsA.remove(event.getBlock().getLocation().toString());
                configData.coordsA.remove(event.getBlock().getLocation().toString());
            }
            if (idB != null) {
                coordsB.remove(event.getBlock().getLocation().toString());
                configData.coordsB.remove(event.getBlock().getLocation().toString());
            }
            if (idRV != null) {
                coordsRV.remove(event.getBlock().getLocation().toString());
                configData.coordsRV.remove(event.getBlock().getLocation().toString());
            }
            if (idLV != null) {
                coordsLV.remove(event.getBlock().getLocation().toString());
                configData.coordsLV.remove(event.getBlock().getLocation().toString());
            }
            if (idC4 != null) {
                coordsC4.remove(event.getBlock().getLocation().toString());
                configData.coordsC4.remove(event.getBlock().getLocation().toString());
            }
        }
    }


    @EventHandler
    public void customTNTDispanser(BlockDispenseEvent event) {
        List<String> lore = event.getItem().getItemMeta().getLore();
        if (lore != null && lore.contains(ColorUtil.msg("&6&lTNT"))) {
            event.setCancelled(true);
            Location location = event.getVelocity().toLocation(event.getBlock().getWorld());
            spawnC4TNT(location);
            event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
        }
    }

    @EventHandler
    public void customTNTUse(TNTPrimeEvent event) {
        if (coordsC4.containsKey(event.getBlock().getLocation().toString())) {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            spawnC4TNT(event.getBlock().getLocation());
            event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
            coordsC4.remove(event.getBlock().getLocation().toString());
            configData.coordsC4.remove(event.getBlock().getLocation().toString());
        }
        if (coordsB.containsKey(event.getBlock().getLocation().toString())) {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            spawnBTNT(event.getBlock().getLocation());
            event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
            coordsB.remove(event.getBlock().getLocation().toString());
            configData.coordsB.remove(event.getBlock().getLocation().toString());
        }
        if (coordsA.containsKey(event.getBlock().getLocation().toString())) {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            spawnATNT(event.getBlock().getLocation());
            event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
            coordsA.remove(event.getBlock().getLocation().toString());
            configData.coordsA.remove(event.getBlock().getLocation().toString());
        }
        if (coordsRV.containsKey(event.getBlock().getLocation().toString())) {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            spawnRVTNT(event.getBlock().getLocation());
            event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
            coordsRV.remove(event.getBlock().getLocation().toString());
            configData.coordsRV.remove(event.getBlock().getLocation().toString());
        }
        if (coordsLV.containsKey(event.getBlock().getLocation().toString())) {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            spawnLVTNT(event.getBlock().getLocation());
            event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
            coordsLV.remove(event.getBlock().getLocation().toString());
            configData.coordsLV.remove(event.getBlock().getLocation().toString());
        }
    }

    @EventHandler
    public void customTNTExplode(EntityExplodeEvent event) {
        if (event.getEntity().getType() == EntityType.PRIMED_TNT && event.getEntity().getCustomName() != null && event.getEntity().getCustomName().equals("C4") && event.getLocation().getBlock().getType() != Material.WATER) {
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
                                String id = String.valueOf(regions.get(loc.getBlock().getLocation().toString()));
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
                            String id = String.valueOf(regions.get(loc.getBlock().getLocation().toString()));
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
            event.setCancelled(true);
            spawnSphere(event.getLocation());
        }
    }

    private void spawnSphere(Location location) {
        HashMap<Location, Material> blocks = new HashMap<>();

        location.setY(location.getY() - 2);
        blocks.put(location.clone(), location.getBlock().getType());
        location.setY(location.getY() + 1);
        blocks.put(location.clone(), location.getBlock().getType());
        location.setX(location.getX() + 1);
        location.setZ(location.getZ() + 1);
        blocks.put(location.clone(), location.getBlock().getType());
        location.setX(location.getX() - 1);
        blocks.put(location.clone(), location.getBlock().getType());
        location.setX(location.getX() - 1);
        blocks.put(location.clone(), location.getBlock().getType());
        location.setZ(location.getZ() - 1);
        blocks.put(location.clone(), location.getBlock().getType());
        location.setZ(location.getZ() - 1);
        blocks.put(location.clone(), location.getBlock().getType());
        location.setX(location.getX() + 1);
        blocks.put(location.clone(), location.getBlock().getType());
        location.setX(location.getX() + 1);
        blocks.put(location.clone(), location.getBlock().getType());
        location.setZ(location.getZ() + 1);
        blocks.put(location.clone(), location.getBlock().getType());
        location.setY(location.getY() + 1);
        location.setX(location.getX() + 1);
        blocks.put(location.clone(), location.getBlock().getType());
        location.setX(location.getX() - 1);
        blocks.put(location.clone(), location.getBlock().getType());
        location.setX(location.getX() - 1);
        blocks.put(location.clone(), location.getBlock().getType());
        location.setX(location.getX() - 1);
        blocks.put(location.clone(), location.getBlock().getType());
        location.setZ(location.getZ() + 1);
        blocks.put(location.clone(), location.getBlock().getType());
        location.setX(location.getX() + 1);
        blocks.put(location.clone(), location.getBlock().getType());
        location.setZ(location.getZ() + 1);
        blocks.put(location.clone(), location.getBlock().getType());
        location.setZ(location.getZ() - 1);
        location.setX(location.getX() + 1);
        blocks.put(location.clone(), location.getBlock().getType());
        location.setZ(location.getZ() - 2);
        blocks.put(location.clone(), location.getBlock().getType());
        location.setX(location.getX() - 1);
        blocks.put(location.clone(), location.getBlock().getType());
        location.setZ(location.getZ() - 1);
        blocks.put(location.clone(), location.getBlock().getType());
        location.setZ(location.getZ() + 1);
        location.setX(location.getX() - 1);
        blocks.put(location.clone(), location.getBlock().getType());
        location.setZ(location.getZ() + 1);
        location.setX(location.getX() - 1);
        blocks.put(location.clone(), location.getBlock().getType());
        location.setX(location.getX() + 1);
        location.setY(location.getY() + 1);
        blocks.put(location.clone(), location.getBlock().getType());
        location.setZ(location.getZ() - 1);
        blocks.put(location.clone(), location.getBlock().getType());
        location.setZ(location.getZ() + 2);
        blocks.put(location.clone(), location.getBlock().getType());
        location.setX(location.getX() + 1);
        blocks.put(location.clone(), location.getBlock().getType());
        location.setX(location.getX() + 1);
        blocks.put(location.clone(), location.getBlock().getType());
        location.setZ(location.getZ() - 1);
        blocks.put(location.clone(), location.getBlock().getType());
        location.setZ(location.getZ() - 1);
        blocks.put(location.clone(), location.getBlock().getType());
        location.setX(location.getX() - 1);
        blocks.put(location.clone(), location.getBlock().getType());
        location.setZ(location.getZ() + 1);
        blocks.put(location.clone(), location.getBlock().getType());
        location.setY(location.getY() + 1);
        blocks.put(location.clone(), location.getBlock().getType());


        for (Map.Entry<Location, Material> entry : blocks.entrySet()) {
            if (entry.getKey().getBlock().getType() == Material.WATER || entry.getKey().getBlock().getType() == Material.AIR) {
                location.getBlock().getWorld().playSound(location, Sound.BLOCK_STONE_PLACE, 1f, 1f);
                entry.getKey().getBlock().setType(Material.PACKED_ICE);
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<Location, Material> entry : blocks.entrySet()) {
                    entry.getKey().getBlock().setType(entry.getValue());
                    location.getBlock().getWorld().playSound(location, Sound.BLOCK_STONE_BREAK, 1f, 1f);
                }
                blocks.clear();
            }
        }.runTaskLater(this, 100);
    }

    private void spawnC4TNT(Location location) {
        TNTPrimed tntPrimed = location.getBlock().getWorld().spawn(location.add(0.5, 0, 0.5), TNTPrimed.class);
        tntPrimed.setFuseTicks(140);
        tntPrimed.setCustomName("C4");
    }

    private void spawnRVTNT(Location location) {
        TNTPrimed tntPrimed = location.getBlock().getWorld().spawn(location.add(0.5, 0, 0.5), TNTPrimed.class);
        tntPrimed.setFuseTicks(140);
        tntPrimed.setCustomName("RV");
    }

    private void spawnLVTNT(Location location) {
        TNTPrimed tntPrimed = location.getBlock().getWorld().spawn(location.add(0.5, 0, 0.5), TNTPrimed.class);
        tntPrimed.setFuseTicks(120);
        tntPrimed.setCustomName("LV");
    }

    private void spawnATNT(Location location) {
        TNTPrimed tntPrimed = location.getBlock().getWorld().spawn(location.add(0.5, 0, 0.5), TNTPrimed.class);
        tntPrimed.setFuseTicks(200);
        tntPrimed.setYield(12);
        tntPrimed.setCustomName("A");
    }

    private void spawnBTNT(Location location) {
        TNTPrimed tntPrimed = location.getBlock().getWorld().spawn(location.add(0.5, 0, 0.5), TNTPrimed.class);
        tntPrimed.setFuseTicks(200);
        tntPrimed.setYield(40);
        tntPrimed.setCustomName("B");
    }

    @Override
    public void onDisable() {
        configData.regions.putAll(regions);
        configData.armorStands.putAll(armorStands);
        configData.durabilityMap.putAll(durabilityMap);
        configData.coordsC4.putAll(coordsC4);
        configData.coordsA.putAll(coordsA);
        configData.coordsB.putAll(coordsB);
        configData.coordsRV.putAll(coordsRV);
        configData.coordsLV.putAll(coordsLV);
        saveTNTConfig();
    }
}