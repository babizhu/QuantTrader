package org.bbz.stock.quanttrader.http.handler.auth;

import java.util.HashSet;
import java.util.Set;

class PermisstionAndRoleSet{
    private Set<String> permissions = new HashSet<>();
    private Set<String> roles = new HashSet<>();
    private Set<String> all = new HashSet<>();

    void addRoles( Set<String> rolesSet ){
        roles.addAll( rolesSet );
        all.addAll( roles );
    }

    void addPermissions( Set<String> permissionsSet ){
        permissions.addAll( permissionsSet );
        all.addAll( permissionsSet );
    }

    @Override
    public String toString(){
        return "PermisstionAndRoleSet{" +
                "permissions=" + permissions +
                ", roles=" + roles +
                '}';
    }

    boolean contains( Set<String> set ){
        for( String s : set ) {
            if( all.contains( s ) ) {
                return true;
            }
        }
        return false;
    }
}