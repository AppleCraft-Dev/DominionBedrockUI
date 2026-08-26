package cn.lunadeer.dominion.bedrockui.menu;

import cn.lunadeer.dominion.api.dtos.CuboidDTO;
import cn.lunadeer.dominion.api.dtos.DominionDTO;
import cn.lunadeer.dominion.api.dtos.PlayerDTO;
import cn.lunadeer.dominion.bedrockui.DominionBedrockUI;
import cn.lunadeer.dominion.bedrockui.form.Forms;
import cn.lunadeer.dominion.bedrockui.service.DominionService;
import cn.lunadeer.dominion.bedrockui.util.Lang;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.CustomForm;

import java.util.ArrayList;
import java.util.List;

/**
 * 领地相关菜单：领地列表、领地管理主面板、创建领地表单，
 * 以及改名 / 进出消息 / 转让 等输入表单。
 *
 * <p>配色约定：按钮为白底，按钮文字一律使用深色代码（见 {@link Lang}）。</p>
 */
public final class DominionMenus {

    private DominionMenus() {
    }

    // ------------------------------------------------------------------
    // 领地列表
    // ------------------------------------------------------------------

    /**
     * @param ownedOnly true 表示“我的领地”列表（用于空列表提示文案区分）
     */
    public static void openList(Player player, String title, List<DominionDTO> dominions,
                                boolean ownedOnly, Runnable back) {
        List<Forms.MenuButton> buttons = new ArrayList<>();
        String content;
        if (dominions.isEmpty()) {
            content = ownedOnly ? "§7你还没有任何领地。\n§7可使用下方按钮返回主菜单创建。"
                    : "§7没有可显示的领地。";
        } else {
            content = "§7共 " + dominions.size() + " 个领地，点击进行管理：";
            for (DominionDTO dominion : dominions) {
                String worldName = dominion.getWorld() != null ? dominion.getWorld().getName() : "?";
                buttons.add(Forms.MenuButton.of(
                        "§1" + dominion.getName() + " §8| §8" + worldName,
                        () -> openEntry(player, dominion, () -> openList(player, title, dominions, ownedOnly, back))));
            }
        }
        buttons.add(Forms.MenuButton.of(Lang.BACK, back));
        Forms.menu(player, title, content, buttons);
    }

    /**
     * 领地入口：可管理 → 管理面板；否则 → 访客视图（信息 + 传送）。
     */
    public static void openEntry(Player player, DominionDTO dominion, Runnable back) {
        if (DominionService.canManage(player, dominion)) {
            openManage(player, dominion, back);
        } else {
            openVisitorView(player, dominion, back);
        }
    }

    // ------------------------------------------------------------------
    // 访客视图（非管理者：查看信息 + 传送）
    // ------------------------------------------------------------------

    private static void openVisitorView(Player player, DominionDTO dominion, Runnable back) {
        List<Forms.MenuButton> buttons = new ArrayList<>();
        buttons.add(Forms.MenuButton.of("§2传送到该领地", () ->
                DominionService.teleport(player, dominion,
                        () -> Lang.send(player, Lang.format(Lang.INFO_TELEPORTING, dominion.getName())),
                        error -> {
                            Lang.send(player, error);
                            openVisitorView(player, dominion, back);
                        })));
        buttons.add(Forms.MenuButton.of(Lang.BACK, back));
        Forms.menu(player, "领地：" + dominion.getName(), infoContent(dominion), buttons);
    }

    // ------------------------------------------------------------------
    // 领地管理主面板
    // ------------------------------------------------------------------

