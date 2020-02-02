package io.github.kristianvld.CraftingAnimations;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main pl;
    private CleanupManager cleanupManager;

    @Override
    public void onEnable() {
        pl = this;

        cleanupManager = new CleanupManager(this);

        new TransferListener(this);
        new AnvilListener(this);
        new EnchantmentListener(this);
        new CraftingListener(this);
    }

    @Override
    public void onDisable() {
        cleanupManager.removeAll();
    }

    public static Main getPlugin() {
        return pl;
    }
}
