package cn.lunadeer.dominion.bedrockui.platform;

import org.geysermc.floodgate.api.FloodgateApi;

import java.util.UUID;

/**
 * 对 floodgate API 的唯一直接引用点。
 *
 * <p>该类只会在 {@link PlatformService} 确认 floodgate 类存在之后才会被加载，
 * 从而避免 floodgate 缺失时触发 {@link NoClassDefFoundError}。</p>
 */
final class FloodgateBridge {

    private FloodgateBridge() {
    }

    static boolean isApiReady() {
        try {
            return FloodgateApi.getInstance() != null;
        } catch (Throwable throwable) {
            return false;
        }
    }

    static boolean isFloodgatePlayer(UUID uuid) {
        try {
            FloodgateApi api = FloodgateApi.getInstance();
            return api != null && api.isFloodgatePlayer(uuid);
        } catch (Throwable throwable) {
            return false;
        }
    }
}
