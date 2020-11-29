package io.github.kristianvld.CraftingAnimations;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class CraftingListener implements Listener {

    public CraftingListener(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory().getType() == InventoryType.WORKBENCH) {
            if (event.getSlotType() == InventoryType.SlotType.RESULT) {
                CraftingInventory inv = (CraftingInventory) event.getClickedInventory();
                ItemStack result = event.getCurrentItem();
                if (result != null && result.getType() != Material.AIR && result.getAmount() > 0) {
                    animateCrafting(inv.getLocation().getBlock(), inv.getMatrix(), event.getCurrentItem());
                }
            }
        }
    }

    public void animateCrafting(Block table, ItemStack[] matrix, ItemStack result) {
        Location origin = table.getLocation().add(0.5, 1, 0.5);

        for (int i = 0; i < matrix.length; i++) {
            matrix[i] = matrix[i] == null ? null : matrix[i].clone();
        }
        ItemStack resultItem = result.clone();

        Runnable startResult = () -> {
            Item item = Util.spawnItem(origin, resultItem);
            item.setVelocity(new Vector(0, 0.25, 0));
            item.setGravity(true);
            float r = 0.15f;
            origin.getWorld().spawnParticle(Particle.END_ROD, origin.clone().add(0, 2 * r, 0), 10, r, r, r, 0.01);
            origin.getWorld().playSound(origin, Sound.ENTITY_ITEM_PICKUP, 0.5f, 0.5f);
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                item.remove();
            }, 60);
        };

        boolean[] first = new boolean[]{true};

        for (int slot = 0; slot < matrix.length; slot++) {
            ItemStack item = matrix[slot];
            if (!Util.isValidItem(item)) {
                continue;
            }
            item = item.clone();
            item.setAmount(1);
            double x = (slot / 3) - 1;
            double z = (slot % 3) - 1;
            Vector start = new Vector(x, 0, z).multiply(0.4);
            Vector end = new Vector(x, 0, z).multiply(0.1);

            List<Animation.KeyFrame> keys = new ArrayList<>();
            keys.add(new Animation.KeyFrame(0, start));
            keys.add(new Animation.KeyFrame(10, end));

            Item ent = Util.spawnItem(origin.clone().add(start), item);

            Animation.PlayableEffect teleport = Animation.entTeleport(ent);

            Animation animation = new Animation(keys, teleport, loc -> {
                ent.remove();

                if (first[0]) {
                    startResult.run();
                    first[0] = false;
                }
            });
            animation.play(origin);
        }
    }
}
