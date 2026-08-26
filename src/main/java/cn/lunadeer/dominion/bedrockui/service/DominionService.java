package cn.lunadeer.dominion.bedrockui.service;

import cn.lunadeer.dominion.api.DominionAPI;
import cn.lunadeer.dominion.api.dtos.DominionDTO;
import cn.lunadeer.dominion.api.dtos.GroupDTO;
import cn.lunadeer.dominion.api.dtos.MemberDTO;
import cn.lunadeer.dominion.api.dtos.PlayerDTO;
import cn.lunadeer.dominion.api.dtos.flag.EnvFlag;
import cn.lunadeer.dominion.api.dtos.flag.PriFlag;
import cn.lunadeer.dominion.bedrockui.DominionBedrockUI;
import cn.lunadeer.dominion.bedrockui.util.Lang;
import cn.lunadeer.dominion.bedrockui.util.Sync;
import cn.lunadeer.dominion.events.dominion.modify.DominionSetMessageEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Dominion 数据服务层。
 *
 * <p>所有对领地数据的读写都经过本类，且全部调用 Dominion 公开 API
 * （{@link DominionAPI} 与各 Provider）。Provider 内部执行与 Java 版命令/界面
 * 完全相同的校验、事件触发与数据库写入，因此两种客户端的操作结果天然互通一致。</p>
 *
 * <p>写操作均为异步（Provider 返回 {@link CompletableFuture}），
 * 本类统一在完成后切回主线程再执行界面回调。</p>
 */
public final class DominionService {

    private DominionService() {
    }

    // ------------------------------------------------------------------
    // 查询
    // ------------------------------------------------------------------

    public static boolean isApiReady() {
        try {
            return DominionAPI.getInstance() != null;
        } catch (Throwable throwable) {
            return false;
        }
    }

    public static DominionAPI api() {
        return DominionAPI.getInstance();
    }

    /** 玩家拥有的领地。 */
    public static List<DominionDTO> ownDominions(Player player) {
        return api().getPlayerOwnDominionDTOs(player.getUniqueId());
    }

    /** 玩家拥有管理权限（非拥有者）的领地。 */
    public static List<DominionDTO> adminDominions(Player player) {
        return api().getPlayerAdminDominionDTOs(player.getUniqueId());
    }

    /** 玩家当前所在的领地（可能为 null）。 */
    public static @Nullable DominionDTO currentDominion(Player player) {
        try {
            return api().getPlayerCurrentDominion(player);
        } catch (Throwable throwable) {
            return api().getDominion(player.getLocation());
        }
    }

    public static @Nullable DominionDTO dominionAt(Location location) {
        return api().getDominion(location);
    }

    /** 玩家是否为领地拥有者。 */
    public static boolean isOwner(Player player, DominionDTO dominion) {
        return dominion.getOwner().equals(player.getUniqueId());
    }

    /** 玩家是否可管理该领地（拥有者或服主权限）。 */
    public static boolean canManage(Player player, DominionDTO dominion) {
        return isOwner(player, dominion) || player.hasPermission("dominion.admin");
    }

    // ------------------------------------------------------------------
    // 领地写操作
    // ------------------------------------------------------------------

    /**
     * 创建领地：转发 Dominion 原生命令，使用玩家已通过圈地工具选好的两个点。
     * 未选点、区域重叠、数量上限、经济扣费等情况均由 Dominion 自行校验并反馈，
     * 与 Java 版执行 /dominion create 的行为完全一致。
     */
    public static void createDominion(Player player, String name) {
        player.performCommand("dominion create " + name);
    }

    public static void rename(Player player, DominionDTO dominion, String newName,
                              Runnable onSuccess, Consumer<String> onError) {
        handle(DominionAPI.getDominionProvider().renameDominion(player, dominion, newName),
                player, dto -> onSuccess.run(), onError);
    }

    public static void setJoinMessage(Player player, DominionDTO dominion, String message,
                                      Runnable onSuccess, Consumer<String> onError) {
        handle(DominionAPI.getDominionProvider().setDominionMessage(
                        player, dominion, DominionSetMessageEvent.TYPE.ENTER, message),
                player, dto -> onSuccess.run(), onError);
    }

    public static void setLeaveMessage(Player player, DominionDTO dominion, String message,
                                       Runnable onSuccess, Consumer<String> onError) {
        handle(DominionAPI.getDominionProvider().setDominionMessage(
                        player, dominion, DominionSetMessageEvent.TYPE.LEAVE, message),
                player, dto -> onSuccess.run(), onError);
    }

    public static void setTpLocationHere(Player player, DominionDTO dominion,
                                         Runnable onSuccess, Consumer<String> onError) {
        handle(DominionAPI.getDominionProvider().setDominionTpLocation(player, dominion, player.getLocation()),
                player, dto -> onSuccess.run(), onError);
    }

    public static void teleport(Player player, DominionDTO dominion,
                                Runnable onSuccess, Consumer<String> onError) {
        handle(DominionAPI.getTeleportProvider().teleport(player, dominion),
                player, ok -> {
                    if (Boolean.TRUE.equals(ok)) {
                        onSuccess.run();
                    } else {
                        onError.accept(Lang.ERR_TELEPORT_FAILED);
                    }
                }, onError);
    }

