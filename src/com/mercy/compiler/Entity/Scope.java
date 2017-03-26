package com.mercy.compiler.Entity;

import com.mercy.compiler.Utility.SemanticError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mercy on 17-3-18.
 */
public class Scope {
    private Map<String, Entity> entities = new HashMap<>();
    private List<Scope> children = new ArrayList<>();
    private Scope parent;
    private boolean isToplevel;

    public Scope(boolean isToplevel) {
        this.isToplevel = isToplevel;
    }

    public Scope(Scope parent) {
        this.parent = parent;
        this.isToplevel = (parent == null);
        if (this.parent != null) {
            this.parent.addChildren(this);
        }
    }

    public void insert(Entity entity) {
        if (entities.containsKey(entity.name()))
            throw new SemanticError(entity.location(), "duplicated symbol : " + entity.name());
        entities.put(entity.name(), entity);
    }

    public void insertConstant(Entity entity) {
        Scope scope = this;
        while (!scope.isToplevel) {
            scope = scope.parent;
        }
        if (entities.containsKey(entity.name()))
            throw new SemanticError(entity.location(), "duplicated string constant : " + entity.name());
        entities.put(entity.name(), entity);
    }

    // search in entire symbol table
    public Entity lookup(String name) {
        Entity entity = entities.get(name);
        if (entity == null) {
            return isToplevel ? null : parent.lookup(name);
        } else {
            return entity;
        }
    }

    // only search in current level
    public Entity find(String name) {
        return entities.get(name);
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

    public void addChildren(Scope s) {
        children.add(s);
    }

    public boolean isToplevel() {
        return isToplevel;
    }

}
