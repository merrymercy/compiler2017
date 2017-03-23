package com.mercy.compiler.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by mercy on 17-3-18.
 */
public class Scope {
    private Map<String, Entity> entities;
    private List<Scope> children;
    private Scope parent;
    private boolean isToplevel;

    public Scope(boolean isToplevel) {
        children = new ArrayList<>();
        this.isToplevel = isToplevel;
    }

    public Scope parent() {
        return isToplevel ? null : parent;
    };

    public Map<String, Entity> entities() {
        return entities;
    }

    public List<Scope> children() {
        return children;
    }

    public boolean isToplevel() {
        return isToplevel;
    }

    protected void addChildren(Scope s) {
        children.add(s);
    }
}
