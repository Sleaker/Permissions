package com.dthielke.permissions;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PermissionsPlugin extends JavaPlugin {
    private GroupManager groupManager;

    @Override
    public void onDisable() {
        clearGroupManager();
    }

    private void clearGroupManager() {
        groupManager.clear();
    }

    @Override
    public void onEnable() {
        setupGroupManager();
        registerEvents();
    }

    private void setupGroupManager() {
        // create the group manager
        groupManager = new GroupManager(getDataFolder(), new PermissionAttachmentFactory(this));

        // load users for any online players
        for (Player player : getServer().getOnlinePlayers()) {
            groupManager.loadUser(player);
        }
    }

    private void registerEvents() {
        PlayerListener listener = new MyPlayerListener(groupManager);
        PluginManager pluginManager = getServer().getPluginManager();

        // lowest priority for PLAYER_JOIN is important because we want other plugins to be able to set permissions
        // that take precedence over our own and bukkit naturally gives precedence to the most recent attachment
        pluginManager.registerEvent(Event.Type.PLAYER_JOIN, listener, Event.Priority.Lowest, this);
        pluginManager.registerEvent(Event.Type.PLAYER_QUIT, listener, Event.Priority.Normal, this);
        pluginManager.registerEvent(Event.Type.PLAYER_CHANGED_WORLD, listener, Event.Priority.Normal, this);
    }
}
