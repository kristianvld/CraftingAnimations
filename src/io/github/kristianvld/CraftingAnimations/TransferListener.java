package io.github.kristianvld.CraftingAnimations;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class TransferListener implements Listener {

    public TransferListener(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(InventoryClickEvent event) {
        onInventoryChange(event.getWhoClicked(), event.getView());
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        onInventoryChange(event.getWhoClicked(), event.getView());
    }

    private void onInventoryChange(HumanEntity whoClicked, InventoryView view) {
        HumanEntity who = whoClicked;
        if (who.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        Location from = whoClicked.getLocation();
        Location to = view.getTopInventory().getLocation();

        if (from == null || to == null) {
            return;
        }
        if (!from.getWorld().getUID().equals(to.getWorld().getUID())) {
            return;
        }
        if (from.clone().subtract(to).lengthSquared() > 8 * 8) {
            return;
        }

        monitorInventory(view.getTopInventory(), from, to);
    }

    /**
     * Monitor the given inventory for 1 tick and animate in/out any items that change
     */
    public void monitorInventory(Inventory target, Location from, Location to) {
        List<ItemStack> target1 = new ArrayList<>(target.getContents().length);
        for (int i = 0; i < target.getContents().length; i++) {
            ItemStack item = target.getContents()[i];
            if (item == null) {
                target1.add(null);
            } else {
                target1.add((item.clone()));
            }
        }
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            List<ItemStack> in = new ArrayList<>();
            List<ItemStack> out = new ArrayList<>();

            for (int i = 0; i < target1.size(); i++) {
                ItemStack cur = target.getContents()[i];
                ItemStack prev = target1.get(i);
                if (cur == null && prev != null) {
                    out.add(prev.clone());
                } else if (cur != null && prev == null) {
                    in.add(cur.clone());
                } else if (cur != null && prev != null) {
                    ItemStack singleCur = cur.clone();
                    singleCur.setAmount(1);
                    ItemStack singlePrev = prev.clone();
                    singlePrev.setAmount(1);

                    if (singleCur.equals(singlePrev)) {
                        int diff = cur.getAmount() - prev.getAmount();
                        singleCur.setAmount(Math.abs(diff));
                        if (diff > 0) {
                            in.add(singleCur);
                        } else {
                            out.add(singleCur);
                        }
                    }
                }
            }

            int delay = 0;
            for (ItemStack item : in) {
                if (item == null || item.getType() == Material.AIR || item.getAmount() == 0) {
                    continue;
                }
                animate(from, to, item, target.getType(), false, delay);
                delay++;
            }
            for (ItemStack item : out) {
                if (item == null || item.getType() == Material.AIR || item.getAmount() == 0) {
                    continue;
                }
                animate(from, to, item, target.getType(), true, delay);
                delay++;
            }

        }, 0);
    }

    private void animate(Location from, Location to, ItemStack item, InventoryType type, boolean reverse, int delay) {
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            switch (type) {
                case CHEST:
                case ENDER_CHEST:
                case DISPENSER:
                case DROPPER:
                case FURNACE:
                case BEACON:
                case HOPPER:
                case SHULKER_BOX:
                case BARREL:
                case BLAST_FURNACE:
                case SMOKER:
                case LOOM:
                    animateContainer(from, to, item, reverse);
            }
        }, delay);
    }

    private BlockFace getDefaultDirection(Block block) {
        switch (block.getType()) {
            case CHEST:
            case ENDER_CHEST:
            case TRAPPED_CHEST:
            case SHULKER_BOX:
            case HOPPER:
            case BEACON:
            case LOOM:
                return BlockFace.UP;
        }
        if (block.getBlockData() instanceof Directional) {
            return ((Directional) block.getBlockData()).getFacing();
        }
        return BlockFace.UP;
    }

    public void animateContainer(Location from, Location to, ItemStack item, boolean reverse) {
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        BlockFace entryDirection = getDefaultDirection(to.getBlock());

        Location start = from.clone().add(0, 1.2, 0).subtract(from);
        Location aboveEnd = to.clone().add(0.5, 0.5, 0.5).add(entryDirection.getDirection().multiply(0.8)).subtract(from);
        Location end = to.clone().add(0.5, 0.5, 0.5).subtract(from);

        List<Animation.KeyFrame> keys = new ArrayList<>();
        keys.add(new Animation.KeyFrame(0, start));
        keys.add(new Animation.KeyFrame(8, aboveEnd));
        keys.add(new Animation.KeyFrame(10, end));

        Location spawn = reverse ? end : start;
        spawn = spawn.clone().add(from);

        Item model = Util.spawnItem(spawn, item);

        Animation animation = new Animation(keys, Animation.entTeleport(model), Animation.entRemove(model));

        if (reverse) {
            animation = animation.reverse();
        }

        animation.play(from);
    }

}
