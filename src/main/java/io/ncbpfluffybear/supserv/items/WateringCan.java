package io.ncbpfluffybear.supserv.items;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nonnull;

import org.bukkit.Effect;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.RayTraceResult;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.items.settings.DoubleRangeSetting;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.common.ChatColors;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.ncbpfluffybear.supserv.SupServPlugin;
import io.ncbpfluffybear.supserv.utils.Constants;
import io.ncbpfluffybear.supserv.utils.Utils;
import me.mrCookieSlime.Slimefun.api.BlockStorage;

public class WateringCan extends SimpleSlimefunItem<ItemUseHandler> implements NotPlaceable {

    private static final int USE_INDEX = 7;
    private static final int MAX_SUGAR_GROW_HEIGHT = 5;

    private final canType canLevel;
    private static final int STONE_SIZE = 64;
    private static final int IRON_SIZE = 100;
    private static final int GOLD_SIZE = 250;
    private static final int DIAMOND_SIZE = 400;
    private static final int EMERALD_SIZE = 650;
    private static final int NETHERITE_SIZE = 1000;

    public final DoubleRangeSetting sugarCaneSuccessChance = new DoubleRangeSetting(this, "sugar-cane-success-chance", 0, 0.3, 1);
    public final DoubleRangeSetting cropSuccessChance = new DoubleRangeSetting(this, "crop-success-chance", 0, 0.3, 1);
    public final DoubleRangeSetting treeSuccessChance = new DoubleRangeSetting(this, "tree-success-chance", 0, 0.3, 1);

    private static final NamespacedKey usageKey = new NamespacedKey(SupServPlugin.getInstance(), "watering_can_usage");

    public WateringCan(ItemGroup category, SlimefunItemStack item, RecipeType recipeType,
                       ItemStack[] recipe, canType size) {
        super(category, item, recipeType, recipe);
        this.canLevel = size;
        addItemSetting(sugarCaneSuccessChance, cropSuccessChance, treeSuccessChance);
    }

    public WateringCan(ItemGroup category, SlimefunItemStack item, RecipeType recipeType,
                       ItemStack[] recipe, canType size, ItemStack recipeOutput) {
        super(category, item, recipeType, recipe, recipeOutput);
        this.canLevel = size;
        addItemSetting(sugarCaneSuccessChance, cropSuccessChance, treeSuccessChance);
    }

    @Nonnull
    @Override
    public ItemUseHandler getItemHandler() {
        return e -> {
            Player p = e.getPlayer();

            e.getInteractEvent().setCancelled(true);

            RayTraceResult rayResult = p.rayTraceBlocks(5d, FluidCollisionMode.SOURCE_ONLY);

            if (rayResult == null) {
                return;
            }

            Block b = rayResult.getHitBlock();
            Location blockLocation = b.getLocation();

            if (!Slimefun.getProtectionManager().hasPermission(e.getPlayer(), blockLocation,
                    Interaction.BREAK_BLOCK)) {
                return;
            }

            ItemStack item = e.getItem();
            BlockData blockData = b.getBlockData();

            // Fill if it hits water
            if (b.getType() == Material.WATER) {
                updateUses(this, p, item, 2);
                return;
            }

            // Sugar Cane
            if (b.getType() == Material.SUGAR_CANE) {

                // Check if can has water
                if (getRemainingUses(item, p, true) < 1) {
                    return;
                }

                int distance = 2;
                Block above = b.getRelative(BlockFace.UP);

                while (above.getType() == Material.SUGAR_CANE) {

                    // Failsafe
                    if (distance >= MAX_SUGAR_GROW_HEIGHT) {
                        //Utils.send(p, "&cThis sugar cane is too tall!");
                        return;
                    }

                    above = b.getRelative(BlockFace.UP, distance);
                    distance++;
                }

                if (above.getType() == Material.AIR) {

                    blockLocation.getWorld().spawnParticle(Particle.WATER_SPLASH, blockLocation, 0);
                    double random = ThreadLocalRandom.current().nextDouble();
                    if (random <= sugarCaneSuccessChance.getValue()) {
                        above.setType(Material.SUGAR_CANE);
                        blockLocation.getWorld().playEffect(blockLocation, Effect.VILLAGER_PLANT_GROW, 0);
                    }

                    updateUses(this, p, item, 1);

                }

                // Crops
            } else if (blockData instanceof Ageable) {

                // Check if can has water
                if (getRemainingUses(item, p, true) < 1) {
                    return;
                }

                Ageable crop = (Ageable) blockData;
                int currentAge = crop.getAge();
                int maxAge = crop.getMaximumAge();

                if (currentAge < maxAge) {
                    blockLocation.getWorld().spawnParticle(Particle.WATER_SPLASH, blockLocation, 0);
                    double random = ThreadLocalRandom.current().nextDouble();
                    if (random <= cropSuccessChance.getValue()) {
                        crop.setAge(currentAge + 1);
                        blockLocation.getWorld().playEffect(blockLocation, Effect.VILLAGER_PLANT_GROW, 0);
                    }

                    updateUses(this, p, item, 1);

                }

                b.setBlockData(blockData);

                // Trees
            } else if (Tag.SAPLINGS.isTagged(b.getType()) ||
                    Material.BROWN_MUSHROOM == b.getType() ||
                    Material.RED_MUSHROOM == b.getType()) {

                // Check if can has water
                if (getRemainingUses(item, p, true) < 1) {
                    return;
                }

                blockLocation.getWorld().spawnParticle(Particle.WATER_SPLASH, blockLocation, 0);
                double random = ThreadLocalRandom.current().nextDouble();
                Material saplingMaterial = b.getType();

                if (BlockStorage.hasBlockInfo(b)) {
                    return; // Prevent use on exotic garden
                } else {

                    if (Constants.SERVER_VERSION < 1163) {
                        if (random <= treeSuccessChance.getValue()) {

                            b.setType(Material.AIR);
                            if (!blockLocation.getWorld().generateTree(blockLocation,
                                    getTreeFromSapling(saplingMaterial))) {
                                b.setType(saplingMaterial);
                            }
                            blockLocation.getWorld().playEffect(blockLocation, Effect.VILLAGER_PLANT_GROW, 0);
                        }
                    } else {
                        b.applyBoneMeal(p.getFacing());
                    }
                }

                updateUses(this, p, item, 1);
            }
        };
    }

