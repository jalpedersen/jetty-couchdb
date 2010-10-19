package org.signaut.couchdb;

import java.util.Arrays;

public class UserContext {
    private String name;
    private String roles[];

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "UserContext [name=" + name + ", roles=" + Arrays.toString(roles) + "]";
    }

}