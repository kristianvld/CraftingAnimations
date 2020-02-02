package io.github.kristianvld.CraftingAnimations;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class EnchantmentListener implements Listener {

    public EnchantmentListener(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnchantment(EnchantItemEvent event) {
        Vector center = new Vector(0, 0, 0);
        Vector top = new Vector(0, 1.5, 0);

        Location origin = event.getEnchantBlock().getLocation().add(0.5, 0.75, 0.5);

        int topTick = 50;

        List<Animation.KeyFrame> keys = new ArrayList<>();
        keys.add(new Animation.KeyFrame(0, center));
        keys.add(new Animation.KeyFrame(topTick, top));
        keys.add(new Animation.KeyFrame(120, top));

        Item item = Util.spawnItem(origin, event.getItem());

        Animation animation = new Animation(keys, l -> {
            Location pl = l.clone().add(0, 0.25, 0);
            if (item.getTicksLived() < topTick) {
                item.setVelocity(l.toVector().subtract(item.getLocation().toVector()));
                double r = 0.0;
                l.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, pl, 2, r, r, r, 0.1);
            } else if (item.getTicksLived() == topTick) {
                item.setGravity(true);
                item.setVelocity(new Vector(0, 0.2, 0));
                double r = 0.0;
                l.getWorld().spawnParticle(Particle.CRIT_MAGIC, pl, 40, r, r, r, 0.8);
                l.getWorld().playSound(l, Sound.ENTITY_ITEM_PICKUP, 1, 1);
            }
        }, Animation.entRemove(item));

        animation.play(origin);
    }
}
