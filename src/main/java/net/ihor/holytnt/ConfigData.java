package net.ihor.holytnt;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigData {

    @Expose
    Map<String, String> coordsC4 = new HashMap<>();

    @Expose
    Map<String, String> coordsRV = new HashMap<>();

    @Expose
    Map<String, String> coordsLV = new HashMap<>();
    @Expose
    Map<String, String> coordsB = new HashMap<>();

    @Expose
    Map<String, String> coordsA = new HashMap<>();

    @Expose
    Map<String, String> coordsB2 = new HashMap<>();

    @Expose
    Map<String, String> regions = new HashMap<>();

    @Expose
    Map<String, String> armorStands = new HashMap<>();

    @Expose
    Map<String, Integer> durabilityMap = new HashMap<>();

    public void setArmorStands(Map<String, String> armorStands) {
        this.armorStands = armorStands;
    }

    public void setCoordsC4(Map<String, String> coords) {
        this.coordsC4 = coords;
    }

    public void setDurabilityMap(Map<String, Integer> durabilityMap) {
        this.durabilityMap = durabilityMap;
    }

    public Map<String, String> getCoordsC4() {
        return coordsC4;
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

    public void setCoordsRV(Map<String, String> coordsRV) {
        this.coordsRV = coordsRV;
    }

    public void setCoordsLV(Map<String, String> coordsLV) {
        this.coordsLV = coordsLV;
    }


    public void setCoordsB(Map<String, String> coordsB) {
        this.coordsB = coordsB;
    }

    public void setCoordsA(Map<String, String> coordsA) {
        this.coordsA = coordsA;
    }

    public Map<String, String> getCoordsRV() {
        return coordsRV;
    }

    public Map<String, String> getCoordsLV() {
        return coordsLV;
    }

    public Map<String, String> getCoordsB() {
        return coordsB;
    }

    public Map<String, String> getCoordsA() {
        return coordsA;
    }

    public Map<String, String> getCoordsB2() {
        return coordsB2;
    }

    public void setCoordsB2(Map<String, String> coordsB2) {
        this.coordsB2 = coordsB2;
    }
}

