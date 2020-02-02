package io.github.kristianvld.CraftingAnimations;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class CleanupManager implements Listener {

    // A scoreboard tag used to remove entities, e.g. animation entities that were not deleted as the server restarted/
    //  stopped before the animation ended. Entities with this tag are removed on chunk load and server reload/plugin
    //  startup. The plugin will also try to delete these items on disable, but might not run in case of a server crash.
    // Using scoreboard tag that is impossible to create in-game along with a random UUID to prevent potential conflicts
    public static String REMOVE_TAG = "§\00Remove remove on load 3f683231-70fc-4472-a81c-f5147e832423";

    public CleanupManager(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        removeAll();
    }

    public void removeAll() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity ent : world.getEntities()) {
                if (ent.getScoreboardTags().contains(REMOVE_TAG)) {
                    ent.remove();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getItem().getScoreboardTags().contains(REMOVE_TAG)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickup(InventoryPickupItemEvent event) {
        if (event.getItem().getScoreboardTags().contains(REMOVE_TAG)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkLoad(ChunkLoadEvent event) {
        for (Entity e : event.getChunk().getEntities()) {
            if (e.getScoreboardTags().contains(REMOVE_TAG)) {
                e.remove();
            }
        }
    }
}
