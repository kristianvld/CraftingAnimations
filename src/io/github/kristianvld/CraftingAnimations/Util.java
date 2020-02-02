package io.github.kristianvld.CraftingAnimations;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.UUID;

public class Util {

    public static Item spawnItem(Location loc, ItemStack item) {
        item = item.clone();
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(UUID.randomUUID().toString()); // prevents merging with other items
        item.setItemMeta(im);
        Item model = loc.getWorld().dropItem(loc, item.clone());
        model.setGravity(false);
        model.setVelocity(new Vector(0, 0, 0));
        model.setPickupDelay(Integer.MAX_VALUE);
        model.addScoreboardTag(CleanupManager.REMOVE_TAG);

        return model;
    }

    public static boolean isValidItem(ItemStack item) {
        return item != null && item.getAmount() > 0 && item.getType() != Material.AIR;
    }

}
