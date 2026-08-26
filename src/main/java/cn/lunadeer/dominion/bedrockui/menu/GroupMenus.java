package cn.lunadeer.dominion.bedrockui.menu;

import cn.lunadeer.dominion.api.dtos.DominionDTO;
import cn.lunadeer.dominion.api.dtos.GroupDTO;
import cn.lunadeer.dominion.api.dtos.MemberDTO;
import cn.lunadeer.dominion.bedrockui.form.Forms;
import cn.lunadeer.dominion.bedrockui.service.DominionService;
import cn.lunadeer.dominion.bedrockui.util.Lang;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.CustomForm;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限组（称号组）管理菜单：组列表、创建、重命名、删除、组旗标、组成员管理。
 */
public final class GroupMenus {

    private GroupMenus() {
    }

    // ------------------------------------------------------------------
    // 组列表
    // ------------------------------------------------------------------

    public static void openGroupList(Player player, DominionDTO dominion, Runnable back) {
        List<GroupDTO> groups = dominion.getGroups();

        List<Forms.MenuButton> buttons = new ArrayList<>();
        buttons.add(Forms.MenuButton.of("§2＋ 创建权限组",
                () -> openCreateGroupForm(player, dominion, () -> openGroupList(player, dominion, back), null, "")));
        for (GroupDTO group : groups) {
            buttons.add(Forms.MenuButton.of("§1" + group.getNamePlain(),
                    () -> openGroupEdit(player, dominion, group, () -> openGroupList(player, dominion, back))));
        }
        buttons.add(Forms.MenuButton.of(Lang.BACK, back));

        String content = groups.isEmpty()
                ? "§7领地「" + dominion.getName() + "」还没有权限组。\n§7权限组可让多名成员共享一套权限。"
                : "§7共 " + groups.size() + " 个权限组，点击管理：";
        Forms.menu(player, "权限组：" + dominion.getName(), content, buttons);
    }

    // ------------------------------------------------------------------
    // 创建组
    // ------------------------------------------------------------------

    private static void openCreateGroupForm(Player player, DominionDTO dominion, Runnable back,
                                            String error, String lastName) {
        CustomForm.Builder builder = Forms.custom("创建权限组");
        if (error != null) {
            builder.label("§c§l错误：§c" + error);
        }
        builder.input("组名称", "例如：好友、建筑组", lastName);
        builder.validResultHandler(response -> {
            // 统一不同 floodgate 版本的下标语义：排除 label 组件后再取值
            response.includeLabels(false);
            // 游标版 asInput() 每次调用都会前进一格，必须只调用一次存入局部变量
            String input = response.asInput();
            String name = input == null ? "" : input.trim();
            if (name.isEmpty()) {
                openCreateGroupForm(player, dominion, back, Lang.ERR_GROUP_NAME_EMPTY, lastName);
                return;
            }
            DominionService.createGroup(player, dominion, name,
                    () -> {
                        Lang.send(player, Lang.format(Lang.OK_GROUP_CREATED, name));
                        back.run();
                    },
                    err -> openCreateGroupForm(player, dominion, back, err, name));
        });
        builder.closedOrInvalidResultHandler(back);
        Forms.send(player, builder.build());
    }

    // ------------------------------------------------------------------
    // 组编辑
    // ------------------------------------------------------------------

    private static void openGroupEdit(Player player, DominionDTO dominion, GroupDTO group, Runnable back) {
        List<MemberDTO> members = groupMembers(group);

        String content = "§7权限组：§b" + group.getNamePlain() + "\n"
                + "§7组成员数：§f" + members.size();

        List<Forms.MenuButton> buttons = new ArrayList<>();
        buttons.add(Forms.MenuButton.of("§3组权限旗标",
                () -> FlagMenus.openGroupFlags(player, dominion, group,
                        () -> openGroupEdit(player, dominion, group, back))));
        buttons.add(Forms.MenuButton.of("§9组成员管理",
                () -> openGroupMembers(player, dominion, group,
                        () -> openGroupEdit(player, dominion, group, back))));
        buttons.add(Forms.MenuButton.of("§5重命名",
                () -> openRenameGroupForm(player, dominion, group,
                        () -> openGroupEdit(player, dominion, group, back), null, group.getNamePlain())));
        buttons.add(Forms.MenuButton.of("§4删除权限组", () ->
                Forms.confirm(player, "删除权限组",
                        "§c确定要删除权限组「" + group.getNamePlain() + "」吗？\n§7组内成员将被移出该组。",
                        "§4确认删除", "§8取消",
                        () -> DominionService.deleteGroup(player, dominion, group,
                                () -> {
                                    Lang.send(player, Lang.format(Lang.OK_GROUP_DELETED, group.getNamePlain()));
                                    back.run();
                                },
                                err -> {
                                    Lang.send(player, err);
                                    openGroupEdit(player, dominion, group, back);
                                }),
                        () -> openGroupEdit(player, dominion, group, back))));
        buttons.add(Forms.MenuButton.of(Lang.BACK, back));

        Forms.menu(player, "权限组：" + group.getNamePlain(), content, buttons);
    }

