package com.mercy.compiler.Entity;

import com.mercy.compiler.Utility.SemanticError;

import java.util.*;

/**
 * Created by mercy on 17-3-18.
 */
public class Scope {
    private Map<String, Entity> entities = new HashMap<>();
    private List<Scope> children = new ArrayList<>();
    private Scope parent;
    private boolean isTopLevel;

    public Scope(boolean isTopLevel) {
        this.isTopLevel = isTopLevel;
    }

    public Scope(Scope parent) {
        this.parent = parent;
        this.isTopLevel = (parent == null);
        if (this.parent != null) {
            this.parent.addChildren(this);
        }
    }

    public void insert(Entity entity) {
        if (entities.containsKey(entity.name()))
            throw new SemanticError(entity.location(), "duplicated symbol : " + entity.name());
        entities.put(entity.name(), entity);
    }

    // search in entire symbol table
    public Entity lookup(String name) {
        Entity entity = entities.get(name);
        if (entity == null) {
            return isTopLevel ? null : parent.lookup(name);
        } else {
            return entity;
        }
    }

    // only search in current level
    public Entity lookupCurrentLevel(String name) {
        return entities.get(name);
    }

    // getter and setter
    public Map<String, Entity> entities() {
        return entities;
    }

    public List<Scope> children() {
        return children;
    }
    public void addChildren(Scope s) {
        children.add(s);
    }

    public Scope parent() {
        return isTopLevel ? null : parent;
    }
    public boolean isTopLevel() {
        return isTopLevel;
    }

    // locate local variable
    public int locateLocalVariable(int base, int align) {
        int offset = 0;
        for (Entity entity : entities.values()) {
            if (entity instanceof VariableEntity) {
                offset += entity.type.size();
                entity.setOffset(offset + base);

                offset += (align - offset % align) % align; // can be optimized here, use shift
            }
        }
        int maxi = 0;
        for (Scope child : children) {
            int tmp = child.locateLocalVariable(base + offset, align);
            if (tmp > maxi)
                maxi = tmp;
        }
        return offset + maxi;
    }

    // set offset in class
    public int locateMember(int align) {
        int offset = 0;
        for (Entity entity : entities.values()) {
            if (!(entity instanceof FunctionEntity)) {
                entity.setOffset(offset);
                offset += entity.size();

                offset += (align - offset % align) % align; // can be optimized here, use shift
            }
        }
        return offset;
    }

    // all variable entities
    public List<VariableEntity> allLocalVariables() {
        List<VariableEntity> ret = new LinkedList<>();

        for (Entity entity : entities.values()) {
            if (entity instanceof VariableEntity) {
                ret.add((VariableEntity)entity);
            }
        }

        for (Scope child : children) {
            ret.addAll(child.allLocalVariables());
        }

        return ret;
    }

    // all entities
    public Set<Entity> gatherAll() {
        Set<Entity> ret = new HashSet<>();

        for (Entity entity : entities.values()) {
            if (entity instanceof FunctionEntity) {
                if (!((FunctionEntity) entity).isLibFunction())
                    ret.addAll(((FunctionEntity) entity).params());
            }
            ret.add(entity);
        }

        for (Scope child : children) {
            ret.addAll(child.allLocalVariables());
        }
        return ret;
    }
}