package tfmc.justin.listeners;

import me.Plugins.TLibs.Objects.API.ItemAPI;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.event.block.Action;
import tfmc.justin.config.AnimalWhistleConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// ==============================================
// Handles animal whistle interaction
// ==============================================
public class WhistleInteractListener implements Listener {

    private final JavaPlugin plugin;
    private final ItemAPI api;
    private final AnimalWhistleConfig config;

    // Resolved once and reused, instead of rebuilding the template on every click
    private ItemStack cachedWhistleTemplate;

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public WhistleInteractListener(JavaPlugin plugin, ItemAPI api, AnimalWhistleConfig config) {
        this.plugin = plugin;
        this.api = api;
        this.config = config;
    }

    // Invalidate the cached template (e.g. after a config reload)
    public void clearItemCache() {
        cachedWhistleTemplate = null;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only right-click
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // The event fires once per hand - only handle the main hand
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Dont do anything if item is null
        if (item == null) {
            return;
        }

        // Check if the item is an animal whistle using TLibs
        if (!isAnimalWhistle(item)) {
            return;
        }

        // Don't also open chests/doors or use the item
        event.setCancelled(true);

        // Enforce per-player cooldown
        long remainingMillis = getRemainingCooldown(player);
        if (remainingMillis > 0) {
            long remainingSeconds = (remainingMillis + 999) / 1000;
            sendMessage(player, config.getCooldownMessage().replace("%seconds%", String.valueOf(remainingSeconds)));
            return;
        }
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());

        // Play whistle sound for all nearby players
        playWhistleSound(player);

        // Get all nearby entities
        List<Entity> nearbyEntities = player.getNearbyEntities(
            config.getDetectionRadius(),
            config.getDetectionRadius(),
            config.getDetectionRadius()
        );

        // Apply glowing effect to all nearby whitelisted animals
        int highlighted = 0;
        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof LivingEntity)) {
                continue;
            }

            if (config.getWhitelistedAnimals().contains(entity.getType())) {
                LivingEntity animal = (LivingEntity) entity;

                // Apply glowing effect for the configured duration (converted to ticks)
                int durationTicks = config.getGlowDuration() * 20;
                PotionEffect glowEffect = new PotionEffect(
                    PotionEffectType.GLOWING,
                    durationTicks,
                    0,
                    false,
                    false
                );

                animal.addPotionEffect(glowEffect);
                highlighted++;
            }
        }

        // Tell the player what the whistle found
        if (highlighted > 0) {
            sendMessage(player, config.getHighlightedMessage().replace("%count%", String.valueOf(highlighted)));
        } else {
            sendMessage(player, config.getNoAnimalsMessage());
        }
    }

    // ==============================================
    // Check if an item is an animal whistle using TLibs
    // ==============================================
    private boolean isAnimalWhistle(ItemStack item) {
        try {
            if (cachedWhistleTemplate == null) {
                cachedWhistleTemplate = api.getCreator().getItemFromPath(config.getAnimalWhistlePath());
            }
            if (cachedWhistleTemplate == null) {
                return false;
            }
            return item.isSimilar(cachedWhistleTemplate);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to validate animal whistle: " + e.getMessage());
            return false;
        }
    }

    // ==============================================
    // Cooldown helpers
    // ==============================================
    private long getRemainingCooldown(Player player) {
        Long lastUse = cooldowns.get(player.getUniqueId());
        if (lastUse == null) {
            return 0;
        }
        long cooldownMillis = config.getCooldownSeconds() * 1000L;
        return lastUse + cooldownMillis - System.currentTimeMillis();
    }

    // ==============================================
    // Play whistle sound at player location
    // ==============================================
    private void playWhistleSound(Player player) {
        Sound sound = config.getWhistleSound();
        if (sound == null) {
            return;
        }
        player.getWorld().playSound(
            player.getLocation(),
            sound,
            SoundCategory.AMBIENT,
            config.getSoundVolume(),
            config.getSoundPitch()
        );
    }

    private void sendMessage(Player player, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}
