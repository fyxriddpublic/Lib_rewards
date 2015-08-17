package com.fyxridd.lib.rewards;

import com.fyxridd.lib.core.api.*;
import com.fyxridd.lib.core.api.event.ReloadConfigEvent;
import com.fyxridd.lib.core.api.inter.FancyMessage;
import com.fyxridd.lib.core.api.inter.FunctionInterface;
import com.fyxridd.lib.core.api.inter.TipTransaction;
import com.fyxridd.lib.core.api.inter.TransactionUser;
import com.fyxridd.lib.enchants.api.EnchantsApi;
import com.fyxridd.lib.iconmenu.api.IconMenuApi;
import com.fyxridd.lib.iconmenu.api.Info;
import com.fyxridd.lib.iconmenu.api.OptionClickEvent;
import com.fyxridd.lib.iconmenu.api.OptionClickEventHandler;
import com.fyxridd.lib.items.api.ItemsApi;
import com.fyxridd.lib.rewards.api.RewardsPlugin;
import com.fyxridd.lib.rewards.model.RewardsUser;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;

public class RewardsMain implements Listener, FunctionInterface,OptionClickEventHandler {
    //RewardsUser读取规则:
    //玩家一加入游戏,就检测从数据库中读取所有的RewardsUser到缓存

    private static final String SHORT_DEFAULT = "re_default";
    private static final String SHORT_LONG = "re_long";
    private static final String FUNC_NAME = "Rewards";

    private static ItemMeta IM = new ItemStack(1).getItemMeta();

    public static RewardsMain instance;
    public static Dao dao;

    //配置
    private String adminPer;
    private String usePer;
    private String infoOtherPer;
    private boolean tipRewards;
    private ItemStack pre,get,next,del;
    private int infoPos, prePos, getPos, nextPos, delPos;
    private int infoItem, infoItemSmallId;
    private String infoOwner, infoName, infoGold, infoExp, infoLevel, infoTip;

    //插件,类型,奖励信息
    private HashMap<String, HashMap<String, RewardsInfo>> rewardsHash = new HashMap<>();

    //缓存

    //动态读取
    //玩家名,类型名,奖励
    private HashMap<String, HashMap<String, RewardsUser>> userHash = new HashMap<>();
    //玩家名,类型名,奖励页面(与userHash同步)
    private HashMap<String, HashMap<String, Info>> infoHash = new HashMap<>();

    //需要更新的列表
    private HashSet<RewardsUser> needUpdateList = new HashSet<>();
    //需要删除的列表
    private HashSet<RewardsUser> needDeleteList = new HashSet<>();

    public RewardsMain() {
        instance = this;
        //初始化配置
        initConfig();
        //读取配置文件
        loadConfig();
        dao = new Dao();
        //注册事件
        Bukkit.getPluginManager().registerEvents(this, RewardsPlugin.instance);
        //注册功能
        FuncApi.register(this);
        //计时器: 更新
        Bukkit.getScheduler().scheduleSyncRepeatingTask(CorePlugin.instance, new Runnable() {
            @Override
            public void run() {
                saveAll();
            }
        }, 436, 436);
    }

    public void onDisable() {
        saveAll();
    }

    @EventHandler(priority= EventPriority.LOW)
    public void onReloadConfig(ReloadConfigEvent e) {
        if (e.getPlugin().equals(RewardsPlugin.pn)) loadConfig();
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        //检测初始化
        checkInit(e.getPlayer().getName());
        //提示领取奖励
        if (tipRewards && userHash.get(e.getPlayer().getName()).size() > 0) ShowApi.tip(e.getPlayer(), get(660), true);
    }