    public static void openManage(Player player, DominionDTO dominion, Runnable back) {
        List<Forms.MenuButton> buttons = new ArrayList<>();

        buttons.add(Forms.MenuButton.of("§2传送到该领地", () ->
                DominionService.teleport(player, dominion,
                        () -> Lang.send(player, Lang.format(Lang.INFO_TELEPORTING, dominion.getName())),
                        error -> {
                            Lang.send(player, error);
                            openManage(player, dominion, back);
                        })));
        buttons.add(Forms.MenuButton.of("§1重命名领地",
                () -> openRenameForm(player, dominion, () -> openManage(player, dominion, back), null, dominion.getName())));
        buttons.add(Forms.MenuButton.of("§1设置进入消息",
                () -> openMessageForm(player, dominion, true, () -> openManage(player, dominion, back), null)));
        buttons.add(Forms.MenuButton.of("§1设置离开消息",
                () -> openMessageForm(player, dominion, false, () -> openManage(player, dominion, back), null)));
        buttons.add(Forms.MenuButton.of("§1设置传送点（当前位置）", () ->
                DominionService.setTpLocationHere(player, dominion,
                        () -> {
                            Lang.send(player, Lang.OK_TP_SET);
                            openManage(player, dominion, back);
                        },
                        error -> {
                            Lang.send(player, error);
                            openManage(player, dominion, back);
                        })));
        buttons.add(Forms.MenuButton.of("§3环境旗标设置",
                () -> FlagMenus.openEnvFlags(player, dominion, () -> openManage(player, dominion, back))));
        buttons.add(Forms.MenuButton.of("§3访客权限旗标",
                () -> FlagMenus.openGuestFlags(player, dominion, () -> openManage(player, dominion, back))));
        buttons.add(Forms.MenuButton.of("§9成员管理",
                () -> MemberMenus.openMemberList(player, dominion, () -> openManage(player, dominion, back))));
        buttons.add(Forms.MenuButton.of("§9权限组管理",
                () -> GroupMenus.openGroupList(player, dominion, () -> openManage(player, dominion, back))));
        buttons.add(Forms.MenuButton.of("§4转让领地",
                () -> openTransferForm(player, dominion, () -> openManage(player, dominion, back), null, "")));
        buttons.add(Forms.MenuButton.of("§4删除领地", () ->
                Forms.confirm(player, "删除领地",
                        "§c确定要删除领地「" + dominion.getName() + "」吗？\n§7此操作不可恢复！",
                        "§4确认删除", "§8取消",
                        () -> DominionService.delete(player, dominion,
                                () -> {
                                    Lang.send(player, Lang.format(Lang.OK_DELETED, dominion.getName()));
                                    MainMenu.open(player);
                                },
                                error -> {
                                    Lang.send(player, error);
                                    openManage(player, dominion, back);
                                }),
                        () -> openManage(player, dominion, back))));
        buttons.add(Forms.MenuButton.of(Lang.BACK, back));

        Forms.menu(player, "管理：" + dominion.getName(), infoContent(dominion), buttons);
    }

    static String infoContent(DominionDTO dominion) {
        CuboidDTO cuboid = dominion.getCuboid();
        String worldName = dominion.getWorld() != null ? dominion.getWorld().getName() : "?";
        String ownerName;
        try {
            PlayerDTO owner = dominion.getOwnerDTO();
            ownerName = owner.getLastKnownName();
        } catch (Throwable throwable) {
            ownerName = dominion.getOwner().toString();
        }
        return "§7世界：§f" + worldName
                + "  §7主人：§f" + ownerName + "\n"
                + "§7范围：§f(" + cuboid.x1() + ", " + cuboid.z1() + ") ~ (" + cuboid.x2() + ", " + cuboid.z2() + ")"
                + "  §7尺寸：§f" + cuboid.xLength() + " x " + cuboid.zLength() + "\n"
                + "§7成员：§f" + dominion.getMembers().size()
                + "  §7权限组：§f" + dominion.getGroups().size() + "\n"
                + "§7进入消息：§f" + dominion.getJoinMessage() + "\n"
                + "§7离开消息：§f" + dominion.getLeaveMessage();
    }

    // ------------------------------------------------------------------
    // 创建领地（使用玩家已通过圈地工具选好的两个点，
    // 名称校验后直接转发 Dominion 原生命令 /dominion create <名称>）
    // ------------------------------------------------------------------

    /**
     * 创建领地表单。校验失败时带着错误提示与已填内容重开表单。
     *
     * @param error    上一次校验的错误（null 表示首次打开）
     * @param lastName 上次填写的名称（回显）
     */
    public static void openCreateForm(Player player, Runnable back, String error, String lastName) {
        var config = DominionBedrockUI.getInstance().pluginConfig();

        CustomForm.Builder builder = Forms.custom("创建新领地");
        if (error != null && !error.isBlank()) {
            builder.label("§c§l错误：§c" + error);
        }
        builder.label("§7请先用圈地工具选定§f两个对角点§7，然后在下方输入领地名称提交创建。\n"
                + "§7如尚未选点，请关闭本窗口完成选点后再打开。");
        builder.input("领地名称", "中英文/数字/下划线/连字符，1-32 字符",
                lastName.isEmpty() ? player.getName() + "的领地" : lastName);

        builder.validResultHandler(response -> {
            // 统一不同 floodgate 版本的下标语义：排除 label 组件后再取值
            response.includeLabels(false);
            // 游标版 asInput() 每次调用都会前进一格，必须只调用一次存入局部变量
            String input = response.asInput();
            String name = input == null ? "" : input.trim();

            // 名称校验
            if (!config.dominionNamePattern().matcher(name).matches()) {
                openCreateForm(player, back, Lang.ERR_NAME_INVALID, name);
                return;
            }
            // 交给 Dominion 原生命令处理（使用玩家已选好的点，
            // 未选点、区域重叠、上限、经济等情况由 Dominion 自行反馈）
            DominionService.createDominion(player, name);
        });
        builder.closedOrInvalidResultHandler(back);
        Forms.send(player, builder.build());
    }

