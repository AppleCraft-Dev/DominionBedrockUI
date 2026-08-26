package cn.lunadeer.dominion.bedrockui;

import cn.lunadeer.dominion.bedrockui.command.BedrockUiCommand;
import cn.lunadeer.dominion.bedrockui.dispatch.SelectionBorderListener;
import cn.lunadeer.dominion.bedrockui.dispatch.UiDispatchListener;
import cn.lunadeer.dominion.bedrockui.platform.PlatformService;
import cn.lunadeer.dominion.bedrockui.util.PluginConfig;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * DominionBedrockUI 主类。
 *
 * <p>本插件是 Dominion 领地插件的独立扩展：不修改 Dominion 本体任何代码，
 * 通过 Dominion 公开 API（{@code cn.lunadeer.dominion.api}）读写领地数据，
 * 通过 floodgate API 为基岩版（Geyser）玩家提供表单式 GUI。</p>
 *
 * <p>界面分发策略：</p>
 * <ul>
 *   <li>Java 版玩家：一切保持 Dominion 原有体验（Chest/Dialog UI），本扩展不干预；</li>
 *   <li>基岩版玩家：执行 {@code /dominion}（或无参别名）时自动打开 floodgate 表单界面；
 *       也可直接使用 {@code /dbui} 命令。</li>
 * </ul>
 */
public final class DominionBedrockUI extends JavaPlugin {

    private static DominionBedrockUI instance;

    private PlatformService platformService;
    private PluginConfig pluginConfig;
    private SelectionBorderListener selectionBorderListener;

    public static DominionBedrockUI getInstance() {
        return instance;
    }

    public PlatformService platforms() {
        return platformService;
    }

    public PluginConfig pluginConfig() {
        return pluginConfig;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.pluginConfig = new PluginConfig(this);
        this.platformService = new PlatformService(this);

        if (!platformService.isFloodgateAvailable()) {
            getLogger().warning("未检测到 floodgate 插件，基岩版表单界面将不可用（Java 玩家不受影响）。");
        } else {
            getLogger().info("已检测到 floodgate，基岩版表单界面已启用。");
        }

        // 命令：/dbui（Java 玩家自动转发到 Dominion 原界面）
        BedrockUiCommand.register(this);
        // 分发：拦截 /dominion 无参命令，按玩家客户端类型分发界面
        Bukkit.getPluginManager().registerEvents(new UiDispatchListener(this), this);
        // 选区边框：本体 BlockDisplay 边框基岩端不可见，为基岩玩家用粒子补显
        selectionBorderListener = new SelectionBorderListener(this);
        Bukkit.getPluginManager().registerEvents(selectionBorderListener, this);

        getLogger().info("DominionBedrockUI 已启用（Dominion 基岩版表单扩展）。");
    }

    @Override
    public void onDisable() {
        if (selectionBorderListener != null) {
            selectionBorderListener.shutdown();
        }
        instance = null;
    }
}
