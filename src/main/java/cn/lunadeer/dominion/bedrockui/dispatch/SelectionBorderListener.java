package cn.lunadeer.dominion.bedrockui.dispatch;

import cn.lunadeer.dominion.bedrockui.DominionBedrockUI;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基岩玩家选区边框补显。
 *
 * <p>Dominion 本体在玩家用圈地工具选定两个点后，用 BlockDisplay 实体渲染发光
 * 玻璃长方体显示范围；Geyser 对 Display 实体支持不完整，基岩玩家看不到任何
 * 范围提示。本监听器在 Dominion 选点逻辑（HIGHEST 优先级）执行完成后
 * （MONITOR 阶段）读取 Dominion 内存中的选点结果，为基岩玩家改用粒子沿
 * 长方体 12 条棱持续描边，时长与本体一致（10 秒）。Java 玩家不受任何影响
 * （仍看本体的 BlockDisplay 边框）。</p>
 *
 * <p>Dominion 内部字段（{@code Configuration.selectTool} 与
 * {@code Dominion.pointsSelect}，均为 public static）通过反射只读访问；
 * 反射失败时本功能静默停用，不影响其他功能。</p>
 */
public final class SelectionBorderListener implements Listener {

    /** 总显示时长（tick），与本体 BlockDisplay 边框一致：200 tick = 10 秒。 */
    private static final long DURATION_TICKS = 200L;
    /** 粒子补发间隔（tick）：粒子约 1 秒消散，0.5 秒补一轮保持连贯。 */
    private static final long PERIOD_TICKS = 10L;
    /** 每格棱长的粒子密度，与本体跨界粒子效果默认值一致。 */
    private static final double DENSITY = 4.0;
    /** 棱线散布半径（格），决定线条粗细。 */
    private static final double LINE_RADIUS = 0.15;
    /** 粒子颜色，与本体选区边框默认发光色一致（亮蓝）。 */
    private static final Particle.DustOptions DUST_OPTIONS =
            new Particle.DustOptions(Color.fromRGB(0, 180, 255), 1.0f);

    private final DominionBedrockUI plugin;
    private final Map<UUID, ScheduledTask> borderTasks = new ConcurrentHashMap<>();

    public SelectionBorderListener(DominionBedrockUI plugin) {
        this.plugin = plugin;
    }

    /**
     * MONITOR 阶段触发：此时 Dominion 的选点 handler（HIGHEST）已把新选点写入
     * pointsSelect。ignoreCancelled 保持默认 false——Dominion 处理选点时会把
     * 事件标记为已取消，这里仍需要收到通知。
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onSelectPoint(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!plugin.platforms().isBedrock(player)) {
            return; // Java 玩家：本体边框正常可见，不干预
        }
        Block block = event.getClickedBlock();
        ItemStack item = event.getItem();
        Action action = event.getAction();
        if (block == null || item == null || item.getType() != resolveSelectTool()) {
            return;
        }
        if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Map<Integer, Location> points = readSelectedPoints(player.getUniqueId());
        if (points == null || points.size() != 2) {
            return;
        }
        Location a = points.get(0);
        Location b = points.get(1);
        if (a == null || b == null || a.getWorld() == null || b.getWorld() == null
                || !a.getWorld().equals(b.getWorld())) {
            return; // 与本体一致：两点不在同一世界时不显示
        }
        startBorder(player, a, b);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        stopBorder(event.getPlayer().getUniqueId());
    }

    // ------------------------------------------------------------------
    // 边框渲染
    // ------------------------------------------------------------------

    private void startBorder(Player player, Location a, Location b) {
        stopBorder(player.getUniqueId());
        if (!player.isOnline()) {
            return;
        }
        UUID uuid = player.getUniqueId();
        // 与本体 CuboidDTO 语义一致：两点各轴取 min/max 即长方体两对角
        int x1 = Math.min(a.getBlockX(), b.getBlockX());
        int y1 = Math.min(a.getBlockY(), b.getBlockY());
        int z1 = Math.min(a.getBlockZ(), b.getBlockZ());
        int x2 = Math.max(a.getBlockX(), b.getBlockX());
        int y2 = Math.max(a.getBlockY(), b.getBlockY());
        int z2 = Math.max(a.getBlockZ(), b.getBlockZ());

        renderEdges(player, x1, y1, z1, x2, y2, z2); // 立即先描一轮

        int rounds = (int) (DURATION_TICKS / PERIOD_TICKS);
        int[] round = {0};
        ScheduledTask task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, t -> {
            if (!player.isOnline() || ++round[0] >= rounds) {
                stopBorder(uuid);
                return;
            }
            renderEdges(player, x1, y1, z1, x2, y2, z2);
        }, PERIOD_TICKS, PERIOD_TICKS);
        borderTasks.put(uuid, task);
    }

    private void stopBorder(UUID uuid) {
        ScheduledTask task = borderTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }

    /** 插件卸载时停止所有边框任务。 */
    public void shutdown() {
        borderTasks.values().forEach(ScheduledTask::cancel);
        borderTasks.clear();
    }

