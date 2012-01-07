package com.dthielke.permissions;

import java.util.*;

public class Group {
    private final String name;
    private Map<String, Boolean> permissions = new TreeMap<String, Boolean>();
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
                Boolean existingValue = aggregate.get(name);
                // add each permission that we don't have unless it would override a negated node
                if (existingValue == null || !existingValue) {
                    aggregate.put(name, permission.getValue());
                }
            }
        }

        // add the current group's permissions on top of them
        aggregate.putAll(permissions);

        // sort by value so that negated permissions come last
        Map<String, Boolean> sortedAggregate = new LinkedHashMap<String, Boolean>();
        SortedSet<Map.Entry<String, Boolean>> sortedEntries = entriesSortedByValues(aggregate);
        for (Map.Entry<String, Boolean> entry : sortedEntries) {
            sortedAggregate.put(entry.getKey(), entry.getValue());
        }

        return sortedAggregate;
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

    static <K extends Comparable<? super K>, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>> entriesSortedByValues(Map<K, V> map) {
        SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<Map.Entry<K, V>>(
                new Comparator<Map.Entry<K, V>>() {
                    @Override
                    public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
                        // compare values first (swapped direction because we want negated nodes at the top)
                        int res = -e1.getValue().compareTo(e2.getValue());
                        // compare keys only if values are equal
                        return res != 0 ? res : e1.getKey().compareTo(e2.getKey());
                    }
                }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }
}
