package cn.lunadeer.dominion.bedrockui.menu;

import cn.lunadeer.dominion.api.dtos.DominionDTO;
import cn.lunadeer.dominion.api.dtos.GroupDTO;
import cn.lunadeer.dominion.api.dtos.MemberDTO;
import cn.lunadeer.dominion.api.dtos.PlayerDTO;
import cn.lunadeer.dominion.bedrockui.form.Forms;
import cn.lunadeer.dominion.bedrockui.service.DominionService;
import cn.lunadeer.dominion.bedrockui.util.Lang;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.CustomForm;

import java.util.ArrayList;
import java.util.List;

/**
 * 成员管理菜单：成员列表、添加成员、成员编辑（权限旗标 / 所属权限组 / 移出领地）。
 */
public final class MemberMenus {

    private MemberMenus() {
    }

    // ------------------------------------------------------------------
    // 成员列表
    // ------------------------------------------------------------------

    public static void openMemberList(Player player, DominionDTO dominion, Runnable back) {
        List<MemberDTO> members = dominion.getMembers();

        List<Forms.MenuButton> buttons = new ArrayList<>();
        buttons.add(Forms.MenuButton.of("§2＋ 添加成员",
                () -> openAddMemberForm(player, dominion, () -> openMemberList(player, dominion, back), null, "")));
        for (MemberDTO member : members) {
            String name = memberName(member);
            String groupTag = groupTag(player, member);
            buttons.add(Forms.MenuButton.of("§1" + name + " §8| §8" + groupTag,
                    () -> openMemberEdit(player, dominion, member, () -> openMemberList(player, dominion, back))));
        }
        buttons.add(Forms.MenuButton.of(Lang.BACK, back));

        String content = members.isEmpty()
                ? "§7领地「" + dominion.getName() + "」还没有成员。"
                : "§7领地「" + dominion.getName() + "」共 " + members.size() + " 名成员，点击管理：";
        Forms.menu(player, "成员管理：" + dominion.getName(), content, buttons);
    }

    // ------------------------------------------------------------------
    // 添加成员
    // ------------------------------------------------------------------

    private static void openAddMemberForm(Player player, DominionDTO dominion, Runnable back,
                                          String error, String lastName) {
        CustomForm.Builder builder = Forms.custom("添加成员");
        if (error != null) {
            builder.label("§c§l错误：§c" + error);
        }
        builder.label("§7输入要添加的玩家游戏ID（该玩家需至少进过一次服务器）。");
        builder.input("玩家名", "输入玩家游戏ID", lastName);
        builder.validResultHandler(response -> {
            // 统一不同 floodgate 版本的下标语义：排除 label 组件后再取值
            response.includeLabels(false);
            // 游标版 asInput() 每次调用都会前进一格，必须只调用一次存入局部变量
            String input = response.asInput();
            String name = input == null ? "" : input.trim();
            if (name.isEmpty()) {
                openAddMemberForm(player, dominion, back, "玩家名不能为空。", lastName);
                return;
            }
            PlayerDTO target = DominionService.findPlayer(name);
            if (target == null) {
                openAddMemberForm(player, dominion, back, Lang.format(Lang.ERR_PLAYER_NOT_FOUND, name), name);
                return;
            }
            DominionService.addMember(player, dominion, target,
                    () -> {
                        Lang.send(player, Lang.format(Lang.OK_MEMBER_ADDED, target.getLastKnownName()));
                        back.run();
                    },
                    err -> openAddMemberForm(player, dominion, back, err, name));
        });
        builder.closedOrInvalidResultHandler(back);
        Forms.send(player, builder.build());
    }

    // ------------------------------------------------------------------
    // 成员编辑
    // ------------------------------------------------------------------

