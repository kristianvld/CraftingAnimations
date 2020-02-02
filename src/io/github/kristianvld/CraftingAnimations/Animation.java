package io.github.kristianvld.CraftingAnimations;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Bit buggy with rotation of locations stuff, doesn't smooth auto fill rotations

public class Animation {

    public static interface PlayableEffect {
        public void play(Location loc);
    }

    public static class TeleportEffect implements PlayableEffect {

        private final Entity ent;

        public TeleportEffect(Entity ent) {
            this.ent = ent;
        }

        @Override
        public void play(Location loc) {
            ent.setVelocity(loc.toVector().subtract(ent.getLocation().toVector()));
        }
    }

    public static class RemoveEffect implements PlayableEffect {

        private final Entity ent;

        public RemoveEffect(Entity ent) {
            this.ent = ent;
        }

        @Override
        public void play(Location loc) {
            ent.remove();
        }
    }

    public static class KeyFrame {

        private final int tick;
        private final Object key;

        @Contract(pure = true)
        private KeyFrame(int tick, Object key) {
            assert tick >= 0;
            this.tick = tick;
            this.key = key;
        }

        @Contract(pure = true)
        public KeyFrame(int tick, Location loc) {
            this(tick, (Object) loc.clone());
        }

        @Contract(pure = true)
        public KeyFrame(int tick, Vector vector) {
            this(tick, (Object) vector.clone());
        }

        @Contract(pure = true)
        public KeyFrame(int tick, PlayableEffect effect) {
            this(tick, (Object) effect);
        }

        @Override
        public String toString() {
            return "KeyFrame{" +
                    "tick=" + tick +
                    ", key=" + key +
                    '}';
        }
    }

    private int tick;
    private int maxTick;
    private List<KeyFrame> locs = new ArrayList<>();
    private Map<Integer, List<PlayableEffect>> keys = new HashMap<>();

    private PlayableEffect locationMonitor;
    private PlayableEffect finishTask;

    private BukkitTask nextTask;

    private Animation(int maxTick, List<KeyFrame> locs, Map<Integer, List<PlayableEffect>> keys) {
        this(maxTick, locs, keys, null, null);
    }

    private Animation(int maxTick, List<KeyFrame> locs, Map<Integer, List<PlayableEffect>> keys, PlayableEffect locationMonitor, PlayableEffect finishTask) {
        this.maxTick = maxTick;
        this.locs = locs;
        this.keys = keys;
        this.locationMonitor = locationMonitor;
        this.finishTask = finishTask;
    }

    public Animation(@NotNull List<KeyFrame> keys) {
        this(keys, null, null);
    }

    public Animation(@NotNull List<KeyFrame> keys, PlayableEffect locationMonitor, PlayableEffect finishTask) {
        this.locationMonitor = locationMonitor;
        this.finishTask = finishTask;

        for (KeyFrame kf : keys) {
            if (kf.key instanceof Location || kf.key instanceof Vector) {
                locs.add(kf);
            } else {
                List<PlayableEffect> ks = this.keys.get(kf.tick);
                if (ks == null) {
                    ks = new ArrayList<>();
                    this.keys.put(kf.tick, ks);
                }
                ks.add((PlayableEffect) kf.key);
            }
            maxTick = Math.max(maxTick, kf.tick);
        }
        locs.sort((kf1, kf2) -> Integer.compare(kf1.tick, kf2.tick));
        KeyFrame first = locs.get(0);
        if (first.tick != 0) {
            locs.add(0, new KeyFrame(0, first.key));
        }
        KeyFrame last = locs.get(locs.size() - 1);
        if (last.tick != maxTick) {
            locs.add(new KeyFrame(maxTick, last));
        }
        for (int i = 0; i < locs.size() - 1; i++) {
            KeyFrame cur = locs.get(i);
            KeyFrame next = locs.get(i + 1);
            if (next.tick - cur.tick > 1) {
                Vector curV = cur.key instanceof Location ? ((Location) cur.key).toVector() : ((Vector) cur.key);
                Vector nextV = next.key instanceof Location ? ((Location) next.key).toVector() : ((Vector) next.key);
                double diff = next.tick - cur.tick;
                Vector diffV = nextV.clone().subtract(curV);
                for (int m = 1; m < diff; m++) {
                    Vector v = curV.clone().add(diffV.clone().multiply(m / diff));
                    locs.add(i + m, new KeyFrame(cur.tick + m, v));
                }
                i += diff - 1;
            }
        }
    }

    public void play(Location origin) {
        play(origin, 0);
    }

    public void play(Location origin, int tick) {
        stop();
        if (tick > maxTick) {
            return;
        }
        KeyFrame kfLoc = locs.get(tick);
        Location loc = kfLoc.key instanceof Vector ? origin.clone().add((Vector) kfLoc.key) : origin.clone().add((Location) kfLoc.key);

        List<PlayableEffect> keys = this.keys.get(tick);
        if (keys != null) {
            for (PlayableEffect pf : keys) {
                pf.play(loc.clone());
            }
        }

        if (locationMonitor != null) {
            locationMonitor.play(loc.clone());
        }

        Location originWithRot = origin.clone();
        originWithRot.setPitch(loc.getPitch());
        originWithRot.setYaw(loc.getYaw());
        if (tick < maxTick) {
            nextTask = Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> play(originWithRot, tick + 1), 1);
        } else if (finishTask != null) {
            finishTask.play(loc.clone());
        }
    }

    public void stop() {
        if (nextTask != null) {
            nextTask.cancel();
            nextTask = null;
        }
    }

    public int getMaxTick() {
        return maxTick;
    }

    public Animation reverse() {
        List<KeyFrame> locs = new ArrayList<>(this.locs);
        Collections.reverse(locs);
        Map<Integer, List<PlayableEffect>> keys = new HashMap<>(this.keys.size());
        for (Map.Entry<Integer, List<PlayableEffect>> e : this.keys.entrySet()) {
            keys.put(maxTick - e.getKey(), e.getValue());
        }
        return new Animation(maxTick, locs, keys, locationMonitor, finishTask);
    }

    @Override
    public Animation clone() {
        return new Animation(maxTick, locs, keys);
    }

    public static TeleportEffect entTeleport(Entity ent) {
        return new TeleportEffect(ent);
    }

    public static RemoveEffect entRemove(Entity ent) {
        return new RemoveEffect(ent);
    }

    @Override
    public String toString() {
        return "Animation{" +
                "tick=" + tick +
                ", maxTick=" + maxTick +
                ", locs=" + locs +
                ", keys=" + keys +
                ", locationMonitor=" + locationMonitor +
                ", finishTask=" + finishTask +
                ", nextTask=" + nextTask +
                '}';
    }
}