    /** 长方体 12 条棱，每条棱一次批量粒子调用。 */
    private void renderEdges(Player player, int x1, int y1, int z1, int x2, int y2, int z2) {
        // 底面 4 条棱
        edge(player, x1, y1, z1, x2, y1, z1);
        edge(player, x1, y1, z2, x2, y1, z2);
        edge(player, x1, y1, z1, x1, y1, z2);
        edge(player, x2, y1, z1, x2, y1, z2);
        // 顶面 4 条棱
        edge(player, x1, y2, z1, x2, y2, z1);
        edge(player, x1, y2, z2, x2, y2, z2);
        edge(player, x1, y2, z1, x1, y2, z2);
        edge(player, x2, y2, z1, x2, y2, z2);
        // 4 条立柱
        edge(player, x1, y1, z1, x1, y2, z1);
        edge(player, x2, y1, z1, x2, y2, z1);
        edge(player, x1, y1, z2, x1, y2, z2);
        edge(player, x2, y1, z2, x2, y2, z2);
    }

    /**
     * 沿一条轴对齐棱散布粒子：以棱中点为中心，沿棱方向偏移半长、另两轴偏移
     * LINE_RADIUS，客户端在偏移盒内随机撒布 count 个粒子拉成线条。
     */
    private void edge(Player player, double sx, double sy, double sz,
                      double ex, double ey, double ez) {
        double dx = ex - sx, dy = ey - sy, dz = ez - sz;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 0.001) {
            return;
        }
        int count = Math.max(1, (int) Math.ceil(len * DENSITY));
        double cx = (sx + ex) / 2.0;
        double cy = (sy + ey) / 2.0;
        double cz = (sz + ez) / 2.0;
        double ax = Math.abs(dx), ay = Math.abs(dy), az = Math.abs(dz);
        double ox, oy, oz;
        if (ax > ay && ax > az) {
            ox = len / 2.0; oy = LINE_RADIUS; oz = LINE_RADIUS;
        } else if (ay > ax && ay > az) {
            ox = LINE_RADIUS; oy = len / 2.0; oz = LINE_RADIUS;
        } else {
            ox = LINE_RADIUS; oy = LINE_RADIUS; oz = len / 2.0;
        }
        player.spawnParticle(Particle.DUST, cx, cy, cz, count, ox, oy, oz, 0, DUST_OPTIONS);
    }

    // ------------------------------------------------------------------
    // Dominion 内部状态读取（反射只读）
    // ------------------------------------------------------------------

    /** Dominion 圈地工具材质（{@code Configuration.selectTool}，public static）。 */
    private Material resolveSelectTool() {
        try {
            Class<?> clazz = Class.forName("cn.lunadeer.dominion.configuration.Configuration");
            String tool = (String) clazz.getField("selectTool").get(null);
            Material material = Material.matchMaterial(tool);
            if (material != null) {
                return material;
            }
        } catch (Throwable ignored) {
        }
        return Material.ARROW; // Dominion 的默认圈地工具
    }

    /** Dominion 内存中的玩家选点（{@code Dominion.pointsSelect}，public static）。 */
    @SuppressWarnings("unchecked")
    private Map<Integer, Location> readSelectedPoints(UUID uuid) {
        try {
            Class<?> clazz = Class.forName("cn.lunadeer.dominion.Dominion");
            Field field = clazz.getField("pointsSelect");
            Map<UUID, Map<Integer, Location>> all =
                    (Map<UUID, Map<Integer, Location>>) field.get(null);
            return all.get(uuid);
        } catch (Throwable throwable) {
            return null;
        }
    }
}
