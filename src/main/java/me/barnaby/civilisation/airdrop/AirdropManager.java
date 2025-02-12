package me.barnaby.civilisation.airdrop;

import me.barnaby.civilisation.Civilisation;
import me.barnaby.civilisation.config.ConfigManager;
import me.barnaby.civilisation.util.ChatUtils;
import me.barnaby.civilisation.util.StaffUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Manages airdrop events, including spawning and loot distribution.
 */
public class AirdropManager {
    private final Civilisation plugin;
    private final ConfigManager configManager;
    private final Random random = new Random();

    public AirdropManager(Civilisation plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        startAirdropTimer();
    }

    /**
     * Starts an automatic airdrop timer based on configured intervals.
     */
    private void startAirdropTimer() {
        int interval = configManager.getConfig().getInt("airdrop.interval", 7200); // Default 2 hours (in seconds)
        new BukkitRunnable() {
            @Override
            public void run() {
                spawnAirdrop();
            }
        }.runTaskTimer(plugin, interval * 20L, interval * 20L); // Convert seconds to ticks
    }

    /**
     * Spawns a random airdrop based on configured types.
     */
    public void spawnAirdrop() {
        String airdropType = getRandomAirdropType();
        if (airdropType == null) {
            plugin.getLogger().severe("No valid airdrop types found!");
            return;
        }
        spawnAirdrop(airdropType);
    }

    /**
     * Spawns an airdrop of a specified type.
     */
    public void spawnAirdrop(String type) {
        ConfigurationSection typeSection = configManager.getConfig().getConfigurationSection("airdrop.types." + type);
        if (typeSection == null) {
            plugin.getLogger().warning("Airdrop type '" + type + "' does not exist!");
            return;
        }

        Location dropLocation = getRandomLocation();
        if (dropLocation == null) {
            plugin.getLogger().severe("Failed to find a valid airdrop location!");
            return;
        }

        // Place the chest on top of the highest block by using the dropLocation (which is already one block above)
        Block block = dropLocation.getBlock();
        block.setType(Material.CHEST);

        Chest chest = (Chest) block.getState();
        fillChestWithLoot(chest, type);

        // Firework Effect at the airdrop location
        launchFirework(dropLocation);

        // Announce to public
        Bukkit.broadcastMessage(configManager.getMessage("airdrop.announcement", type.toUpperCase(), dropLocation.getBlockX(), dropLocation.getBlockZ()));

        // Notify staff with a clickable teleport message
        StaffUtil.getOnlineStaff().forEach(staff -> ChatUtils.sendClickableMessage(
                staff,
                configManager.getMessage("airdrop.teleport_click"),
                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/teleport " + dropLocation.getBlockX() + " " + dropLocation.getBlockY() + " " + dropLocation.getBlockZ()),
                new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(configManager.getMessage("airdrop.teleport_hover")))
        ));
    }

    /**
     * Retrieves a random airdrop type based on weighted chance.
     */
    private String getRandomAirdropType() {
        ConfigurationSection typesSection = configManager.getConfig().getConfigurationSection("airdrop.types");
        if (typesSection == null) return null;

        List<String> airdropTypes = new ArrayList<>();
        for (String key : typesSection.getKeys(false)) {
            int chance = typesSection.getInt(key + ".chance", 100);
            for (int i = 0; i < chance; i++) {
                airdropTypes.add(key);
            }
        }

        return airdropTypes.isEmpty() ? null : airdropTypes.get(random.nextInt(airdropTypes.size()));
    }

    /**
     * Retrieves a random valid location for the airdrop.
     * <p>
     * This method attempts to find a location within the defined region
     * where the airdrop can safely land. It checks that the block at the
     * highest point is both solid and not liquid (e.g., water or lava).
     * If a valid location is found, it returns the location one block above,
     * where the chest will be placed.
     * </p>
     */
    private Location getRandomLocation() {
        ConfigurationSection section = configManager.getConfig().getConfigurationSection("airdrop.region");
        if (section == null) return null;

        World world = Bukkit.getWorld(section.getString("world", "world"));
        if (world == null) return null;

        int x1 = section.getInt("x1");
        int z1 = section.getInt("z1");
        int x2 = section.getInt("x2");
        int z2 = section.getInt("z2");

        int attempts = 0;
        while (attempts < 10) {
            int x = random.nextInt(Math.abs(x2 - x1) + 1) + Math.min(x1, x2);
            int z = random.nextInt(Math.abs(z2 - z1) + 1) + Math.min(z1, z2);
            Block highestBlock = world.getHighestBlockAt(x, z);

            // Ensure the block is solid and not liquid (e.g., water or lava)
            if (!highestBlock.isLiquid() && highestBlock.getType().isSolid()) {
                // Return the location one block above the highest block
                return highestBlock.getLocation().add(0, 1, 0);
            }
            attempts++;
        }
        return null;
    }

    /**
     * Fills the airdrop chest with loot based on its type.
     */
    private void fillChestWithLoot(Chest chest, String airdropType) {
        ConfigurationSection typeSection = configManager.getConfig().getConfigurationSection("airdrop.types." + airdropType);
        if (typeSection == null) {
            plugin.getLogger().warning("Airdrop type '" + airdropType + "' does not exist in config!");
            return;
        }

        ConfigurationSection itemsSection = typeSection.getConfigurationSection("items");
        if (itemsSection == null) return;

        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
            if (itemSection == null) continue;

            int chance = itemSection.getInt("chance-of-inclusion", 100);
            if (random.nextInt(100) >= chance) continue;

            Material material = Material.matchMaterial(key);
            if (material == null) continue;

            int minAmount = itemSection.getInt("amount-min", 1);
            int maxAmount = itemSection.getInt("amount-max", 1);
            int amount = random.nextInt((maxAmount - minAmount) + 1) + minAmount;

            ItemStack item = new ItemStack(material, amount);
            ItemMeta meta = item.getItemMeta();

            // Apply custom name
            if (itemSection.contains("name") && meta != null) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', itemSection.getString("name")));
            }

            // Apply enchantments
            if (itemSection.contains("enchants") && meta != null) {
                for (String enchantEntry : itemSection.getStringList("enchants")) {
                    String[] parts = enchantEntry.split(":");
                    if (parts.length == 2) {
                        Enchantment enchantment = Enchantment.getByName(parts[0].toUpperCase());
                        int level = Integer.parseInt(parts[1].trim());

                        if (enchantment != null) {
                            meta.addEnchant(enchantment, level, true);
                        }
                    }
                }
            }

            if (meta != null) {
                item.setItemMeta(meta);
            }

            chest.getInventory().addItem(item);
        }
    }

    /**
     * Launches a firework at the airdrop location.
     */
    private void launchFirework(Location loc) {
        Firework firework = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffect(
                FireworkEffect.builder()
                        .with(FireworkEffect.Type.BALL)
                        .withColor(Color.RED, Color.YELLOW)
                        .withFlicker()
                        .build()
        );
        meta.setPower(1);
        firework.setFireworkMeta(meta);
    }
}
