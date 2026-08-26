package cn.lunadeer.dominion.bedrockui.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * 线程调度工具：Dominion 的 Provider 操作均异步返回，
 * 回调完成后需要切回主线程再操作表单/给玩家发消息。
 * 使用 GlobalRegionScheduler，兼容 Paper 与 Folia。
 */
public final class Sync {

    private Sync() {
    }

    public static void run(Plugin plugin, Runnable runnable) {
        Bukkit.getGlobalRegionScheduler().run(plugin, task -> runnable.run());
    }
}