    public static void delete(Player player, DominionDTO dominion,
                              Runnable onSuccess, Consumer<String> onError) {
        handle(DominionAPI.getDominionProvider().deleteDominion(player, dominion, false, true),
                player, dto -> onSuccess.run(), onError);
    }

    public static void transfer(Player player, DominionDTO dominion, PlayerDTO newOwner,
                                Runnable onSuccess, Consumer<String> onError) {
        handle(DominionAPI.getDominionProvider().transferDominion(player, dominion, newOwner, false),
                player, dto -> onSuccess.run(), onError);
    }

    // ------------------------------------------------------------------
    // 旗标
    // ------------------------------------------------------------------

    public static void setEnvFlag(Player player, DominionDTO dominion, EnvFlag flag, boolean value,
                                  Runnable onSuccess, Consumer<String> onError) {
        handle(DominionAPI.getDominionProvider().setDominionEnvFlag(player, dominion, flag, value),
                player, dto -> onSuccess.run(), onError);
    }

    public static void setGuestFlag(Player player, DominionDTO dominion, PriFlag flag, boolean value,
                                    Runnable onSuccess, Consumer<String> onError) {
        handle(DominionAPI.getDominionProvider().setDominionGuestFlag(player, dominion, flag, value),
                player, dto -> onSuccess.run(), onError);
    }

    // ------------------------------------------------------------------
    // 成员
    // ------------------------------------------------------------------

    public static @Nullable PlayerDTO findPlayer(String name) {
        return api().getPlayer(name);
    }

    public static void addMember(Player player, DominionDTO dominion, PlayerDTO target,
                                 Runnable onSuccess, Consumer<String> onError) {
        handle(DominionAPI.getMemberProvider().addMember(player, dominion, target),
                player, dto -> onSuccess.run(), onError);
    }

    public static void removeMember(Player player, DominionDTO dominion, MemberDTO member,
                                    Runnable onSuccess, Consumer<String> onError) {
        handle(DominionAPI.getMemberProvider().removeMember(player, dominion, member),
                player, dto -> onSuccess.run(), onError);
    }

    public static void setMemberFlag(Player player, DominionDTO dominion, MemberDTO member,
                                     PriFlag flag, boolean value,
                                     Runnable onSuccess, Consumer<String> onError) {
        handle(DominionAPI.getMemberProvider().setMemberFlag(player, dominion, member, flag, value),
                player, dto -> onSuccess.run(), onError);
    }

    // ------------------------------------------------------------------
    // 权限组
    // ------------------------------------------------------------------

    public static void createGroup(Player player, DominionDTO dominion, String name,
                                   Runnable onSuccess, Consumer<String> onError) {
        handle(DominionAPI.getGroupProvider().createGroup(player, dominion, name),
                player, dto -> onSuccess.run(), onError);
    }

    public static void deleteGroup(Player player, DominionDTO dominion, GroupDTO group,
                                   Runnable onSuccess, Consumer<String> onError) {
        handle(DominionAPI.getGroupProvider().deleteGroup(player, dominion, group),
                player, dto -> onSuccess.run(), onError);
    }

    public static void renameGroup(Player player, DominionDTO dominion, GroupDTO group, String newName,
                                   Runnable onSuccess, Consumer<String> onError) {
        handle(DominionAPI.getGroupProvider().renameGroup(player, dominion, group, newName),
                player, dto -> onSuccess.run(), onError);
    }

    public static void setGroupFlag(Player player, DominionDTO dominion, GroupDTO group,
                                    PriFlag flag, boolean value,
                                    Runnable onSuccess, Consumer<String> onError) {
        handle(DominionAPI.getGroupProvider().setGroupFlag(player, dominion, group, flag, value),
                player, dto -> onSuccess.run(), onError);
    }

    public static void addMemberToGroup(Player player, DominionDTO dominion, GroupDTO group, MemberDTO member,
                                        Runnable onSuccess, Consumer<String> onError) {
        handle(DominionAPI.getGroupProvider().addMember(player, dominion, group, member),
                player, dto -> onSuccess.run(), onError);
    }

    public static void removeMemberFromGroup(Player player, DominionDTO dominion, GroupDTO group, MemberDTO member,
                                             Runnable onSuccess, Consumer<String> onError) {
        handle(DominionAPI.getGroupProvider().removeMember(player, dominion, group, member),
                player, dto -> onSuccess.run(), onError);
    }

    // ------------------------------------------------------------------
    // 内部工具
    // ------------------------------------------------------------------

    /**
     * 统一处理 Provider 返回的 Future：null 结果或异常均视为失败，
     * 回调切回主线程执行，避免在工作线程上直接操作表单。
     */
    private static <T> void handle(CompletableFuture<T> future, Player player,
                                   Consumer<T> onSuccess, Consumer<String> onError) {
        future.whenComplete((result, throwable) ->
                Sync.run(DominionBedrockUI.getInstance(), () -> {
                    if (throwable != null) {
                        onError.accept(throwable.getMessage() == null
                                ? Lang.ERR_OPERATION_FAILED : throwable.getMessage());
                    } else if (result == null) {
                        onError.accept(Lang.ERR_OPERATION_FAILED);
                    } else {
                        onSuccess.accept(result);
                    }
                }));
    }
}
