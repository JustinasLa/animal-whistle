package tfmc.justin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import tfmc.justin.config.AnimalWhistleConfig;
import tfmc.justin.listeners.WhistleInteractListener;

import java.util.Collections;
import java.util.List;

// ==============================================
// /animalwhistle admin command
// ==============================================
public class WhistleCommand implements CommandExecutor, TabCompleter {

    private final AnimalWhistleConfig config;
    private final WhistleInteractListener listener;

    public WhistleCommand(AnimalWhistleConfig config, WhistleInteractListener listener) {
        this.config = config;
        this.listener = listener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            config.reload();
            listener.clearItemCache();
            sender.sendMessage(ChatColor.GREEN + "AnimalWhistle config reloaded.");
            return true;
        }
        sender.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " reload");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && "reload".startsWith(args[0].toLowerCase())) {
            return Collections.singletonList("reload");
        }
        return Collections.emptyList();
    }
}
