package cn.lunadeer.dominion.bedrockui.command;

import cn.lunadeer.dominion.bedrockui.DominionBedrockUI;
import cn.lunadeer.dominion.bedrockui.menu.MainMenu;
import cn.lunadeer.dominion.bedrockui.util.Lang;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * /dbui 命令：显式打开界面。
 *
 * <p>基岩玩家 → floodgate 表单主菜单；
 * Java 玩家 → 转发到 Dominion 原生命令（/dominion），保持原有体验。</p>
 */
public final class BedrockUiCommand implements CommandExecutor {

    private final DominionBedrockUI plugin;

    private BedrockUiCommand(DominionBedrockUI plugin) {
        this.plugin = plugin;
    }

    public static void register(DominionBedrockUI plugin) {
        PluginCommand command = plugin.getCommand("dbui");
        if (command != null) {
            command.setExecutor(new BedrockUiCommand(plugin));
        } else {
            plugin.getLogger().warning("plugin.yml 中未找到 dbui 命令定义，命令注册失败。");
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("该命令只能由玩家执行。");
            return true;
        }
        if (!player.hasPermission("dominionbedrockui.use")) {
            Lang.send(player, "§c你没有权限使用该命令。");
            return true;
        }

        if (plugin.platforms().isBedrock(player)) {
            MainMenu.open(player);
        } else {
            // Java 玩家：转发到 Dominion 原有主界面
            player.performCommand("dominion");
        }
        return true;
    }
}
