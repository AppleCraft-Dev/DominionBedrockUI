package cn.lunadeer.dominion.bedrockui.form;

import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.Form;
import org.geysermc.cumulus.form.ModalForm;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.List;

/**
 * floodgate 表单发送封装。
 *
 * <p>提供三类表单的简式构建：按钮菜单（SimpleForm）、
 * 确认框（ModalForm）、以及直接发送自定义表单（CustomForm）。
 * 所有点击回调均已处理好响应解析，菜单代码只需关心业务逻辑。</p>
 *
 * <p>注意：本类引用了 floodgate/cumulus 类型，只应在
 * floodgate 可用（{@code PlatformService.isFloodgateAvailable()}）时被调用。</p>
 */
public final class Forms {

    private Forms() {
    }

    /**
     * 一个按钮菜单项。
     *
     * @param text   按钮文本（支持 § 颜色代码）
     * @param action 点击后执行的动作
     */
    public record MenuButton(String text, Runnable action) {
        public static MenuButton of(String text, Runnable action) {
            return new MenuButton(text, action);
        }
    }

    /** 直接发送任意表单。 */
    public static void send(Player player, Form form) {
        FloodgateApi.getInstance().sendForm(player.getUniqueId(), form);
    }

    /**
     * 发送按钮菜单（SimpleForm）。
     *
     * @param player   基岩玩家
     * @param title    标题
     * @param content  正文（可为空串）
     * @param buttons  按钮列表
     * @param onClosed 玩家直接关闭表单时的回调（可为 null）
     */
    public static void menu(Player player, String title, String content,
                            List<MenuButton> buttons, Runnable onClosed) {
        SimpleForm.Builder builder = SimpleForm.builder()
                .title(title)
                .content(content == null ? "" : content);
        for (MenuButton button : buttons) {
            builder.button(button.text());
        }
        builder.validResultHandler(response -> {
            int id = response.clickedButtonId();
            if (id >= 0 && id < buttons.size()) {
                Runnable action = buttons.get(id).action();
                if (action != null) {
                    action.run();
                }
            }
        });
        if (onClosed != null) {
            builder.closedOrInvalidResultHandler(onClosed);
        }
        send(player, builder.build());
    }

    public static void menu(Player player, String title, String content, List<MenuButton> buttons) {
        menu(player, title, content, buttons, null);
    }

    /**
     * 发送确认框（ModalForm）。
     *
     * @param onConfirm 点击按钮 1（确认）时执行
     * @param onCancel  点击按钮 2（取消）或关闭时执行（可为 null）
     */
    public static void confirm(Player player, String title, String content,
                               String confirmText, String cancelText,
                               Runnable onConfirm, Runnable onCancel) {
        ModalForm.Builder builder = ModalForm.builder()
                .title(title)
                .content(content)
                .button1(confirmText)
                .button2(cancelText)
                .validResultHandler(response -> {
                    if (response.clickedButtonId() == 0) {
                        onConfirm.run();
                    } else if (onCancel != null) {
                        onCancel.run();
                    }
                });
        if (onCancel != null) {
            builder.closedOrInvalidResultHandler(onCancel);
        }
        send(player, builder.build());
    }

    /**
     * 构建一个自定义表单（CustomForm），用于输入/开关/下拉场景。
     * 业务侧自行链式添加组件并设置 {@code validResultHandler} 后调用 build 发送。
     *
     * <p>响应取值注意（反编译 Cumulus 1.1.2 确认的语义）：带下标的
     * {@code asInput(i)}/{@code asToggle(i)}/{@code asDropdown(i)} 永远按
     * 「包含 label 在内的原始组件下标」取值，{@code includeLabels(boolean)} 对它们无效。
     * 正确做法：回调开头 {@code response.includeLabels(false)}，然后用<b>无参游标版</b>
     * {@code asInput()}/{@code asToggle()}/{@code asDropdown()} 按组件顺序依次取值，
     * 游标会自动跳过 label——该语义在各 floodgate 版本间一致。</p>
     */
    public static CustomForm.Builder custom(String title) {
        return CustomForm.builder().title(title);
    }
}
