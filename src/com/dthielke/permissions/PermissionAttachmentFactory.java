package com.dthielke.permissions;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

public class PermissionAttachmentFactory {
    private JavaPlugin plugin;

    public PermissionAttachmentFactory(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public PermissionAttachment createPermissionAttachment(Player player) {
        return player.addAttachment(plugin);
    }
}
