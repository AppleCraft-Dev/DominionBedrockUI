package cn.lunadeer.dominion.bedrockui.util;

import org.bukkit.entity.Player;

/**
 * 集中管理的界面文案（简体中文）。
 * 所有表单文本统一从这里取用，便于后续做多语言扩展。
 *
 * <p>配色注意：基岩版表单的「按钮」是白底，按钮文字只能用深色代码
 * （§0-§6、§8、§9），浅色（§a-§f、§7）在白底上无法阅读；
 * 「内容区」是黑底，可正常使用浅色代码。</p>
 */
public final class Lang {

    private Lang() {
    }

    public static final String PREFIX = "§6[§eDominion§6]§f ";

    // ===== 通用（按钮文字：深色系） =====
    public static final String BACK = "§8◀ 返回上一级";
    public static final String CLOSE_HINT = "关闭此窗口不会执行任何操作。";
    public static final String ON = "§2[开启]";
    public static final String OFF = "§4[关闭]";

    // ===== 主菜单（按钮文字：深色系） =====
    public static final String MAIN_TITLE = "Dominion 领地系统";
    public static final String BTN_MY_DOMINIONS = "§1我的领地";
    public static final String BTN_ADMIN_DOMINIONS = "§5我参与管理的领地";
    public static final String BTN_CREATE = "§2创建新领地";
    public static final String BTN_CURRENT = "§9管理当前所在领地";
    public static final String BTN_HELP = "§8使用帮助";

    // ===== 反馈（聊天栏，可用浅色） =====
    public static final String ERR_NO_FLOODGATE = "服务器未安装 floodgate，无法使用基岩版界面。";
    public static final String ERR_NO_DOMINION_API = "Dominion 插件未就绪，请稍后再试。";
    public static final String ERR_OPERATION_FAILED = "操作失败：可能权限不足、达到数量上限或名称被占用。";
    public static final String ERR_NOT_IN_DOMINION = "你当前不在任何领地内。";
    public static final String ERR_NAME_INVALID = "领地名称不合法：仅支持中英文、数字、下划线、连字符，长度 1-32。";
    public static final String ERR_PLAYER_NOT_FOUND = "找不到玩家「%s」（该玩家可能从未进入过本服务器）。";
    public static final String ERR_GROUP_NAME_EMPTY = "权限组名称不能为空。";

    public static final String OK_DELETED = "§a领地「%s」已删除。";
    public static final String OK_RENAMED = "§a领地已重命名为「%s」。";
    public static final String OK_MESSAGE_SET = "§a消息已更新。";
    public static final String OK_TP_SET = "§a传送点已设置为当前位置。";
    public static final String OK_TRANSFERRED = "§a领地已转让给「%s」。";
    public static final String OK_MEMBER_ADDED = "§a已将「%s」添加为成员。";
    public static final String OK_MEMBER_REMOVED = "§a已将成员「%s」移出领地。";
    public static final String OK_GROUP_CREATED = "§a权限组「%s」创建成功。";
    public static final String OK_GROUP_DELETED = "§a权限组「%s」已删除。";
    public static final String OK_GROUP_RENAMED = "§a权限组已重命名为「%s」。";
    public static final String OK_FLAG_UPDATED = "§a权限设置已更新。";
    public static final String INFO_TELEPORTING = "§e正在传送到领地「%s」...";
    public static final String ERR_TELEPORT_FAILED = "传送失败：可能领地跨服不可用或被阻止。";

    // ===== 内容区（黑底，可用浅色） =====
    public static final String HELP_CONTENT =
            "§b· 我的领地：查看并管理你拥有的领地\n" +
            "§b· 创建新领地：先用圈地工具选好两个点，再输入名称创建\n" +
            "§b· 管理当前所在领地：直接管理脚下这片领地\n" +
            "§7在领地管理中可以设置进出消息、传送点、\n" +
            "§7环境旗标、访客权限、成员与权限组。";

    public static void send(Player player, String message) {
        player.sendMessage(PREFIX + message);
    }

    public static void send(org.bukkit.command.CommandSender sender, String message) {
        sender.sendMessage(PREFIX + message);
    }

    public static String format(String template, Object... args) {
        return String.format(template, args);
    }
}
