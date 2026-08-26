package cn.lunadeer.dominion.bedrockui.platform;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * 玩家客户端平台检测服务（模块化封装，便于后续维护/替换实现）。
 *
 * <p>检测基于 floodgate API；floodgate 未安装时所有玩家均视为 Java 版，
 * 保证本扩展在缺少 floodgate 的环境下不会影响任何玩家。</p>
 */
public final class PlatformService {

    public enum Platform {
        JAVA,
        BEDROCK
    }

    private final boolean floodgateAvailable;

    public PlatformService(Plugin plugin) {
        boolean available;
        try {
            Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            available = FloodgateBridge.isApiReady();
        } catch (Throwable throwable) {
            available = false;
        }
        this.floodgateAvailable = available;
        if (!available) {
            plugin.getLogger().info("floodgate 不可用，平台检测将把所有玩家视为 Java 版。");
        }
    }

    public boolean isFloodgateAvailable() {
        return floodgateAvailable;
    }

    /**
     * 判断玩家是否为基岩版（通过 Geyser 连接）玩家。
     * 已进行账号关联（linked）的基岩玩家同样会被识别为基岩版，
     * 因为他们实际使用的仍是基岩版客户端，无法打开 Java 箱子界面。
     */
    public boolean isBedrock(Player player) {
        if (!floodgateAvailable || player == null) {
            return false;
        }
        return FloodgateBridge.isFloodgatePlayer(player.getUniqueId());
    }

    public Platform platformOf(Player player) {
        return isBedrock(player) ? Platform.BEDROCK : Platform.JAVA;
    }
}
