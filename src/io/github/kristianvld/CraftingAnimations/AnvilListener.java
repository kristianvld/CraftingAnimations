package io.github.kristianvld.CraftingAnimations;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class AnvilListener implements Listener {

    public AnvilListener(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAnvil(InventoryClickEvent event) {
        if (event.getClickedInventory() instanceof AnvilInventory) {
            AnvilInventory anvil = (AnvilInventory) event.getClickedInventory();

            ItemStack a = anvil.getItem(0);
            ItemStack b = anvil.getItem(1);
            if (b == null) {
                b = new ItemStack(Material.NAME_TAG);
            }
            ItemStack result = anvil.getItem(2);

            if (event.getSlot() == 2) {
                if (result != null && result.getType() != Material.AIR && result.getAmount() > 0) {
                    animateAnvil(anvil.getLocation(), a.clone(), b.clone(), result.clone());
                }
            }
        }
    }

    public void animateAnvil(Location loc, ItemStack a, ItemStack b, ItemStack result) {
        if (loc.getBlock().getBlockData() instanceof Directional) {
            BlockFace face = ((Directional) loc.getBlock().getBlockData()).getFacing();

            Location origin = loc.clone().add(0.5, 1, 0.5);

            Vector center = new Vector(0, 0, 0);
            Vector vecA = face.getDirection().multiply(-0.5);
            Vector vecB = face.getDirection().multiply(0.5);

            List<Animation.KeyFrame> keys = new ArrayList<>();
            keys.add(new Animation.KeyFrame(0, center));
            keys.add(new Animation.KeyFrame(45, center));

            Runnable runLast = () -> {
                Item item = Util.spawnItem(origin, result);

                Animation animation = new Animation(keys, Animation.entTeleport(item), Animation.entRemove(item));
                animation.play(origin);
            };


            List<Animation.KeyFrame> keysA = new ArrayList<>();
            keysA.add(new Animation.KeyFrame(0, vecA));
            keysA.add(new Animation.KeyFrame(15, center));

            Item itemA = Util.spawnItem(origin.clone().add(vecA), a);
            Animation animationA = new Animation(keysA, Animation.entTeleport(itemA), Animation.entRemove(itemA));

            List<Animation.KeyFrame> keysB = new ArrayList<>();
            keysB.add(new Animation.KeyFrame(0, vecB));
            keysB.add(new Animation.KeyFrame(15, center));

            Item itemB = Util.spawnItem(origin.clone().add(vecB), b);
            Animation animationB = new Animation(keysB, Animation.entTeleport(itemB), l -> {
                itemB.remove();
                runLast.run();
                l.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, l, 20, 0, 0, 0, 0.05);
            });

            animationA.play(origin);
            animationB.play(origin);
        }
    }

}
