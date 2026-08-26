package cn.lunadeer.dominion.bedrockui.dispatch;

import cn.lunadeer.dominion.bedrockui.DominionBedrockUI;
import cn.lunadeer.dominion.bedrockui.menu.MainMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Locale;

/**
 * 界面分发器：按玩家客户端类型自动分发对应的操作界面。
 *
 * <p>当玩家执行无参数的 {@code /dominion}（或配置的别名）时：</p>
 * <ul>
 *   <li>基岩版玩家 → 取消原命令，打开 floodgate 表单主菜单；</li>
 *   <li>Java 版玩家 → 不做任何干预，事件正常传递到 Dominion，
 *       由其打开原有的 Chest/Dialog 界面，体验完全不变。</li>
 * </ul>
 *
 * <p>只拦截「无参数」形式；带子命令的调用（如 /dominion create ...）一律放行，
 * 保证 Dominion 命令体系不受影响。</p>
 */
public final class UiDispatchListener implements Listener {

    private final DominionBedrockUI plugin;

    public UiDispatchListener(DominionBedrockUI plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDominionCommand(PlayerCommandPreprocessEvent event) {
        if (!plugin.pluginConfig().shouldIntercept()) {
            return;
        }
        if (!plugin.platforms().isFloodgateAvailable()) {
            return;
        }

        Player player = event.getPlayer();
        if (!plugin.platforms().isBedrock(player)) {
            return; // Java 玩家：完全不干预
        }

        // 仅匹配「/命令」无参数形式
        String message = event.getMessage().toLowerCase(Locale.ROOT).trim();
        if (!message.startsWith("/")) {
            return;
        }
        String body = message.substring(1).trim();
        if (body.contains(" ")) {
            return; // 带子命令/参数，放行
        }
        // 兼容 plugin.yml 中 "dominion:dom" 形式的命名空间写法
        int colon = body.indexOf(':');
        if (colon >= 0) {
            body = body.substring(colon + 1);
        }
        if (!plugin.pluginConfig().interceptCommands().contains(body)) {
            return;
        }

        event.setCancelled(true);
        MainMenu.open(player);
    }
}
