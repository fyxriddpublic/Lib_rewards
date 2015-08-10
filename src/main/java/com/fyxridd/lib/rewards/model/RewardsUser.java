package com.fyxridd.lib.rewards.model;

import com.fyxridd.lib.items.api.ItemsApi;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RewardsUser implements Serializable{
    private String name;
    private String type;

    private int money;
    private int exp;
    private int level;
    private String tip;
    //可为空列表不为null
    private HashMap<Integer, String> itemsData;

    //临时,不保存到数据库

    //可为空列表不为null
    private HashMap<Integer,ItemStack> itemsHash;

    public RewardsUser(){}

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
        updateItems();
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

    public HashMap<Integer, String> getItemsData() {
        return itemsData;
    }

    public void setItemsData(HashMap<Integer, String> itemsData) {
        this.itemsData = itemsData;
    }

    public HashMap<Integer, ItemStack> getItemsHash() {
        return itemsHash;
    }

    public void setItemsHash(HashMap<Integer, ItemStack> itemsHash) {
        this.itemsHash = itemsHash;
    }

    /**
     * 根据itemsHash更新itemsData
     */
    public void updateItems() {
        this.itemsData = new HashMap<>();
        for (Map.Entry<Integer, ItemStack> entry:itemsHash.entrySet()) {
            itemsData.put(entry.getKey(), ItemsApi.saveItem(entry.getValue()));
        }
    }
}