    @Override
    public void onOptionClick(OptionClickEvent e) {
        try {
            Info info = e.getInfo();
            int cmd = e.getPos()-info.getInv().getSize();
            if (cmd >= 0) {
                if (cmd == prePos) {
                    Player p = e.getP();
                    Inventory inv = info.getInv(p);
                    int size = info.getInv().getSize();
                    ItemStack infoItem = inv.getItem(size);
                    String tar = infoItem.getItemMeta().getDisplayName().substring(infoOwner.length());
                    int page = infoItem.getAmount()-1;
                    delayShow(p, tar, page);
                    e.setWillClose(true);
                }else if (cmd == getPos) {
                    Player p = e.getP();
                    Inventory inv = info.getInv(p);
                    int size = info.getInv().getSize();
                    ItemStack infoItem = inv.getItem(size);
                    String type = infoItem.getItemMeta().getLore().get(0).substring(infoName.length());
                    delayGet(p, type);
                    e.setWillClose(true);
                }else if (cmd == nextPos) {
                    Player p = e.getP();
                    Inventory inv = info.getInv(p);
                    int size = info.getInv().getSize();
                    ItemStack infoItem = inv.getItem(size);
                    String tar = infoItem.getItemMeta().getDisplayName().substring(infoOwner.length());
                    int page = infoItem.getAmount()+1;
                    delayShow(p, tar, page);
                    e.setWillClose(true);
                }else if (cmd == delPos) {
                    Player p = e.getP();
                    Inventory inv = info.getInv(p);
                    int size = info.getInv().getSize();
                    ItemStack infoItem = inv.getItem(size);
                    String tar = infoItem.getItemMeta().getDisplayName().substring(infoOwner.length());
                    String type = infoItem.getItemMeta().getLore().get(0).substring(infoName.length());
                    //cmd
                    String cmd2 = "/f re a {tar} {type}";
                    //tip
                    List<FancyMessage> tip = new ArrayList<>();
                    tip.add(get(710));
                    //map
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("tar", tar);
                    map.put("type", type);
                    TipTransaction tipTransaction = TransactionApi.newTipTransaction(true, p.getName(), -1, -1, cmd2, tip, map, "type");
                    TransactionUser tu = TransactionApi.getTransactionUser(p.getName());
                    tu.addTransaction(tipTransaction);
                    tu.setRunning(tipTransaction.getId());
                    tipTransaction.updateShow();
                    //
                    e.setWillClose(true);
                }
            }
        } catch (Exception e1) {
            //do nothing
        }
    }

    @Override
    public String getName() {
        return FUNC_NAME;
    }

    /**
     * data为'admin'时使用权限adminPer<br>
     * data为null或其它时使用权限usePer
     */
    @Override
    public boolean isOn(String name, String subFunc) {
        if (subFunc != null && subFunc.equalsIgnoreCase("admin")) return PerApi.has(name, adminPer);
        else return PerApi.has(name, usePer);
    }