    // ------------------------------------------------------------------
    // 组成员管理
    // ------------------------------------------------------------------

    private static void openGroupMembers(Player player, DominionDTO dominion, GroupDTO group, Runnable back) {
        List<MemberDTO> members = groupMembers(group);

        List<Forms.MenuButton> buttons = new ArrayList<>();
        buttons.add(Forms.MenuButton.of("§2＋ 添加成员到本组",
                () -> openAddToGroupForm(player, dominion, group,
                        () -> openGroupMembers(player, dominion, group, back))));
        for (MemberDTO member : members) {
            String name = MemberMenus.memberName(member);
            buttons.add(Forms.MenuButton.of("§4移出：" + name, () ->
                    DominionService.removeMemberFromGroup(player, dominion, group, member,
                            () -> {
                                Lang.send(player, Lang.OK_FLAG_UPDATED);
                                openGroupMembers(player, dominion, group, back);
                            },
                            err -> {
                                Lang.send(player, err);
                                openGroupMembers(player, dominion, group, back);
                            })));
        }
        buttons.add(Forms.MenuButton.of(Lang.BACK, back));

        String content = members.isEmpty()
                ? "§7组「" + group.getNamePlain() + "」内还没有成员。"
                : "§7组「" + group.getNamePlain() + "」共 " + members.size() + " 名成员：";
        Forms.menu(player, "组成员：" + group.getNamePlain(), content, buttons);
    }

    /**
     * 从领地成员中选择尚未加入本组的人加入（下拉选择）。
     */
    private static void openAddToGroupForm(Player player, DominionDTO dominion, GroupDTO group, Runnable back) {
        List<MemberDTO> candidates = new ArrayList<>();
        List<String> options = new ArrayList<>();
        for (MemberDTO member : dominion.getMembers()) {
            if (!group.getId().equals(member.getGroupId())) {
                candidates.add(member);
                options.add(MemberMenus.memberName(member));
            }
        }
        if (candidates.isEmpty()) {
            Lang.send(player, "§e领地内没有可加入本组的成员（请先添加领地成员）。");
            back.run();
            return;
        }

        CustomForm.Builder builder = Forms.custom("添加成员到：" + group.getNamePlain());
        builder.dropdown("选择成员", 0, options.toArray(new String[0]));
        builder.validResultHandler(response -> {
            // 统一不同 floodgate 版本的下标语义：排除 label 组件后再取值
            response.includeLabels(false);
            MemberDTO chosen = candidates.get(response.asDropdown());
            DominionService.addMemberToGroup(player, dominion, group, chosen,
                    () -> {
                        Lang.send(player, Lang.OK_FLAG_UPDATED);
                        back.run();
                    },
                    err -> {
                        Lang.send(player, err);
                        back.run();
                    });
        });
        builder.closedOrInvalidResultHandler(back);
        Forms.send(player, builder.build());
    }

    // ------------------------------------------------------------------
    // 重命名组
    // ------------------------------------------------------------------

    private static void openRenameGroupForm(Player player, DominionDTO dominion, GroupDTO group,
                                            Runnable back, String error, String lastName) {
        CustomForm.Builder builder = Forms.custom("重命名权限组");
        if (error != null) {
            builder.label("§c§l错误：§c" + error);
        }
        builder.input("新名称", "输入新的组名称", lastName);
        builder.validResultHandler(response -> {
            // 统一不同 floodgate 版本的下标语义：排除 label 组件后再取值
            response.includeLabels(false);
            // 游标版 asInput() 每次调用都会前进一格，必须只调用一次存入局部变量
            String input = response.asInput();
            String name = input == null ? "" : input.trim();
            if (name.isEmpty()) {
                openRenameGroupForm(player, dominion, group, back, Lang.ERR_GROUP_NAME_EMPTY, lastName);
                return;
            }
            DominionService.renameGroup(player, dominion, group, name,
                    () -> {
                        Lang.send(player, Lang.format(Lang.OK_GROUP_RENAMED, name));
                        back.run();
                    },
                    err -> openRenameGroupForm(player, dominion, group, back, err, name));
        });
        builder.closedOrInvalidResultHandler(back);
        Forms.send(player, builder.build());
    }

    // ------------------------------------------------------------------
    // 工具
    // ------------------------------------------------------------------

    private static List<MemberDTO> groupMembers(GroupDTO group) {
        try {
            return group.getMembers();
        } catch (Throwable throwable) {
            return List.of();
        }
    }
}
