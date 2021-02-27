package io.ncbpfluffybear.supserv;

import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.ncbpfluffybear.supserv.items.LavaSponge;
import io.ncbpfluffybear.supserv.objects.NonInteractableItem;
import io.ncbpfluffybear.supserv.utils.Utils;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.api.SlimefunItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class SupServItemSetup {

    private SupServItemSetup() {}

    public static void setup(@Nonnull SupServPlugin plugin) {

        new LavaSponge(SupServItems.SUPSERV_CATEGORY, SupServItems.LAVA_SPONGE, RecipeType.ENHANCED_CRAFTING_TABLE,
            new ItemStack[] {
                null, new ItemStack(Material.BROWN_WOOL), null,
                new ItemStack(Material.BROWN_WOOL), new ItemStack(Material.OBSIDIAN),
                new ItemStack(Material.BROWN_WOOL),
                null, new ItemStack(Material.BROWN_WOOL), null
            }, new SlimefunItemStack(SupServItems.LAVA_SPONGE, 8)
        ).register(plugin);

        new NonInteractableItem(SupServItems.SUPSERV_CATEGORY, SupServItems.CHIPPED_WITHER_SKELETON_SKULL,
            RecipeType.MAGIC_WORKBENCH,
            new ItemStack[] {
                SlimefunItems.COMPRESSED_CARBON, SlimefunItems.COMPRESSED_CARBON, SlimefunItems.COMPRESSED_CARBON,
                SlimefunItems.COMPRESSED_CARBON, new ItemStack(Material.SKELETON_SKULL),
                SlimefunItems.COMPRESSED_CARBON,
                SlimefunItems.COMPRESSED_CARBON, SlimefunItems.COMPRESSED_CARBON, SlimefunItems.COMPRESSED_CARBON
            }).register(plugin);

        new NonInteractableItem(SupServItems.SUPSERV_CATEGORY, SupServItems.FRAGMENTED_WITHER_SKELETON_SKULL,
            RecipeType.MAGIC_WORKBENCH,
            Utils.build3x3Recipe(SupServItems.CHIPPED_WITHER_SKELETON_SKULL)
        ).register(plugin);

        new NonInteractableItem(SupServItems.SUPSERV_CATEGORY, SupServItems.WITHER_SKELETON_SKULL,
            RecipeType.MAGIC_WORKBENCH,
            new ItemStack[] {
                SupServItems.FRAGMENTED_WITHER_SKELETON_SKULL,
                SupServItems.FRAGMENTED_WITHER_SKELETON_SKULL, null,
                SupServItems.FRAGMENTED_WITHER_SKELETON_SKULL, SupServItems.FRAGMENTED_WITHER_SKELETON_SKULL, null,
                null, null, null
            }, new ItemStack(Material.WITHER_SKELETON_SKULL)
        ).register(plugin);

    }

}
