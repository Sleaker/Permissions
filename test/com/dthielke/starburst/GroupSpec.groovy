package com.dthielke.starburst;

import spock.lang.Specification

class GroupSpec extends Specification {

    def group1, group2, group3

    def setup() {
        group1 = new Group("group1")
        group2 = new Group("group2")
        group3 = new Group("group3")
    }

    def "test permission aggregation when both child and parent groups specify a permission"() {
        given:
        group1.addChild(group2)
        group1.addPermission("permission", rootValue, false)
        group2.addPermission("permission", childValue, false)

        when:
        def aggregate = group1.aggregatePermissions()

        then:
        aggregate.get("permission") == rootValue

        where:
        rootValue | childValue
        true      | true
        true      | false
        false     | true
        false     | false
    }

    def "test permission aggregation when only child specifies a permission"() {
        given:
        group1.addChild(group2)
        group2.addPermission("permission", childValue, false)

        when:
        def aggregate = group1.aggregatePermissions()

        then:
        aggregate.get("permission") == childValue

        where:
        childValue << [true, false]
    }

    def "test permission aggregation when one child specifies a permission and the other negates it"() {
        given:
        group1.addChild(group2)
        group1.addChild(group3)
        group2.addPermission("permission", firstChildValue, false)
        group2.addPermission("permission", secondChildValue, false)

        when:
        def aggregate = group1.aggregatePermissions()

        then:
        !aggregate.get("permission")

        where:
        firstChildValue | secondChildValue
        true            | false
        false           | true
    }

    def "test aggregate permission sorting"() {
        given:
        group1.addChild(group2)
        group1.addPermission("my.perms.admin", false, false) // negated admin node
        group2.addPermission("my.perms.mod", false, false)   // negated mod node
        group2.addPermission("my.perms.*", true, false)      // positive wildcard node

        when:
        Map<String, Boolean> aggregate = group1.aggregatePermissions()
        def entryList = new ArrayList<Map.Entry<String, Boolean>>(aggregate.entrySet())


        then:
        entryList.size() == 3
        entryList.get(0).key == "my.perms.*"
        entryList.get(0).value
        entryList.get(1).key == "my.perms.admin"
        !entryList.get(1).value
        entryList.get(2).key == "my.perms.mod"
        !entryList.get(2).value
    }
}