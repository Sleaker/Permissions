package com.dthielke.starburst;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GroupSet {
    private Map<String, Group> groups = new HashMap<String, Group>();
    private Group defaultGroup;
    private FileConfiguration config;

    public GroupSet(File configFile) {
        // create a default config with empty group and user sections
        MemoryConfiguration defaultConfig = new MemoryConfiguration();
        defaultConfig.set("default", "YOUR DEFAULT GROUP GOES HERE!");
        defaultConfig.createSection("groups");
        defaultConfig.createSection("users");

        // create the actual config and apply the defaults
        config = new YamlConfiguration();
        config.options().copyDefaults(true);
        config.setDefaults(defaultConfig);

        // load existing data
        if (configFile.exists()) {
            try {
                config.load(configFile);
            } catch (IOException e) {
                StarburstPlugin.warning("Could not load file: " + configFile.getAbsolutePath());
            } catch (InvalidConfigurationException e) {
                StarburstPlugin.warning("Invalid configuration file: " + configFile.getAbsolutePath());
            }
        }

        // save the file
        try {
            config.save(configFile);
        } catch (IOException e) {
            StarburstPlugin.warning("Could not save file: " + configFile.getAbsolutePath());
        }
    }

    public void addGroup(Group group) {
        groups.put(group.getName(), group);
    }

    public void clear() {
        // remove all groups
        for (Group group : getGroups()) {
            group.clear();
            removeGroup(group);
        }
    }

    public Group getDefaultGroup() {
        return defaultGroup;
    }

    public Group getGroup(String name) {
        return groups.get(name);
    }

    public Set<Group> getGroups() {
        return new HashSet<Group>(groups.values());
    }

    public boolean hasGroup(String name) {
        return groups.containsKey(name);
    }

    public void loadGroup(ConfigurationSection config, Group group) {
        // clear the group's existing data
        group.clear();

        // populate permissions
        List<String> permissions = config.getStringList("permissions");
        for (String permission : permissions) {
            String key;
            Boolean value;

            // check if the permission is negated
            if (permission.startsWith("^")) {
                key = permission.substring(1);
                value = false;
            } else {
                key = permission;
                value = true;
            }

            // add the permission
            group.addPermission(key, value, false);
        }

        // add child groups
        List<String> children = config.getStringList("groups");
        for (String childName : children) {
            Group child = getGroup(childName);

            if (child == null) {
                StarburstPlugin.warning("Child group not found: " + childName + " in " + group.getName());
            } else if (hasCircularReference(group, child)) {
                StarburstPlugin.warning("Circular child reference found: " + childName + " in " + group.getName());
            } else {
                group.addChild(child);
            }
        }
    }

    public void loadGroups() {
        ConfigurationSection groupSection = config.getConfigurationSection("groups");
        Set<String> groupNames = groupSection.getKeys(false);

        // create all the groups first so they're available for inheritance structuring
        for (String group : groupNames) {
            addGroup(new Group(group));
        }

        // populate each group
        for (String group : groupNames) {
            loadGroup(groupSection.getConfigurationSection(group), getGroup(group));
        }

        // set the default group
        String defaultGroupName = config.getString("default");
        defaultGroup = getGroup(defaultGroupName);
    }

    public User loadUser(User user) {
        // clear existing data
        user.clear();

        // load the user data if there is any
        String sectionName = "users." + user.getPlayer().getName();
        if (config.isConfigurationSection(sectionName)) {
            ConfigurationSection section = config.getConfigurationSection(sectionName);
            loadGroup(section, user);
        } else {
            user.addChild(defaultGroup);
        }

        // apply the user's permissions
        user.applyPermissions();

        // add the user to the map
        return user;
    }

    public void removeGroup(Group group) {
        groups.remove(group.getName());
    }

    public void setDefaultGroup(Group group) {
        if (group != null) {
            defaultGroup = getGroup(group.getName());
        } else {
            defaultGroup = null;
        }
    }

    private static boolean hasCircularReference(Group group, Group child) {
        if (child.equals(group)) {
            return true;
        }

        for (Group grandChild : child.getChildren()) {
            if (hasCircularReference(group, grandChild)) {
                return true;
            }
        }

        return false;
    }
}
