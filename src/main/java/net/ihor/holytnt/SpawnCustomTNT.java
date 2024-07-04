package net.ihor.holytnt;

import org.bukkit.Location;
import org.bukkit.entity.TNTPrimed;

public class SpawnCustomTNT {

    private static void spawnTNT(Location location, int fuseTicks, String customName, float yield) {
        TNTPrimed tntPrimed = location.getBlock().getWorld().spawn(location.add(0.5, 0, 0.5), TNTPrimed.class);
        tntPrimed.setFuseTicks(fuseTicks);
        tntPrimed.setCustomName(customName);
        tntPrimed.setYield(yield);
    }

    public static void spawnC4TNT(Location location) {
        spawnTNT(location, 140, "C4", 4.0f);
    }

    public static void spawnB2TNT(Location location) {
        spawnTNT(location, 200, "B2", 4.0f);
    }

    public static void spawnRVTNT(Location location) {
        spawnTNT(location, 140, "RV", 4.0f);
    }

    public static void spawnLVTNT(Location location) {
        spawnTNT(location, 120, "LV", 4.0f);
    }

    public static void spawnATNT(Location location) {
        spawnTNT(location, 200, "A", 12.0f);
    }

    public static void spawnBTNT(Location location) {
        spawnTNT(location, 200, "B", 40.0f);
    }
}
