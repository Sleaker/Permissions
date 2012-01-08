package com.dthielke.starburst;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.Map;

public class User extends Group {
    private final Player player;
    private PermissionAttachment attachment;

    public User(PermissionAttachmentFactory factory, Player player) {
        super(player.getName());
        this.player = player;
        this.attachment = factory.createPermissionAttachment(player);
    }

    public void applyPermissions() {
        // clear old permissions
        for (Map.Entry<String, Boolean> permission : attachment.getPermissions().entrySet()) {
            attachment.unsetPermission(permission.getKey());
        }

        // add current permissions
        Map<String, Boolean> permissions = this.aggregatePermissions();
        for (Map.Entry<String, Boolean> permission : permissions.entrySet()) {
            attachment.setPermission(permission.getKey(), permission.getValue());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof User) {
            return player.equals(((User) obj).getPlayer());
        } else {
            return super.equals(obj);
        }
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public int hashCode() {
        return player.hashCode();
    }

    public void removeAttachment() {
        // detach the attachment from the player
        if (attachment.getPermissible().equals(player)) {
            player.removeAttachment(attachment);
        }

        // remove all permissions from the attachment to be thorough
        for (Map.Entry<String, Boolean> permission : attachment.getPermissions().entrySet()) {
            attachment.unsetPermission(permission.getKey());
        }
    }
}
