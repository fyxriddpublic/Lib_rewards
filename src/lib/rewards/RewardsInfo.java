package lib.rewards;

public class RewardsInfo {
    String plugin;
    String type;

    int minMoney, maxMoney;
    int minExp, maxExp;
    int minLevel, maxLevel;

    String itemsPlugin, itemsGetType;
    String enchantsPlugin, enchantsType;

    String tip;

    public RewardsInfo(String plugin, String type,
                       int minMoney, int maxMoney,
                       int minExp, int maxExp,
                       int minLevel, int maxLevel,
                       String itemsPlugin, String itemsGetType,
                       String enchantsPlugin, String enchantsType,
                       String tip) {
        super();
        this.plugin = plugin;
        this.type = type;
        this.minMoney = minMoney;
        this.maxMoney = maxMoney;
        this.minExp = minExp;
        this.maxExp = maxExp;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.tip = tip;
        this.itemsPlugin = itemsPlugin;
        this.itemsGetType = itemsGetType;
        this.enchantsPlugin = enchantsPlugin;
        this.enchantsType = enchantsType;
    }
}
