package com.dthielke.permissions;

import java.util.*;

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

    public void addPermission(String name, boolean value) {
        // negative permissions take precedence
        if (permissions.containsKey(name) && !permissions.get(name)) {
            return;
        }
        permissions.put(name, value);
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

    public Map<String, Boolean> getPermissions() {
        return new HashMap<String, Boolean>(permissions);
    }

    public boolean hasChild(Group child) {
        return children.contains(child);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public boolean hasPermission(String name) {
        return permissions.get(name);
    }

    public void overwritePermission(String name, boolean value) {
        permissions.put(name, value);
    }

    public void removeChild(Group child) {
        children.remove(child);
    }

    public void removePermission(String name) {
        permissions.remove(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
