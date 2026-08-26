package cn.lunadeer.dominion.bedrockui.menu;

import cn.lunadeer.dominion.api.dtos.DominionDTO;
import cn.lunadeer.dominion.bedrockui.form.Forms;
import cn.lunadeer.dominion.bedrockui.service.DominionService;
import cn.lunadeer.dominion.bedrockui.util.Lang;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 基岩版主菜单（对应 Java 版 /dominion 打开的 RootMenu）。
 */
public final class MainMenu {

    private MainMenu() {
    }

    public static void open(Player player) {
        if (!DominionService.isApiReady()) {
            Lang.send(player, Lang.ERR_NO_DOMINION_API);
            return;
        }

        DominionDTO current = DominionService.currentDominion(player);

        StringBuilder content = new StringBuilder();
        content.append("§f你好，§b").append(player.getName()).append("§f！\n");
        if (current != null) {
            content.append("§7当前所在领地：§e").append(current.getName()).append("\n");
        } else {
            content.append("§7当前不在任何领地内。\n");
        }

        List<Forms.MenuButton> buttons = new ArrayList<>();
        buttons.add(Forms.MenuButton.of(Lang.BTN_MY_DOMINIONS,
                () -> DominionMenus.openList(player, "我的领地",
                        DominionService.ownDominions(player), true, () -> open(player))));
        buttons.add(Forms.MenuButton.of(Lang.BTN_ADMIN_DOMINIONS,
                () -> DominionMenus.openList(player, "我参与管理的领地",
                        DominionService.adminDominions(player), false, () -> open(player))));
        buttons.add(Forms.MenuButton.of(Lang.BTN_CREATE,
                () -> DominionMenus.openCreateForm(player, () -> open(player), null, "")));
        if (current != null) {
            buttons.add(Forms.MenuButton.of(Lang.BTN_CURRENT,
                    () -> DominionMenus.openEntry(player, current, () -> open(player))));
        }
        buttons.add(Forms.MenuButton.of(Lang.BTN_HELP,
                () -> Forms.menu(player, "使用帮助", Lang.HELP_CONTENT,
                        List.of(Forms.MenuButton.of(Lang.BACK, () -> open(player))))));

        Forms.menu(player, Lang.MAIN_TITLE, content.toString(), buttons);
    }
}
