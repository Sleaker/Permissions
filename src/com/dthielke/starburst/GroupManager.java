package com.dthielke.starburst;

import org.bukkit.entity.Player;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GroupManager {
    private static final FilenameFilter FILTER = new YAMLFilter();
    private static final int YAML_EXTENSION_LENGTH = 4;

    private Map<String, GroupSet> worldSets = new HashMap<String, GroupSet>();
    private Map<Player, User> users = new HashMap<Player, User>();
    private GroupSet defaultGroupSet;
    private PermissionAttachmentFactory factory;

    static class YAMLFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(".yml");
        }
    }

    public GroupManager(File configFolder, PermissionAttachmentFactory factory) {
        // create the default world settings
        File defaultGroupFile = new File(configFolder, "defaults.yml");
        defaultGroupSet = new GroupSet(defaultGroupFile);

        // load settings for each world file
        for (File file : configFolder.listFiles(FILTER)) {
            // skip the default file
            if (file.equals(defaultGroupFile)) {
                continue;
            }

            // get the world name
            String world = file.getName();
            world = world.substring(0, world.length() - YAML_EXTENSION_LENGTH).toLowerCase();

            // load the groups
            GroupSet set = new GroupSet(file);
            set.loadGroups();

            // apply the default group settings
            for (Group group : set.getGroups()) {
                group.addChild(defaultGroupSet.getGroup(group.getName()));
            }

            // copy non-existent groups
            for (Group group : defaultGroupSet.getGroups()) {
                if (!set.hasGroup(group.getName())) {
                    set.addGroup(group);
                }
            }

            // copy the default group if needed
            if (set.getDefaultGroup() == null) {
                set.setDefaultGroup(defaultGroupSet.getDefaultGroup());
            }

            // add the new group set to the map
            worldSets.put(world, set);
        }

        // assign the permission attachment factory
        this.factory = factory;
    }

    public void clear() {
        // remove all worldSets
        for (GroupSet set : getWorldSets()) {
            set.clear();
        }
        worldSets.clear();

        // remove all users
        for (User user : getUsers()) {
            removeUser(user);
        }
    }

    public User getUser(Player player) {
        return users.get(player);
    }

    public Set<User> getUsers() {
        return new HashSet<User>(users.values());
    }

    public Set<GroupSet> getWorldSets() {
        return new HashSet<GroupSet>(worldSets.values());
    }

    public void loadUser(Player player) {
        // get the group set to be used
        GroupSet set;
        String world = player.getWorld().getName().toLowerCase();
        if (worldSets.containsKey(world)) {
            set = worldSets.get(world);
        } else {
            set = defaultGroupSet;
        }

        // get the user
        User user;
        if (users.containsKey(player)) {
            user = users.get(player);
        } else {
            user = new User(factory, player);
        }

        // load the user's data from the group set
        set.loadUser(user);
        users.put(player, user);
    }

    public void removeUser(User user) {
        if (user != null) {
            user.removeAttachment();
            users.remove(user.getPlayer());
        }
    }

    public void removeUser(Player player) {
        removeUser(getUser(player));
    }
}
