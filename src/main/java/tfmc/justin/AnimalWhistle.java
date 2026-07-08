package tfmc.justin;

import me.Plugins.TLibs.Enums.APIType;
import me.Plugins.TLibs.Objects.API.ItemAPI;
import me.Plugins.TLibs.TLibs;
import org.bukkit.plugin.java.JavaPlugin;
import tfmc.justin.commands.WhistleCommand;
import tfmc.justin.config.AnimalWhistleConfig;
import tfmc.justin.listeners.WhistleInteractListener;

public class AnimalWhistle extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        ItemAPI api = (ItemAPI) TLibs.getApiInstance(APIType.ITEM_API);
        if (api == null) {
            getLogger().severe("TLibs ItemAPI is not available - disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        AnimalWhistleConfig config = new AnimalWhistleConfig(this);
        WhistleInteractListener listener = new WhistleInteractListener(this, api, config);

        getServer().getPluginManager().registerEvents(listener, this);

        WhistleCommand command = new WhistleCommand(config, listener);
        getCommand("animalwhistle").setExecutor(command);
        getCommand("animalwhistle").setTabCompleter(command);

        getLogger().info("AnimalWhistle plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("AnimalWhistle plugin has been disabled!");
    }
}
