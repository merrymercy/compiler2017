package com.mercy.compiler.FrontEnd;

import com.mercy.compiler.Entity.Entity;

/**
 * Created by mercy on 17-5-22.
 */
public class DependenceEdge {
    public Entity base, rely;
    public boolean visited = false;
    public DependenceEdge (Entity base, Entity rely) {
        this.base = base;
        this.rely = rely;
    }

    @Override
    public boolean equals(Object o) {
        return hashCode() == o.hashCode();
    }

    @Override
    public int hashCode() {
        return base.hashCode() ^ rely.hashCode() + base.hashCode() * rely.hashCode();
    }
}