    private static void openMemberEdit(Player player, DominionDTO dominion, MemberDTO member, Runnable back) {
        String name = memberName(member);
        GroupDTO group = DominionService.api().getGroup(member);

        String content = "§7成员：§b" + name + "\n"
                + "§7UUID：§f" + member.getPlayerUUID() + "\n"
                + "§7所属权限组：§f" + (group != null ? group.getNamePlain() : "（无）");

        List<Forms.MenuButton> buttons = new ArrayList<>();
        buttons.add(Forms.MenuButton.of("§3权限旗标设置",
                () -> FlagMenus.openMemberFlags(player, dominion, member, name,
                        () -> openMemberEdit(player, dominion, member, back))));
        buttons.add(Forms.MenuButton.of("§5设置所属权限组",
                () -> openAssignGroupForm(player, dominion, member,
                        () -> openMemberEdit(player, dominion, member, back))));
        buttons.add(Forms.MenuButton.of("§4移出该领地", () ->
                Forms.confirm(player, "移出成员",
                        "§c确定要把成员「" + name + "」移出领地「" + dominion.getName() + "」吗？",
                        "§4确认移出", "§8取消",
                        () -> DominionService.removeMember(player, dominion, member,
                                () -> {
                                    Lang.send(player, Lang.format(Lang.OK_MEMBER_REMOVED, name));
                                    back.run();
                                },
                                err -> {
                                    Lang.send(player, err);
                                    openMemberEdit(player, dominion, member, back);
                                }),
                        () -> openMemberEdit(player, dominion, member, back))));
        buttons.add(Forms.MenuButton.of(Lang.BACK, back));

        Forms.menu(player, "成员：" + name, content, buttons);
    }

    // ------------------------------------------------------------------
    // 设置成员所属权限组（下拉选择）
    // ------------------------------------------------------------------

    private static void openAssignGroupForm(Player player, DominionDTO dominion, MemberDTO member, Runnable back) {
        List<GroupDTO> groups = dominion.getGroups();
        GroupDTO current = DominionService.api().getGroup(member);

        List<String> options = new ArrayList<>();
        options.add("（无分组）");
        int defaultIndex = 0;
        for (int i = 0; i < groups.size(); i++) {
            options.add(groups.get(i).getNamePlain());
            if (current != null && current.getId().equals(groups.get(i).getId())) {
                defaultIndex = i + 1;
            }
        }

        CustomForm.Builder builder = Forms.custom("设置所属权限组");
        builder.label("§7为成员 §b" + memberName(member) + " §7选择一个权限组：");
        builder.dropdown("权限组", defaultIndex, options.toArray(new String[0]));
        builder.validResultHandler(response -> {
            // 统一不同 floodgate 版本的下标语义：排除 label 组件后再取值
            response.includeLabels(false);
            int index = response.asDropdown();
            GroupDTO chosen = index == 0 ? null : groups.get(index - 1);

            // 未变化则直接返回
            if ((current == null && chosen == null)
                    || (current != null && chosen != null && current.getId().equals(chosen.getId()))) {
                back.run();
                return;
            }
            // 先移出旧组（如有），再加入新组（如有）
            Runnable joinNew = () -> {
                if (chosen == null) {
                    Lang.send(player, Lang.OK_FLAG_UPDATED);
                    back.run();
                } else {
                    DominionService.addMemberToGroup(player, dominion, chosen, member,
                            () -> {
                                Lang.send(player, Lang.OK_FLAG_UPDATED);
                                back.run();
                            },
                            err -> {
                                Lang.send(player, err);
                                back.run();
                            });
                }
            };
            if (current != null) {
                DominionService.removeMemberFromGroup(player, dominion, current, member,
                        joinNew,
                        err -> {
                            Lang.send(player, err);
                            back.run();
                        });
            } else {
                joinNew.run();
            }
        });
        builder.closedOrInvalidResultHandler(back);
        Forms.send(player, builder.build());
    }

    // ------------------------------------------------------------------
    // 工具
    // ------------------------------------------------------------------

    static String memberName(MemberDTO member) {
        try {
            String name = member.getPlayer().getLastKnownName();
            if (name != null && !name.isBlank()) {
                return name;
            }
        } catch (Throwable ignored) {
        }
        try {
            return DominionService.api().getPlayerName(member.getPlayerUUID());
        } catch (Throwable throwable) {
            return member.getPlayerUUID().toString();
        }
    }

    private static String groupTag(Player player, MemberDTO member) {
        GroupDTO group = DominionService.api().getGroup(member);
        return group != null ? "组: " + group.getNamePlain() : "无分组";
    }
}