    /**
     * 'a 目标玩家 奖励名' 请求删除目标玩家的奖励
     * 'b' 重新读取奖励配置
     * 'c 目标玩家 第几页' 查看目标玩家的奖励列表
     * 'd 目标玩家 插件 奖励类型[ 说明]' 给目标玩家添加奖励
     * 'e 目标玩家 钱 经验 等级[ 说明]' 给目标玩家发奖励(包括物品编辑器中的)
     * 'f 类型' 请求获取未领取的奖励
     */
    @Override
    public void onOperate(Player p, String... args) {
        if (args.length > 0) {
            try {
                //不定长
                if (args.length >= 4) {
                    if (args[0].equalsIgnoreCase("d")) {//给目标玩家添加奖励
                        //权限检测
                        if (!PerApi.checkPer(p, adminPer)) return;
                        //短期间隔
                        if (!SpeedApi.checkShort(p, RewardsPlugin.pn, SHORT_DEFAULT, 2)) return;

                        String tip;
                        if (args.length == 4) tip = null;
                        else tip = CoreApi.combine(args, " ", 4, args.length);
                        if (addRewards(args[1], args[2], args[3], null, tip, true))
                            ShowApi.tip(p, get(685), true);
                        else
                            ShowApi.tip(p, get(690), true);
                        return;
                    }
                    if (args.length >= 5) {
                        if (args[0].equalsIgnoreCase("e")) {//给目标玩家发奖励(包括物品编辑器中的)
                            //短期间隔
                            if (!SpeedApi.checkShort(p, RewardsPlugin.pn, SHORT_DEFAULT, 2)) return;

                            String tip;
                            if (args.length == 5) tip = "";
                            else tip = CoreApi.combine(args, " ", 5, args.length);
                            give(p, args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), tip);
                            return;
                        }
                    }
                }
                //定长
                switch (args.length) {
                    case 1:
                        if (args[0].equalsIgnoreCase("b")) {//重新读取奖励配置
                            //权限检测
                            if (!PerApi.checkPer(p, adminPer)) return;
                            //短期间隔
                            if (!SpeedApi.checkShort(p, RewardsPlugin.pn, SHORT_DEFAULT, 2)) return;

                            loadConfig();
                            ShowApi.tip(p, get(698), true);
                            return;
                        }
                        break;
                    case 2:
                        if (args[0].equalsIgnoreCase("f")) {//请求获取未领取的奖励
                            get(p, args[1]);
                            return;
                        }
                        break;
                    case 3:
                        if (args[0].equalsIgnoreCase("a")) {//请求删除目标玩家的奖励
                            //权限检测
                            if (!PerApi.checkPer(p, adminPer)) return;
                            //短期间隔
                            if (!SpeedApi.checkShort(p, RewardsPlugin.pn, SHORT_DEFAULT, 2)) return;
                            //目标玩家存在性检测
                            String tar = args[1];
                            tar = CoreApi.getRealName(p, tar);
                            if (tar == null) return;
                            //移除
                            if (remove(tar, args[2]))
                                ShowApi.tip(p, get(665), true);
                            else ShowApi.tip(p, get(670), true);
                            return;
                        } else if (args[0].equalsIgnoreCase("c")) {//查看目标玩家的奖励列表
                            showList(p, args[1], Integer.parseInt(args[2]));
                            return;
                        }
                        break;
                }
            } catch (NumberFormatException e) {//数字格式异常
                ShowApi.tip(p, get(10), true);
                return;
            } catch (Exception e) {//操作异常
                ShowApi.tip(p, get(5), true);
                return;
            }
        }
        //输入格式错误
        ShowApi.tip(p, get(40), true);
    }

    /**
     * @see com.fyxridd.lib.rewards.api.RewardsApi#reloadRewards(String, File)
     */
    public void reloadRewards(String plugin, File file) {
        if (plugin == null || file == null) return;
        reloadRewards(plugin, CoreApi.loadConfigByUTF8(file));
    }

    /**
     * @see com.fyxridd.lib.rewards.api.RewardsApi#reloadRewards(String, org.bukkit.configuration.file.YamlConfiguration)
     */
    public void reloadRewards(String plugin, YamlConfiguration config) {
        if (plugin == null || config == null) return;

        rewardsHash.put(plugin, new HashMap<String, RewardsInfo>());

        Map<String, Object> map = config.getValues(false);
        if (map != null) {
            for (String type : map.keySet()) {
                MemorySection ms = (MemorySection) config.get(type);
                //minMoney,maxMoney
                int minMoney = Integer.parseInt(ms.getString("money").split("\\-")[0]);
                int maxMoney = Integer.parseInt(ms.getString("money").split("\\-")[1]);
                //minExp,maxExp
                int minExp = Integer.parseInt(ms.getString("exp").split("\\-")[0]);
                int maxExp = Integer.parseInt(ms.getString("exp").split("\\-")[1]);
                //minLevel,maxLevel
                int minLevel = Integer.parseInt(ms.getString("level").split("\\-")[0]);
                int maxLevel = Integer.parseInt(ms.getString("level").split("\\-")[1]);
                //items
                String s = ms.getString("itemsType");
                String itemsPlugin = null, itemsGetType = null;
                if (s != null) {
                    String[] ss = s.split(":");
                    switch (ss.length) {
                        case 1:
                            itemsPlugin = plugin;
                            itemsGetType = ss[0];
                            break;
                        case 2:
                            itemsPlugin = ss[0];
                            itemsGetType = ss[1];
                            break;
                    }
                }
                //enchants
                s = ms.getString("enchantsType");
                String enchantsPlugin = null, enchantsType = null;
                if (s != null) {
                    String[] ss = s.split(":");
                    switch (ss.length) {
                        case 1:
                            enchantsPlugin = plugin;
                            enchantsType = ss[0];
                            break;
                        case 2:
                            enchantsPlugin = ss[0];
                            enchantsType = ss[1];
                            break;
                    }
                }
                //tip
                String tip = ms.getString("tip");
                //添加
                RewardsInfo rewardsInfo = new RewardsInfo(plugin, type, minMoney, maxMoney, minExp, maxExp,
                        minLevel, maxLevel, itemsPlugin, itemsGetType, enchantsPlugin, enchantsType, tip);
                rewardsHash.get(plugin).put(type, rewardsInfo);
            }
        }
    }

    /**
     * @see com.fyxridd.lib.rewards.api.RewardsApi#addRewards(String, String, String, String, String, boolean)
     */
    public boolean addRewards(String tar, String plugin, String type, String show, String tip, boolean force) {
        if (tar == null || type == null) return false;
        if (plugin == null) plugin = RewardsPlugin.pn;
        //目标玩家存在性检测
        tar = CoreApi.getRealName(null, tar);
        if (tar == null) return false;
        //获取奖励信息
        RewardsInfo info = getRewardsInfo(plugin, type);
        if (info == null) return false;
        //添加
        HashMap<Integer, ItemStack> itemsHash;
        if (info.itemsPlugin == null) itemsHash = null;
        else {
            itemsHash = new HashMap<>();
            List<ItemStack> itemsList = ItemsApi.getItems(info.itemsPlugin, info.itemsGetType);
            int index = 0;
            for (ItemStack is:itemsList) {
                //附魔
                EnchantsApi.addEnchant(info.enchantsPlugin, info.enchantsType, is);
                itemsHash.put(index++, is);
            }
        }
        return addRewards(plugin, show, tar,
                CoreApi.Random.nextInt(info.maxMoney-info.minMoney+1)+info.minMoney,
                CoreApi.Random.nextInt(info.maxExp-info.minExp+1)+info.minExp,
                CoreApi.Random.nextInt(info.maxLevel-info.minLevel+1)+info.minLevel,
                tip, itemsHash, force);
    }

    /**
     * @see com.fyxridd.lib.rewards.api.RewardsApi#addRewards(String, String, String, int, int, int, String, java.util.HashMap, boolean)
     */
    public boolean addRewards(String plugin, String type, String tar, int money, int exp, int level, String tip, HashMap<Integer, ItemStack> itemsHash, boolean force) {
        if (tar == null || money < 0 || exp < 0 || level < 0 || CoreApi.getRealName(null, tar) == null) return false;
        //修正
        if (plugin == null) plugin = RewardsPlugin.pn;
        if (money < 0) money = 0;
        if (exp < 0) exp = 0;
        if (level < 0) level = 0;
        //type修正
        if (type == null) type = getNextName(plugin, tar);
        else type = plugin+"-"+type;
        if (tip == null) tip = get(645).getText();
        if (itemsHash == null) itemsHash = new HashMap<>();
        //保存
        RewardsUser rewardsUser = getRewardsUser(tar, type);
        if (rewardsUser != null && !force) return false;
        if (rewardsUser != null) {
            rewardsUser.setMoney(money);
            rewardsUser.setExp(exp);
            rewardsUser.setLevel(level);
            rewardsUser.setTip(tip);
            rewardsUser.setItemsHash(itemsHash);
            rewardsUser.updateItems();
        }else rewardsUser = new RewardsUser(tar, type, money, exp, level, tip, itemsHash);
        //添加缓存
        addToHash(rewardsUser);
        //添加更新
        needDeleteList.remove(rewardsUser);
        needUpdateList.add(rewardsUser);
        //提示
        CoreApi.sendMsg(tar, get(695), false);
        return true;
    }

    /**
     * 获取奖励信息
     * @param plugin 插件,不为null
     * @param type 类型,不为null
     * @return 奖励信息,没有返回null
     */
    private RewardsInfo getRewardsInfo(String plugin, String type) {
        try {
            return rewardsHash.get(plugin).get(type);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 查看目标玩家未领取的奖励列表
     * @param p 查看者,不为null
     * @param tar 目标玩家,null表示自己
     * @param page 第几页,1-hash.size()
     * @return 是否查看成功
     */
    private boolean showList(Player p, String tar, int page) {
        //短期间隔
        if (!SpeedApi.checkShort(p, RewardsPlugin.pn, SHORT_DEFAULT, 2)) return false;
        //目标玩家存在性检测
        tar = CoreApi.getRealName(p, tar);
        if (tar == null) return false;
        //查看其它玩家奖励列表权限检测
        if (!p.getName().equals(tar) && !PerApi.checkPer(p, infoOtherPer)) return false;
        //检测初始化
        checkInit(tar);
        //目标玩家没有未获取的奖励列表
        HashMap<String, Info> infHash = infoHash.get(tar);
        if (infHash.isEmpty()) {
            ShowApi.tip(p, get(655), true);
            return false;
        }
        int maxPage = infHash.size();
        //页面检测
        if (page < 1 || page > maxPage) {
            ShowApi.tip(p, get(705, maxPage), true);
            return false;
        }
        Info info = getInfo(page-1, infHash);
        if (info != null) {
            String type = getKey(info, infHash);
            if (type == null) return false;//异常
            try {
                RewardsUser ru = userHash.get(tar).get(type);

                //创建操作栏
                Inventory inv = Bukkit.createInventory(p, 9, "none");

                //提示物品
                ItemStack infoItem = new ItemStack(instance.infoItem, page, (short)instance.infoItemSmallId);
                ItemMeta im = infoItem.getItemMeta();
                im.setDisplayName(infoOwner+tar);
                List<String> lore = new ArrayList<>();
                lore.add(infoName+type);
                lore.add(infoGold+ru.getMoney());
                lore.add(infoExp+ru.getExp());
                lore.add(infoLevel+ru.getLevel());
                lore.add(infoTip+ru.getTip());
                im.setLore(lore);
                infoItem.setItemMeta(im);
                inv.setItem(infoPos, infoItem);

                //前一页
                if (page > 1) {
                    ItemStack preItem = pre.clone();
                    inv.setItem(prePos, preItem);
                }

                //获取奖励
                if (tar.equals(p.getName())) {
                    ItemStack getItem = get.clone();
                    inv.setItem(getPos, getItem);
                }

                //后一页
                if (page < maxPage) {
                    ItemStack nextItem = next.clone();
                    inv.setItem(nextPos, nextItem);
                }

                //删除奖励
                if (PerApi.has(p, adminPer)) {
                    ItemStack delItem = del.clone();
                    inv.setItem(delPos, delItem);
                }

                IconMenuApi.open(p, info, null, inv);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    /**
     * 给玩家添加奖励<br>
     * 包括物品编辑器中的物品
     * @param p 命令发出者,不为null
     * @param tar 目标玩家,不为null
     * @param money 钱,>=0
     * @param exp 经验,>=0
     * @param level 等级,>=0
     * @param tip 说明,可为null
     */
    private void give(Player p, String tar, int money, int exp, int level, String tip) {
        //权限检测
        if (!PerApi.checkPer(p, adminPer)) return;
        //目标玩家存在性检测
        tar = CoreApi.getRealName(p, tar);
        if (tar == null) return;
        //修正
        if (money < 0) money = 0;
        if (exp < 0) exp = 0;
        if (level < 0) level = 0;
        if (tip == null) tip = "";
        //物品
        Inventory result = Bukkit.createInventory(null, 36);
        Inventory inv = ItemsApi.getInv(p.getName(), false);
        if (inv != null) {
            int index = 0;
            for (int i=0;i<inv.getSize();i++) {
                ItemStack is = inv.getItem(i);
                if (is != null && !is.getType().equals(Material.AIR)) {
                    result.setItem(index, is.clone());
                    index ++;
                    if (index > 35) break;
                }
            }
        }
        HashMap<Integer, ItemStack> itemsHash = new HashMap<>();
        for (int i=0;i<36;i++) {
            ItemStack is = result.getItem(i);
            if (is != null && !is.getType().equals(Material.AIR)) itemsHash.put(i, is);
        }
        //添加
        if (addRewards(RewardsPlugin.pn, null, tar, money, exp, level, tip, itemsHash , true)) ShowApi.tip(p, get(685), true);
        else ShowApi.tip(p, get(690), true);
    }

    /**
     * 玩家请求获取指定类型的奖励
     * @param p 玩家,不为null
     * @param type 奖励类型,不为null
     */
    private void get(Player p, String type) {
        //短期间隔
        if (!SpeedApi.checkShort(p, RewardsPlugin.pn, SHORT_LONG, 2)) return;
        String name = p.getName();
        //检测初始化
        checkInit(p.getName());
        //奖励类型不存在
        HashMap<String, RewardsUser> rewardsHash = userHash.get(name);
        RewardsUser rewardsInfo = rewardsHash.get(type);
        if (rewardsInfo == null) {
            ShowApi.tip(p, get(635), true);
            return;
        }
        int money = rewardsInfo.getMoney();
        int exp = rewardsInfo.getExp();
        int level = rewardsInfo.getLevel();
        HashMap<Integer, ItemStack> itemsHash = rewardsInfo.getItemsHash();
        //背包空格检测
        PlayerInventory inv = p.getInventory();
        int emptySlots = ItemApi.getEmptySlots(inv);
        //背包空格不够
        if (emptySlots < itemsHash.size()) {
            ShowApi.tip(p, get(50, itemsHash.size()), true);
            return;
        }
        //成功
        //删除
        remove(name, type);
        //money
        if (money > 0) {
            EcoApi.add(p.getName(), money);
            ShowApi.tip(p, get(55, money), true);
        }
        //exp
        if (exp > 0) {
            p.setTotalExperience(p.getTotalExperience()+exp);
            ShowApi.tip(p, get(60, exp), true);
        }
        //level
        if (level > 0) {
            p.setLevel(p.getLevel()+level);
            ShowApi.tip(p, get(65, level), true);
        }
        //item
        for (int i:itemsHash.keySet()) {
            ItemStack is = itemsHash.get(i);
            inv.addItem(is);
            ShowApi.tip(p, get(70, is.getAmount(), NamesApi.getItemName(is)), true);
        }
        //更新背包
        p.updateInventory();
        //tip
        ShowApi.tip(p, get(640), true);
        //检测显示下个列表
        if (rewardsHash.size() > 0) delayShow(p, name, 1);
    }

    /**
     * 移除指定玩家的指定类型奖励
     * @param name 玩家(必须是存在的),不为null
     * @param type 奖励名,不为null
     * @return 是否移除成功
     */
    private boolean remove(String name, String type) {
        //检测初始化
        checkInit(name);
        //从缓存中删除
        HashMap<String, RewardsUser> rewardsHash = userHash.get(name);
        RewardsUser user = rewardsHash.remove(type);
        if (user == null) return false;
        try {
            infoHash.get(name).remove(type);
        } catch (Exception e) {
            //do nothing
        }
        //添加更新
        needDeleteList.add(user);
        needUpdateList.remove(user);
        return true;
    }

    /**
     * 获取下一个未被使用的名字
     * @param plugin 添加奖励的插件名
     * @param tar 玩家名
     * @return 下一个未被使用的名字(完全)
     */
    private String getNextName(String plugin, String tar) {
        //检测初始化
        checkInit(tar);
        //获取
        HashMap<String, RewardsUser> rewardsHash = userHash.get(tar);
        int index = 1;
        while (rewardsHash.containsKey(plugin+"-"+index)) index ++;
        return plugin+"-"+index;
    }

    /**
     * 获取值对应的键
     * @param info 内容,不为null
     * @param hash hash
     * @return 出错返回null
     */
    private String getKey(Info info, HashMap<String, Info> hash) {
        for (Map.Entry<String, Info> entry:hash.entrySet()) {
            if (entry.getValue().equals(info)) return entry.getKey();
        }
        return null;
    }

    /**
     * 获取指定位置的Info
     * @param pos 位置,0到hash的长度-1
     * @param hash hash
     * @return 出错返回null
     */
    private Info getInfo(int pos, HashMap<String, Info> hash) {
        if (hash.size() <= 0) return null;
        if (pos < 0) pos = 0;
        if (pos >= hash.size()) pos = hash.size() -1;
        String key = (String) hash.keySet().toArray()[pos];
        return hash.get(key);
    }

    /**
     * 延时0秒显示
     * @param p 玩家
     * @param tar 查看的目标玩家
     * @param page 页面
     */
    private void delayShow(final Player p, final String tar, final int page) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(RewardsPlugin.instance, new Runnable() {
            @Override
            public void run() {
                if (p.isOnline()) p.chat("/f re c " + tar + " " + page);
            }
        });
    }

    /**
     * 延时0秒获取
     * @param p 玩家
     * @param type 奖励类型
     */
    private void delayGet(final Player p, final String type) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(RewardsPlugin.instance, new Runnable() {
            @Override
            public void run() {
                if (p.isOnline()) p.chat("/f re f "+type);
            }
        });
    }

    /**
     * @return 可能为null
     */
    private RewardsUser getRewardsUser(String name, String type) {
        checkInit(name);
        return userHash.get(name).get(type);
    }

    /**
     * 检测初始化玩家
     */
    private void checkInit(String name) {
        if (!userHash.containsKey(name)) {
            userHash.put(name, new HashMap<String, RewardsUser>());
            infoHash.put(name, new HashMap<String, Info>());
            //从数据库中读取玩家所有的RewardsUser
            for (RewardsUser user:dao.getRewardsUsers(name)) {
                //解析
                HashMap<Integer, ItemStack> itemHash = new HashMap<>();
                for (Map.Entry<Integer, String> entry:user.getItemsData().entrySet()) {
                    itemHash.put(entry.getKey(), ItemsApi.loadItem(entry.getValue()));
                }
                user.setItemsHash(itemHash);
                //添加到缓存
                addToHash(user);
            }
        }
    }

    /**
     * 添加RewardsUser到缓存
     * 同时更新Info
     */
    private void addToHash(RewardsUser rewardsUser) {
        //初始化
        HashMap<String, RewardsUser> rewardsHash = userHash.get(rewardsUser.getName());
        if (rewardsHash == null) {
            rewardsHash = new HashMap<>();
            userHash.put(rewardsUser.getName(), rewardsHash);
        }
        HashMap<String, Info> infHash = infoHash.get(rewardsUser.getName());
        if (infHash == null) {
            infHash = new HashMap<>();
            infoHash.put(rewardsUser.getName(), infHash);
        }
        //添加
        rewardsHash.put(rewardsUser.getType(), rewardsUser);
        Info info = IconMenuApi.register(get(700, rewardsUser.getName()).getText(), 36, false, instance);
        infHash.put(rewardsUser.getType(), info);
        for (Map.Entry<Integer, ItemStack> entry: rewardsUser.getItemsHash().entrySet()) {
            if (entry.getKey() >= 0 && entry.getKey() < 36) {
                info.setItem(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 更新
     */
    private void saveAll() {
        //保存
        if (!needUpdateList.isEmpty()) {
            RewardsMain.dao.saveOrUpdates(needUpdateList);
            needUpdateList.clear();
        }
        //删除
        if (!needDeleteList.isEmpty()) {
            RewardsMain.dao.deletes(needDeleteList);
            needDeleteList.clear();
        }
    }

    private void initConfig() {
        ConfigApi.register(RewardsPlugin.file, RewardsPlugin.dataPath, RewardsPlugin.pn, null);
        ConfigApi.loadConfig(RewardsPlugin.pn);
    }

    private void loadConfig() {
        YamlConfiguration config = ConfigApi.getConfig(RewardsPlugin.pn);

        adminPer = config.getString("per");
        usePer = config.getString("usePer");
        infoOtherPer = config.getString("infoOtherPer");

        tipRewards = config.getBoolean("tip");

        //物品显示
        int id, smallId;
        String name;
        List<String> lore;
        ItemMeta im;

        prePos = config.getInt("showRewards.pre.pos");
        String[] temp = config.getString("showRewards.pre.item").split(":");
        if (temp.length == 2) {
            id = Integer.parseInt(temp[0]);
            smallId = Integer.parseInt(temp[1]);
        }else {
            id = Integer.parseInt(temp[0]);
            smallId = 0;
        }
        name = CoreApi.convert(config.getString("showRewards.pre.name"));
        lore = config.getStringList("showRewards.pre.lore");
        for (int i=0;i<lore.size();i++) lore.set(i, CoreApi.convert(lore.get(i)));
        pre = new ItemStack(id, 1, (short)smallId);
        im = IM.clone();
        im.setDisplayName(name);
        im.setLore(lore);
        pre.setItemMeta(im);

        getPos = config.getInt("showRewards.get.pos");
        temp = config.getString("showRewards.get.item").split(":");
        if (temp.length == 2) {
            id = Integer.parseInt(temp[0]);
            smallId = Integer.parseInt(temp[1]);
        }else {
            id = Integer.parseInt(temp[0]);
            smallId = 0;
        }
        name = CoreApi.convert(config.getString("showRewards.get.name"));
        lore = config.getStringList("showRewards.get.lore");
        for (int i=0;i<lore.size();i++) lore.set(i, CoreApi.convert(lore.get(i)));
        get = new ItemStack(id, 1, (short)smallId);
        im = IM.clone();
        im.setDisplayName(name);
        im.setLore(lore);
        get.setItemMeta(im);

        nextPos = config.getInt("showRewards.next.pos");
        temp = config.getString("showRewards.next.item").split(":");
        if (temp.length == 2) {
            id = Integer.parseInt(temp[0]);
            smallId = Integer.parseInt(temp[1]);
        }else {
            id = Integer.parseInt(temp[0]);
            smallId = 0;
        }
        name = CoreApi.convert(config.getString("showRewards.next.name"));
        lore = config.getStringList("showRewards.next.lore");
        for (int i=0;i<lore.size();i++) lore.set(i, CoreApi.convert(lore.get(i)));
        next = new ItemStack(id, 1, (short)smallId);
        im = IM.clone();
        im.setDisplayName(name);
        im.setLore(lore);
        next.setItemMeta(im);

        delPos = config.getInt("showRewards.del.pos");
        temp = config.getString("showRewards.del.item").split(":");
        if (temp.length == 2) {
            id = Integer.parseInt(temp[0]);
            smallId = Integer.parseInt(temp[1]);
        }else {
            id = Integer.parseInt(temp[0]);
            smallId = 0;
        }
        name = CoreApi.convert(config.getString("showRewards.del.name"));
        lore = config.getStringList("showRewards.del.lore");
        for (int i=0;i<lore.size();i++) lore.set(i, CoreApi.convert(lore.get(i)));
        del = new ItemStack(id, 1, (short)smallId);
        im = IM.clone();
        im.setDisplayName(name);
        im.setLore(lore);
        del.setItemMeta(im);

        infoPos = config.getInt("showRewards.info.pos");
        infoItem = config.getInt("showRewards.info.item");
        infoItemSmallId = config.getInt("showRewards.info.smallId");
        infoOwner = CoreApi.convert(config.getString("showRewards.info.owner"));
        infoName = CoreApi.convert(config.getString("showRewards.info.name"));
        infoGold = CoreApi.convert(config.getString("showRewards.info.gold"));
        infoExp = CoreApi.convert(config.getString("showRewards.info.exp"));
        infoLevel = CoreApi.convert(config.getString("showRewards.info.level"));
        infoTip = CoreApi.convert(config.getString("showRewards.info.tip"));

        //读取奖励信息
        String path = config.getString("path");
        File file = new File(path);
        file.getParentFile().mkdirs();
        YamlConfiguration saveConfig = CoreApi.loadConfigByUTF8(file);
        if (saveConfig == null) {
            ConfigApi.log(RewardsPlugin.pn, "path is error");
            return;
        }
        reloadRewards(RewardsPlugin.pn, saveConfig);
    }

    private static FancyMessage get(int id, Object... args) {
        return FormatApi.get(RewardsPlugin.pn, id, args);
    }
}
