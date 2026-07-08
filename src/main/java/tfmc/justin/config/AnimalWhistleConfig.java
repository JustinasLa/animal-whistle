package tfmc.justin.config;

import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumSet;
import java.util.Set;

// ==============================================
// Configuration manager for AnimalWhistle
// ==============================================
public class AnimalWhistleConfig {

    private final JavaPlugin plugin;

    private String animalWhistlePath;

    private double detectionRadius;
    private int glowDuration;
    private int cooldownSeconds;
    private Set<EntityType> whitelistedAnimals;

    private Sound whistleSound;
    private float soundVolume;
    private float soundPitch;

    private String highlightedMessage;
    private String noAnimalsMessage;
    private String cooldownMessage;

    public AnimalWhistleConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    // Re-reads config.yml from disk and reparses all values
    public void reload() {
        plugin.reloadConfig();
        load();
    }

    private void load() {
        FileConfiguration config = plugin.getConfig();

        // Load item paths
        this.animalWhistlePath = config.getString("items.animal-whistle", "m.pets.animal_whistle");

        // Load settings
        this.detectionRadius = config.getDouble("settings.detection-radius", 64.0);
        this.glowDuration = config.getInt("settings.glow-duration", 5);
        this.cooldownSeconds = config.getInt("settings.cooldown", 3);

        // Parse the whitelist into entity types once, so the listener
        // doesn't string-match on every click
        this.whitelistedAnimals = EnumSet.noneOf(EntityType.class);
        for (String name : config.getStringList("settings.whitelisted-animals")) {
            try {
                whitelistedAnimals.add(EntityType.valueOf(name.toUpperCase()));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Unknown entity type in whitelist: " + name);
            }
        }
        if (whitelistedAnimals.isEmpty()) {
            plugin.getLogger().warning("Animal whitelist is empty - defaulting to HORSE");
            whitelistedAnimals.add(EntityType.HORSE);
        }

        // Load sound settings
        String soundName = config.getString("sound.type", "ITEM_GOAT_HORN_SOUND_6");
        try {
            this.whistleSound = Sound.valueOf(soundName);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound type in config: " + soundName);
            this.whistleSound = null;
        }
        this.soundVolume = (float) config.getDouble("sound.volume", 4.0);
        this.soundPitch = (float) config.getDouble("sound.pitch", 2.0);

        // Load messages
        this.highlightedMessage = config.getString("messages.highlighted", "&aHighlighted &6%count% &aanimals nearby.");
        this.noAnimalsMessage = config.getString("messages.no-animals", "&7No animals found nearby.");
        this.cooldownMessage = config.getString("messages.cooldown", "&7The whistle is on cooldown for another &6%seconds%s&7.");
    }

    public String getAnimalWhistlePath() {
        return animalWhistlePath;
    }

    public double getDetectionRadius() {
        return detectionRadius;
    }

    public int getGlowDuration() {
        return glowDuration;
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    public Set<EntityType> getWhitelistedAnimals() {
        return whitelistedAnimals;
    }

    public Sound getWhistleSound() {
        return whistleSound;
    }

    public float getSoundVolume() {
        return soundVolume;
    }

    public float getSoundPitch() {
        return soundPitch;
    }

    public String getHighlightedMessage() {
        return highlightedMessage;
    }

    public String getNoAnimalsMessage() {
        return noAnimalsMessage;
    }

    public String getCooldownMessage() {
        return cooldownMessage;
    }
}
