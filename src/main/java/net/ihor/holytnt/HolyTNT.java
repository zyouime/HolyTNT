package net.ihor.holytnt;

import com.destroystokyo.paper.event.block.TNTPrimeEvent;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
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

import java.util.*;

public final class HolyTNT extends JavaPlugin implements Listener {

    private List<Location> coords = new ArrayList<>();

    private Map<Location, String> regions = new HashMap<>();

    private Map<Location, ArmorStand> armorStands = new HashMap<>();

    private Map<String, Integer> durabilityMap = new HashMap<>();
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("tnt").setExecutor(new CommandEx());
    }

    @EventHandler
    public void customTNTPlace(BlockPlaceEvent event) {
        List<String> lore = event.getItemInHand().getItemMeta().getLore();
        if (lore != null && lore.contains(ColorUtil.msg("&6&lTNT"))) {
            coords.add(event.getBlock().getLocation());
        }
        if (event.getBlock().getType() == Material.ANCIENT_DEBRIS) {
            int radius = 10;
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
            regions.put(event.getBlock().getLocation(), id);
            armorStands.put(event.getBlock().getLocation(), armorStand);
            durabilityMap.put(id, 4);
        }
    }

    @EventHandler
    public void regionRemove(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.ANCIENT_DEBRIS) {
            if (event.getPlayer().isOp()) {
                String id = regions.get(event.getBlock().getLocation());
                event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1f);
                if (id != null) {
                   ArmorStand armorStand = armorStands.get(event.getBlock().getLocation());
                    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                    RegionManager manager = container.get(BukkitAdapter.adapt(event.getPlayer().getWorld()));
                    manager.removeRegion(id);
                    regions.remove(event.getBlock().getLocation());
                    armorStand.remove();
                    armorStands.remove(event.getBlock().getLocation());
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
            spawnTNT(location);
            event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
        }
    }

    @EventHandler
    public void customTNTUse(TNTPrimeEvent event) {
        if (coords.contains(event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            spawnTNT(event.getBlock().getLocation());
            event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
            coords.remove(event.getBlock().getLocation());
        }
    }

    @EventHandler
    public void customTNTExplode(EntityExplodeEvent event) {
        if (event.getEntity().getType() == EntityType.PRIMED_TNT && event.getEntity().getCustomName()!= null && event.getEntity().getCustomName().equals("GLEBBARIO")) {
            Location location = event.getLocation();
            int radius = 3;
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        Location loc = location.clone().add(x, y, z);
                        if (loc.getBlock().getType() == Material.OBSIDIAN) {
                            loc.getBlock().setType(Material.AIR);
                            loc.getBlock().getWorld().dropItem(loc, new ItemStack(Material.OBSIDIAN));
                        }
                        if (loc.getBlock().getType() == Material.ANCIENT_DEBRIS) {
                            String id = regions.get(loc.getBlock().getLocation());
                            int durability = durabilityMap.get(id) - 1;
                            durabilityMap.put(id, durability);
                            if (durability == 0) {
                                ArmorStand armorStand = armorStands.get(loc.getBlock().getLocation());
                                armorStand.remove();
                                armorStands.remove(loc.getBlock().getLocation());
                                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                                RegionManager manager = container.get(BukkitAdapter.adapt(event.getEntity().getWorld()));
                                loc.getBlock().setType(Material.AIR);
                                loc.getBlock().getWorld().playSound(loc.getBlock().getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1f);
                                loc.getBlock().getWorld().dropItem(loc, new ItemStack(Material.ANCIENT_DEBRIS));
                                manager.removeRegion(id);
                                regions.remove(loc.getBlock().getLocation());
                            } else if (durability == 3) {
                                ArmorStand armorStand = armorStands.get(loc.getBlock().getLocation());
                                armorStand.remove();
                                armorStands.remove(loc.getBlock().getLocation());
                                ArmorStand armorStand1 = (ArmorStand) loc.getBlock().getLocation().getWorld().spawnEntity(loc.getBlock().getLocation().add(0.5, 1, 0.5), EntityType.ARMOR_STAND);
                                armorStand1.setGravity(false);
                                armorStand1.setMarker(true);
                                armorStand1.setCustomName(ColorUtil.msg("&e&lГЛЕБИЩЕ " + "&f&l3/4"));
                                armorStand1.setCustomNameVisible(true);
                                armorStand1.setVisible(false);
                                armorStands.put(loc.getBlock().getLocation(), armorStand1);
                                System.out.println(armorStands);
                            } else if (durability == 2) {
                                ArmorStand armorStand = armorStands.get(loc.getBlock().getLocation());
                                armorStand.remove();
                                armorStands.remove(loc.getBlock().getLocation());
                                ArmorStand armorStand1 = (ArmorStand) loc.getBlock().getLocation().getWorld().spawnEntity(loc.getBlock().getLocation().add(0.5, 1, 0.5), EntityType.ARMOR_STAND);
                                armorStand1.setGravity(false);
                                armorStand1.setMarker(true);
                                armorStand1.setCustomName(ColorUtil.msg("&6&lГЛЕБИЩЕ " + "&f&l2/4"));
                                armorStand1.setCustomNameVisible(true);
                                armorStand1.setVisible(false);
                                armorStands.put(loc.getBlock().getLocation(), armorStand1);
                                System.out.println(armorStands);
                            } else if (durability == 1) {
                                ArmorStand armorStand = armorStands.get(loc.getBlock().getLocation());
                                armorStand.remove();
                                armorStands.remove(loc.getBlock().getLocation());
                                ArmorStand armorStand1 = (ArmorStand) loc.getBlock().getLocation().getWorld().spawnEntity(loc.getBlock().getLocation().add(0.5, 1, 0.5), EntityType.ARMOR_STAND);
                                armorStand1.setGravity(false);
                                armorStand1.setMarker(true);
                                armorStand1.setCustomName(ColorUtil.msg("&c&lГЛЕБИЩЕ " + "&f&l1/4"));
                                armorStand1.setCustomNameVisible(true);
                                armorStand1.setVisible(false);
                                armorStands.put(loc.getBlock().getLocation(), armorStand1);
                                System.out.println(armorStands);
                            }
                        }
                    }
                }
            }
        }
    }

    private void spawnTNT(Location location) {
        TNTPrimed tntPrimed = location.getBlock().getWorld().spawn(location.add(0.5, 0, 0.5), TNTPrimed.class);
        tntPrimed.setYield(10);
        tntPrimed.setCustomNameVisible(true);
        tntPrimed.setCustomName("GLEBBARIO");
    }

    @Override
    public void onDisable() {
    }
}