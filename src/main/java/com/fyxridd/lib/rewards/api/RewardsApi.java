package com.fyxridd.lib.rewards.api;

import com.fyxridd.lib.rewards.RewardsMain;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class RewardsApi {
    /**
     * 重新读取奖励配置信息
     * 会读取插件数据文件夹下rewards.yml文件
     */
    public static void reloadRewards(String plugin) {
        RewardsMain.instance.reloadRewards(plugin);
    }

    /**
     * 给目标玩家添加奖励
     * @param plugin 添加奖励的插件名,null表示rewards插件名
     * @param type null表示名字随意(注意:不能使用数字作类型名,因为数字已被预留)
     * @param tar 目标玩家,可为null(null或玩家不存在时返回false)
     * @param money 金币,>=0(<0时返回false)
     * @param exp 经验,>=0(<0时返回false)
     * @param level 等级,>=0(<0时返回false)
     * @param tip 奖励说明,可为null
     * @param itemsHash 物品,可为null
     * @param force 在type不为null的情况下如果已经有此类型奖励未领取是否强制覆盖原来的奖励
     * @param direct 是否直接添加到背包,true时先尝试添加到背包,添加不了的再放到奖励列表;false时添加到奖励列表
     * @return 是否添加成功
     */
    public static boolean addRewards(String plugin, String type, String tar, int money, int exp, int level, String tip, HashMap<Integer, ItemStack> itemsHash, boolean force, boolean direct) {
        return RewardsMain.instance.addRewards(plugin, type, tar, money, exp, level, tip, itemsHash, force, direct);
    }

    /**
     * 给玩家添加指定类型的奖励
     * @param tar 目标玩家,可为null(null时返回false)
     * @param plugin 插件名,可为null,null时使用rewards插件名
     * @param type 奖励名,可为null(null时返回false)
     * @param show 奖励显示名,null表示名字随意(注意:不能使用数字作类型名,因为数字已被预留)
     * @param force 在show不为null的情况下如果已经有此类型奖励未领取是否强制覆盖原来的奖励
     * @param direct 是否直接添加到背包,true时先尝试添加到背包,添加不了的再放到奖励列表;false时添加到奖励列表
     * @return 是否添加成功
     */
    public static boolean addRewards(String tar, String plugin, String type, String show, boolean force, boolean direct) {
        return RewardsMain.instance.addRewards(tar, plugin, type, show, force, direct);
    }
}