    /**
     * Gets remaining uses in a watering can
     */
    private static int getRemainingUses(ItemStack item, Player p, boolean warnRefill) {
        ItemMeta meta = item.getItemMeta();
        int remainingUses = meta.getPersistentDataContainer().getOrDefault(usageKey, PersistentDataType.INTEGER, 0);

        if (warnRefill && remainingUses < 1) {
            Utils.send(p, "&c你需要重新装满你的喷壶!");
        }

        return remainingUses;
    }

    public static boolean updateUses(WateringCan can, Player p, ItemStack item, int updateType) {

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();

        int usesLeft = getRemainingUses(item, p, false);

        if (updateType == 1) {

            p.playSound(p.getLocation(), Sound.ENTITY_DROWNED_AMBIENT_WATER, 0.5F, 1F);
            usesLeft--;

        } else if (updateType == 2) {
            p.playSound(p.getLocation(), Sound.ENTITY_DROWNED_DEATH_WATER, 0.5F, 1F);
            Utils.send(p, "&a你已经装满了你的喷壶");
            usesLeft = can.canLevel.size();

        } else if (updateType == 3) {
            if (usesLeft == 0) {
                Utils.send(p, "&c你需要重新装满你的喷壶!");
                return false;
            }
            usesLeft = 0;
            p.playSound(p.getLocation(), Sound.ITEM_BUCKET_EMPTY, 0.5F, 1F);
        } else {
            p.sendMessage("Error");
        }

        /*
        if (usesLeft == 0) {
            changeSkull(meta, emptyCan);
        }
         */

        lore.set(USE_INDEX, ChatColors.color("&a使用次数剩余: &e" + usesLeft));
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(usageKey, PersistentDataType.INTEGER, usesLeft);
        item.setItemMeta(meta);
        //Utils.send(p, "&eYou have " + usesLeft + " uses left");

        return true;
    }

    private static TreeType getTreeFromSapling(Material m) {
        TreeType treeType = TreeType.TREE;
        String parseSapling = m.toString()
                .replace("_SAPLING", "");

        if (!parseSapling.equals("OAK")) {
            if (parseSapling.equals("JUNGLE")) {
                parseSapling = "SMALL_JUNGLE";
            }
            return TreeType.valueOf(parseSapling);
        }
        return treeType;
    }

    public enum canType {
        STONE(STONE_SIZE),
        IRON(IRON_SIZE),
        GOLD(GOLD_SIZE),
        DIAMOND(DIAMOND_SIZE),
        EMERALD(EMERALD_SIZE),
        NETHERITE(NETHERITE_SIZE);

        private final int size;

        canType(int size) {
            this.size = size;
        }

        private int size() {
            return size;
        }
    }
}