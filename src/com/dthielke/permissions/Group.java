package com.dthielke.permissions;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Group {
    private final String name;
    private Map<String, Boolean> permissions = new LinkedHashMap<String, Boolean>();
    private Set<Group> children = new HashSet<Group>();

    public Group(String name) {
        this.name = name;
    }

    public void addChild(Group child) {
        if (child != null) {
            children.add(child);
        }
    }

    public void addPermission(String name, boolean value, boolean overwrite) {
        // negative permissions take precedence unless we're overwriting the value
        if (overwrite || !permissions.containsKey(name) || permissions.get(name)) {
            permissions.put(name, value);
        }
    }

    public Map<String, Boolean> aggregatePermissions() {
        Map<String, Boolean> aggregate = new LinkedHashMap<String, Boolean>();

        // collect child group permissions
        for (Group child : children) {
            Map<String, Boolean> childPerms = child.aggregatePermissions();
            for (Map.Entry<String, Boolean> permission : childPerms.entrySet()) {
                String name = permission.getKey();
                if (!(aggregate.containsKey(name) && !aggregate.get(name))) {
                    aggregate.put(name, permission.getValue());
                }
            }
        }

        // add the current group's permissions on top of them
        aggregate.putAll(permissions);

        return aggregate;
    }

    public void clear() {
        permissions.clear();
        children.clear();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Group && name.equals(((Group) obj).name);
    }

    public Set<Group> getChildren() {
        return new HashSet<Group>(children);
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
