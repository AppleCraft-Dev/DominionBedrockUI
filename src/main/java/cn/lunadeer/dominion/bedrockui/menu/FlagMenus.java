package cn.lunadeer.dominion.bedrockui.menu;

import cn.lunadeer.dominion.api.dtos.DominionDTO;
import cn.lunadeer.dominion.api.dtos.GroupDTO;
import cn.lunadeer.dominion.api.dtos.MemberDTO;
import cn.lunadeer.dominion.api.dtos.flag.Flag;
import cn.lunadeer.dominion.api.dtos.flag.Flags;
import cn.lunadeer.dominion.bedrockui.form.Forms;
import cn.lunadeer.dominion.bedrockui.service.DominionService;
import cn.lunadeer.dominion.bedrockui.util.Lang;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 旗标（权限开关）菜单。
 *
 * <p>旗标列表直接复用 Dominion 的旗标注册表（{@link Flags#getAllEnvFlagsEnable()} /
 * {@link Flags#getAllPriFlagsEnable()}），显示名与 Java 版界面一致（由服务端语言文件决定）。
 * 点击某个旗标即切换其开关状态，写操作走 Dominion Provider，与 Java 版逻辑完全对等。</p>
 */
public final class FlagMenus {

    private FlagMenus() {
    }

    /** 切换回调：执行写操作并在结束后重开列表。 */
    private interface FlagToggle {
        void apply(Flag flag, boolean newValue, Runnable reopen, Consumer<String> onError);
    }

    // ------------------------------------------------------------------
    // 环境旗标（领地）
    // ------------------------------------------------------------------

    public static void openEnvFlags(Player player, DominionDTO dominion, Runnable back) {
        List<Flag> flags = new ArrayList<>(Flags.getAllEnvFlagsEnable());
        openFlagList(player, "环境旗标：" + dominion.getName(),
                "§7控制领地内的环境行为（刷怪、爆炸、火灾等），点击切换：",
                flags,
                flag -> dominion.getEnvFlagValue(flagAsEnv(flag)),
                (flag, value, reopen, onError) ->
                        DominionService.setEnvFlag(player, dominion, flagAsEnv(flag), value, reopen, onError),
                () -> openEnvFlags(player, dominion, back), back);
    }

    // ------------------------------------------------------------------
    // 访客权限旗标（领地）
    // ------------------------------------------------------------------

    public static void openGuestFlags(Player player, DominionDTO dominion, Runnable back) {
        List<Flag> flags = new ArrayList<>(Flags.getAllPriFlagsEnable());
        openFlagList(player, "访客权限：" + dominion.getName(),
                "§7控制「非成员」玩家在该领地内可以做什么，点击切换：",
                flags,
                flag -> dominion.getGuestFlagValue(flagAsPri(flag)),
                (flag, value, reopen, onError) ->
                        DominionService.setGuestFlag(player, dominion, flagAsPri(flag), value, reopen, onError),
                () -> openGuestFlags(player, dominion, back), back);
    }

    // ------------------------------------------------------------------
    // 成员权限旗标
    // ------------------------------------------------------------------

    public static void openMemberFlags(Player player, DominionDTO dominion, MemberDTO member,
                                       String memberName, Runnable back) {
        List<Flag> flags = new ArrayList<>(Flags.getAllPriFlagsEnable());
        openFlagList(player, "成员权限：" + memberName,
                "§7控制该成员在领地「" + dominion.getName() + "」内的权限，点击切换：",
                flags,
                flag -> member.getFlagValue(flagAsPri(flag)),
                (flag, value, reopen, onError) ->
                        DominionService.setMemberFlag(player, dominion, member, flagAsPri(flag), value, reopen, onError),
                () -> openMemberFlags(player, dominion, member, memberName, back), back);
    }

    // ------------------------------------------------------------------
    // 权限组旗标
    // ------------------------------------------------------------------

    public static void openGroupFlags(Player player, DominionDTO dominion, GroupDTO group, Runnable back) {
        List<Flag> flags = new ArrayList<>(Flags.getAllPriFlagsEnable());
        openFlagList(player, "权限组：" + group.getNamePlain(),
                "§7该组内所有成员继承以下权限，点击切换：",
                flags,
                flag -> group.getFlagValue(flagAsPri(flag)),
                (flag, value, reopen, onError) ->
                        DominionService.setGroupFlag(player, dominion, group, flagAsPri(flag), value, reopen, onError),
                () -> openGroupFlags(player, dominion, group, back), back);
    }

    // ------------------------------------------------------------------
    // 通用旗标列表
    // ------------------------------------------------------------------

    private static void openFlagList(Player player, String title, String header,
                                     List<Flag> flags,
                                     Function<Flag, Boolean> currentValue,
                                     FlagToggle toggle,
                                     Runnable reopen, Runnable back) {
        List<Forms.MenuButton> buttons = new ArrayList<>();
        for (Flag flag : flags) {
            boolean value;
            try {
                value = Boolean.TRUE.equals(currentValue.apply(flag));
            } catch (Throwable throwable) {
                value = flag.getDefaultValue();
            }
            final boolean current = value;
            String state = current ? Lang.ON : Lang.OFF;
            buttons.add(Forms.MenuButton.of(state + " §0" + flag.getDisplayName(), () ->
                    toggle.apply(flag, !current,
                            () -> {
                                Lang.send(player, Lang.OK_FLAG_UPDATED);
                                reopen.run();
                            },
                            error -> {
                                Lang.send(player, error);
                                reopen.run();
                            })));
        }
        buttons.add(Forms.MenuButton.of(Lang.BACK, back));
        Forms.menu(player, title, header, buttons);
    }

    private static cn.lunadeer.dominion.api.dtos.flag.EnvFlag flagAsEnv(Flag flag) {
        return (cn.lunadeer.dominion.api.dtos.flag.EnvFlag) flag;
    }

    private static cn.lunadeer.dominion.api.dtos.flag.PriFlag flagAsPri(Flag flag) {
        return (cn.lunadeer.dominion.api.dtos.flag.PriFlag) flag;
    }
}
