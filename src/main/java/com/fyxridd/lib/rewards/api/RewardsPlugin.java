package com.fyxridd.lib.rewards.api;

import com.fyxridd.lib.core.api.ConfigApi;
import com.fyxridd.lib.core.api.CorePlugin;
import com.fyxridd.lib.rewards.RewardsMain;
import com.fyxridd.lib.core.api.CoreApi;
import com.fyxridd.lib.core.api.FormatApi;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class RewardsPlugin extends JavaPlugin{
    public static RewardsPlugin instance;
    public static String pn;
    public static File file;
    public static String dataPath;
    public static String ver;

    public static RewardsMain rewardsMain;

    @Override
    public void onLoad() {
        instance = this;
        file = getFile();
        pn = getName();
        dataPath = getDataFolder().getAbsolutePath();
        ver = CoreApi.getPluginVersion(file);

        //生成文件
        ConfigApi.generateFiles(file, pn);

        //注册hbm
        CorePlugin.registerHbm(new File(dataPath, "RewardsUser.hbm.xml"));
    }

    @Override
    public void onEnable() {
        rewardsMain = new RewardsMain();

        //成功启动
        CoreApi.sendConsoleMessage(FormatApi.get(pn, 25, pn, ver).getText());
    }

    @Override
    public void onDisable() {
        RewardsMain.instance.onDisable();
        //显示插件成功停止信息
        CoreApi.sendConsoleMessage(FormatApi.get(pn, 30, pn, ver).getText());
    }
}
