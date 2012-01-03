package com.dthielke.permissions;

import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

public class MyPlayerListener extends PlayerListener {
    private final GroupManager groupManager;

    public MyPlayerListener(GroupManager groupManager) {
        this.groupManager = groupManager;
    }

    @Override
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        groupManager.loadUser(event.getPlayer());
    }

    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        groupManager.loadUser(event.getPlayer());
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        groupManager.removeUser(event.getPlayer());
    }
}
