package cn.lunadeer.dominion.bedrockui.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 扩展自身配置的只读封装。
 */
public final class PluginConfig {

    private final boolean interceptDominionCommand;
    private final List<String> interceptCommands;
    private final Pattern dominionNamePattern;

    public PluginConfig(Plugin plugin) {
        FileConfiguration config = plugin.getConfig();
        this.interceptDominionCommand = config.getBoolean("intercept-dominion-command", true);
        this.interceptCommands = config.getStringList("intercept-commands").stream()
                .map(s -> s.toLowerCase(Locale.ROOT))
                .filter(s -> !s.isBlank())
                .toList();

        Pattern pattern;
        try {
            pattern = Pattern.compile(config.getString("create.name-regex", "^[\\w一-龥-]{1,32}$"));
        } catch (PatternSyntaxException exception) {
            plugin.getLogger().warning("create.name-regex 配置无效，已回退到默认值: " + exception.getMessage());
            pattern = Pattern.compile("^[\\w一-龥-]{1,32}$");
        }
        this.dominionNamePattern = pattern;
    }

    public boolean shouldIntercept() {
        return interceptDominionCommand;
    }

    public List<String> interceptCommands() {
        return interceptCommands;
    }

    public Pattern dominionNamePattern() {
        return dominionNamePattern;
    }
}
