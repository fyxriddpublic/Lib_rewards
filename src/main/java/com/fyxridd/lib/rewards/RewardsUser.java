package com.fyxridd.lib.rewards;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class RewardsUser {
    private String name;
    private String type;
    private int money;
    private int exp;
    private int level;
    private String tip;
    private HashMap<Integer,ItemStack> itemsHash;
    public RewardsUser(String name, String type, int money, int exp,
                       int level, String tip, HashMap<Integer, ItemStack> itemsHash) {
        super();
        this.name = name;
        this.type = type;
        this.money = money;
        this.exp = exp;
        this.level = level;
        this.tip = tip;
        this.itemsHash = itemsHash;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public HashMap<Integer, ItemStack> getItemsHash() {
        return itemsHash;
    }

    public void setItemsHash(HashMap<Integer, ItemStack> itemsHash) {
        this.itemsHash = itemsHash;
    }
}
