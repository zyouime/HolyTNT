package net.ihor.holytnt;

import com.google.gson.annotations.Expose;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigData {

    @Expose
    List<Location> coords = new ArrayList<>();

    @Expose
    List<String> armorStandsUUID = new ArrayList<>();

    public void setArmorStandsUUID(List<String> armorStandsUUID) {
        this.armorStandsUUID = armorStandsUUID;
    }

    public List<String> getArmorStandsUUID() {
        return armorStandsUUID;
    }

    @Expose
    List<String> regionsUUID = new ArrayList<>();

    @Expose
    Map<Location, String> regions = new HashMap<>();

    public void setRegions(Map<Location, String> regions) {
        this.regions = regions;
    }

    public Map<Location, String> getRegions() {
        return regions;
    }

    public void setRegionsUUID(List<String> regionsUUID) {
        this.regionsUUID = regionsUUID;
    }

    public List<String> getRegionsUUID() {
        return regionsUUID;
    }

    @Expose
    Map<Location, ArmorStand> armorStands = new HashMap<>();

    @Expose
    Map<String, Integer> durabilityMap = new HashMap<>();

    public void setArmorStands(Map<Location, ArmorStand> armorStands) {
        this.armorStands = armorStands;
    }

    public void setCoords(List<Location> coords) {
        this.coords = coords;
    }

    public void setDurabilityMap(Map<String, Integer> durabilityMap) {
        this.durabilityMap = durabilityMap;
    }

    public List<Location> getCoords() {
        return coords;
    }



    public Map<Location, ArmorStand> getArmorStands() {
        return armorStands;
    }

    public Map<String, Integer> getDurabilityMap() {
        return durabilityMap;
    }
}