    // ------------------------------------------------------------------
    // 重命名 / 消息 / 转让 表单
    // ------------------------------------------------------------------

    private static void openRenameForm(Player player, DominionDTO dominion, Runnable back,
                                       String error, String lastName) {
        var config = DominionBedrockUI.getInstance().pluginConfig();
        CustomForm.Builder builder = Forms.custom("重命名领地");
        if (error != null) {
            builder.label("§c§l错误：§c" + error);
        }
        builder.input("新名称", "中英文/数字/下划线/连字符，1-32 字符", lastName);
        builder.validResultHandler(response -> {
            // 统一不同 floodgate 版本的下标语义：排除 label 组件后再取值
            response.includeLabels(false);
            // 游标版 asInput() 每次调用都会前进一格，必须只调用一次存入局部变量
            String input = response.asInput();
            String name = input == null ? "" : input.trim();
            if (!config.dominionNamePattern().matcher(name).matches()) {
                openRenameForm(player, dominion, back, Lang.ERR_NAME_INVALID, name);
                return;
            }
            DominionService.rename(player, dominion, name,
                    () -> {
                        Lang.send(player, Lang.format(Lang.OK_RENAMED, name));
                        back.run();
                    },
                    err -> openRenameForm(player, dominion, back, err, name));
        });
        builder.closedOrInvalidResultHandler(back);
        Forms.send(player, builder.build());
    }

    /**
     * @param join true=进入消息，false=离开消息
     */
    private static void openMessageForm(Player player, DominionDTO dominion, boolean join,
                                        Runnable back, String error) {
        String current = join ? dominion.getJoinMessage() : dominion.getLeaveMessage();
        CustomForm.Builder builder = Forms.custom(join ? "设置进入消息" : "设置离开消息");
        if (error != null) {
            builder.label("§c§l错误：§c" + error);
        }
        builder.label("§7支持颜色代码（如 §b&b§7）。留空则不显示消息。");
        builder.input("消息内容", "输入消息文本", current);
        builder.validResultHandler(response -> {
            // 统一不同 floodgate 版本的下标语义：排除 label 组件后再取值
            response.includeLabels(false);
            // 游标版 asInput() 每次调用都会前进一格，必须只调用一次存入局部变量
            String input = response.asInput();
            String message = input == null ? "" : input;
            Runnable onSuccess = () -> {
                Lang.send(player, Lang.OK_MESSAGE_SET);
                back.run();
            };
            if (join) {
                DominionService.setJoinMessage(player, dominion, message, onSuccess,
                        err -> openMessageForm(player, dominion, true, back, err));
            } else {
                DominionService.setLeaveMessage(player, dominion, message, onSuccess,
                        err -> openMessageForm(player, dominion, false, back, err));
            }
        });
        builder.closedOrInvalidResultHandler(back);
        Forms.send(player, builder.build());
    }

    private static void openTransferForm(Player player, DominionDTO dominion, Runnable back,
                                         String error, String lastName) {
        CustomForm.Builder builder = Forms.custom("转让领地");
        if (error != null) {
            builder.label("§c§l错误：§c" + error);
        }
        builder.label("§c警告：转让后你将失去该领地的所有权！");
        builder.input("新主人玩家名", "输入对方的游戏ID", lastName);
        builder.validResultHandler(response -> {
            // 统一不同 floodgate 版本的下标语义：排除 label 组件后再取值
            response.includeLabels(false);
            // 游标版 asInput() 每次调用都会前进一格，必须只调用一次存入局部变量
            String input = response.asInput();
            String name = input == null ? "" : input.trim();
            PlayerDTO target = DominionService.findPlayer(name);
            if (target == null) {
                openTransferForm(player, dominion, back, Lang.format(Lang.ERR_PLAYER_NOT_FOUND, name), name);
                return;
            }
            Forms.confirm(player, "确认转让",
                    "§c确定要把领地「" + dominion.getName() + "」转让给 §f" + target.getLastKnownName() + " §c吗？",
                    "§4确认转让", "§8取消",
                    () -> DominionService.transfer(player, dominion, target,
                            () -> {
                                Lang.send(player, Lang.format(Lang.OK_TRANSFERRED, target.getLastKnownName()));
                                MainMenu.open(player);
                            },
                            err -> openTransferForm(player, dominion, back, err, name)),
                    () -> openTransferForm(player, dominion, back, null, name));
        });
        builder.closedOrInvalidResultHandler(back);
        Forms.send(player, builder.build());
    }
}
