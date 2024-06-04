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
    List<String> coords = new ArrayList<>();

    @Expose
    Map<String, String> regions = new HashMap<>();

    @Expose
    Map<String, String> armorStands = new HashMap<>();

    @Expose
    Map<String, Integer> durabilityMap = new HashMap<>();

    public void setArmorStands(Map<String, String> armorStands) {
        this.armorStands = armorStands;
    }

    public void setCoords(List<String> coords) {
        this.coords = coords;
    }

    public void setDurabilityMap(Map<String, Integer> durabilityMap) {
        this.durabilityMap = durabilityMap;
    }

    public List<String> getCoords() {
        return coords;
    }

    public Map<String, String> getArmorStands() {
        return armorStands;
    }

    public Map<String, Integer> getDurabilityMap() {
        return durabilityMap;
    }

    public void setRegions(Map<String, String> regions) {
        this.regions = regions;
    }

    public Map<String, String> getRegions() {
        return regions;
    }
}

