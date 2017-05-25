package com.mercy.compiler.BackEnd;

import com.mercy.Option;
import com.mercy.compiler.Entity.FunctionEntity;
import com.mercy.compiler.INS.Instruction;
import com.mercy.compiler.INS.Move;
import com.mercy.compiler.INS.Operand.Reference;

import java.util.*;

import static java.lang.System.err;

/**
 * Created by mercy on 17-5-23.
 */
public class Allocator {
    List<FunctionEntity> functionEntities;

    public Allocator (InstructionEmitter emitter) {
        functionEntities = emitter.functionEntities();
    }

    public void allocate() {
        for (FunctionEntity functionEntity : functionEntities) {
            if (Option.enableInlineFunction && functionEntity.canbeInlined())
                continue;
            init();
            allocateFunction(functionEntity);
        }
    }

    private void init() {
        // global
        edgeSet          = new LinkedHashSet<>();

        // node set (disjoint)
        simplifyWorklist = new LinkedHashSet<>();
        precolred        = new LinkedHashSet<>();
        initial          = new LinkedHashSet<>();
        freezeWorklist   = new LinkedHashSet<>();
        spillWorklist    = new LinkedHashSet<>();
        spilledNodes     = new LinkedHashSet<>();
        coalescedNodes   = new LinkedHashSet<>();
        coloredNodes     = new LinkedHashSet<>();
        selectStack      = new Stack<>();

        // move set (disjoint)
        coalescedMoves   = new LinkedHashSet<>();
        constrainedMoves = new LinkedHashSet<>();
        frozenMoves      = new LinkedHashSet<>();
        worklistMoves    = new LinkedHashSet<>();
        activeMoves      = new LinkedHashSet<>();
    }

    // aha
    Set<Edge> edgeSet;
    int K = 7;

    // node set (disjoint)
    Set<Reference> precolred;
    Set<Reference> initial;
    Set<Reference> simplifyWorklist;
    Set<Reference> freezeWorklist;
    Set<Reference> spillWorklist;
    Set<Reference> spilledNodes;
    Set<Reference> coalescedNodes;
    Set<Reference> coloredNodes;
    Stack<Reference> selectStack;

    // move set (disjoint)
    Set<Move> coalescedMoves;
    Set<Move> constrainedMoves;
    Set<Move> frozenMoves;
    Set<Move> worklistMoves;
    Set<Move> activeMoves;

    public void allocateFunction(FunctionEntity entity) {
        livenessAnalysis(entity);
        build(entity);
        makeWorklist();
        if (true)
            return;
        do {
            if (!simplifyWorklist.isEmpty())
                simplify();
            else if (!worklistMoves.isEmpty())
                coalesce();
            else if (!freezeWorklist.isEmpty())
                freeze();
            else if (!spillWorklist.isEmpty())
                selectSpill();

        } while (!simplifyWorklist.isEmpty() || !worklistMoves.isEmpty() || !freezeWorklist.isEmpty() || !spillWorklist.isEmpty());
        assignColors(entity);
        if (spilledNodes.isEmpty()) {
            rewriteProgram(entity);
        }
    }

    List<BasicBlock> sorted;
    Set<BasicBlock> visited = new HashSet<>();
    private void dfsSort(BasicBlock bb) {
        sorted.add(bb);
        visited.add(bb);
        for (BasicBlock pre : bb.predecessor()) {
            if (!visited.contains(pre)) {
                dfsSort(pre);
            }
        }
    }
    private void livenessAnalysis(FunctionEntity entity) {
        sorted = new LinkedList<>();

        // print Def and Use
        for (Instruction ins : entity.ins()) {
            err.printf("%-20s def:", ins.toString());
            for (Reference reference : ins.def()) {
                err.print(" " + reference);
            }
            err.print("       use: ");
            for (Reference reference : ins.use()) {
                err.print(" " + reference);
            }
            err.println();
        }
        err.println();

        /***** solve dataflow equation *****/
        // in block
        err.println(entity.name());
        for (BasicBlock basicBlock : entity.bbs()) {
            Set<Reference> def = basicBlock.def();
            Set<Reference> use = basicBlock.use();
            for (Instruction ins : basicBlock.ins()) {
                for (Reference ref : ins.use()) {
                    if (!def.contains(ref)) {
                        use.add(ref);
                    }
                }
                for (Reference ref : ins.def()) {
                    def.add(ref);
                }
            }
        }
        // sort blocks to boost iteration, iterate in reverse
        ListIterator li = entity.bbs().listIterator(entity.bbs().size());
        while (li.hasPrevious()) {
            BasicBlock pre = (BasicBlock) li.previous();
            if (!visited.contains(pre))
                dfsSort(pre);
        }

        // among blocks
        boolean modified = true;
        while (modified) {
            modified = false;
            for (BasicBlock bb : sorted) {
                Set<Reference> newIn = new HashSet<>();
                Set<Reference> right = new HashSet<>(bb.liveOut());
                right.removeAll(bb.def());
                newIn.addAll(bb.use());
                newIn.addAll(right);

                Set<Reference> newOut = new HashSet<>();
                for (BasicBlock suc : bb.successor()) {
                    newOut.addAll(suc.liveIn());
                }

                modified |= !bb.liveIn().equals(newIn) || !bb.liveOut().equals(newOut);

                bb.setLiveIn(newIn);
                bb.setLiveOut(newOut);
            }
        }
    }

    Set<Reference> tmp;
    private void build(FunctionEntity entity) {
        for (BasicBlock basicBlock : entity.bbs()) {
            HashSet<Reference> live = new HashSet<>(basicBlock.liveOut());

            // generate an iterator. Start just after the last element.
            ListIterator li = basicBlock.ins().listIterator(basicBlock.ins().size());
            while (li.hasPrevious()) {
                Instruction ins = (Instruction) li.previous();

                tmp= new HashSet<>(); tmp.addAll(live); ins.setOut(tmp);

                if (ins instanceof Move && ((Move) ins).isRefMove()) {
                   // System.out.println(ins.toString());
                    live.removeAll(ins.use());
                    for (Reference ref : ins.def())
                        ref.moveList.add((Move)ins);
                    for (Reference ref : ins.use())
                        ref.moveList.add((Move)ins);
                    worklistMoves.add((Move)ins);
                }
                live .addAll(ins.def());
                for (Reference d : ins.def()) {
                    for (Reference l : live) {
                        addEdge(d, l);
                    }
                }

                live.removeAll(ins.def());
                live.addAll(ins.use());

                tmp= new HashSet<>(); tmp.addAll(live); ins.setIn(tmp);
            }
        }

        // print Liveness Info
        for (Instruction ins : entity.ins()) {
            err.printf("%-20s in:", ins.toString());
            for (Reference reference : ins.in()) {
                err.print("  " + reference);
            }
            err.print("  out: ");
            for (Reference reference : ins.out()) {
                err.print("  " + reference);
            }
            err.println();
        }
        err.println();
    }

    private void makeWorklist() {
        for (Reference ref : initial) {
            if (ref.degree >= K) {
                spillWorklist.add(ref);
            } else if (isMoveRelated(ref)) {
                freezeWorklist.add(ref);
            } else {
                simplifyWorklist.add(ref);
            }
        }
    }

    private void simplify() {
        Reference ref = simplifyWorklist.iterator().next();
        simplifyWorklist.remove(ref);
        selectStack.push(ref);
        for (Reference adj : ref.adjList) {
            decreaseDegree(adj);
        }
    }

    private void decreaseDegree(Reference ref) {
        int d = ref.degree--;
        if (d == K) {
            enableMoves(ref);
            ref.adjList.forEach(this::enableMoves);
            spillWorklist.remove(ref);
            if (isMoveRelated(ref)) {
                freezeWorklist.add(ref);
            } else {
                simplifyWorklist.add(ref);
            }
        }
    }

    private boolean isMoveRelated(Reference ref) {
        for (Instruction ins : ref.moveList) {
            if (activeMoves.contains(ins) || worklistMoves.contains(ins))
                return true;
        }
        return false;
    }

    private void enableMoves(Reference ref) {
        for (Move move : ref.moveList) {
            if (activeMoves.contains(move)) {
                activeMoves.remove(move);
                worklistMoves.add(move);
            }
        }
    }

    private void addWorkList(Reference ref) {
        if (!precolred.contains(ref) && !isMoveRelated(ref) && ref.degree < K) {
            freezeWorklist.remove(ref);
            simplifyWorklist.add(ref);
        }
    }

    private boolean OK(Reference u, Reference v) {
        for (Reference t : v.adjList) {
            tmpEdge.u = t; tmpEdge.v = u;
            if (!(t.degree < K || precolred.contains(t) || edgeSet.contains(tmpEdge)))
                return false;
        }
        return true;
    }

    private boolean conservative(Reference u, Reference v) {
        int k = 0;
        Set<Reference> adjs = new HashSet<>();
        adjs.addAll(u.adjList); adjs.addAll(v.adjList);
        for (Reference ref : adjs) {
            if (ref.degree >= K)
                k++;
        }
        return k < K;
    }

    private Reference getAlias(Reference ref) {
        if (coalescedNodes.contains(ref)) {
            return getAlias(ref.alias);
        } else {
            return ref;
        }
    }

    private void combine(Reference u, Reference v) {
        if (freezeWorklist.contains(v)) {
            freezeWorklist.remove(v);
        } else {
            spillWorklist.remove(v);
        }

        coalescedNodes.add(v);
        v.alias = u;
        u.moveList.addAll(v.moveList);
        enableMoves(v);
        for (Reference t : v.adjList) {
            addEdge(t, u);
            decreaseDegree(t);
        }
        if (u.degree >= K && freezeWorklist.contains(u)) {
            freezeWorklist.remove(u);
            spillWorklist.add(u);
        }
    }

    private void coalesce() {
        Move move = worklistMoves.iterator().next();
        Reference x = getAlias((Reference) move.src());
        Reference y = getAlias((Reference) move.dest());
        Reference u, v;

        if (precolred.contains(y)) {
            u = y; v = x;
        } else {
            u = x; v = y;
        }

        tmpEdge.u = u; tmpEdge.v = v;
        worklistMoves.remove(move);
        if (u == v) {
            coalescedMoves.add(move);
            addWorkList(u);
        } else if (precolred.contains(v) || edgeSet.contains(tmpEdge)) {
            constrainedMoves.add(move);
            addWorkList(u);
            addWorkList(v);
        } else if (precolred.contains(u) && OK(v, u) ||
                !precolred.contains(u) && conservative(u, v)) {
            coalescedMoves.add(move);
            combine(u,v);
            addWorkList(u);
        } else {
            activeMoves.add(move);
        }

    }

    private void freeze() {
        Reference u = freezeWorklist.iterator().next();
        freezeWorklist.remove(u);
        simplifyWorklist.add(u);
        freezeMoves(u);
    }

    private void freezeMoves(Reference u) {
        for (Move move : u.moveList) {
            if (activeMoves.contains(move) || worklistMoves.contains(move)) {
                Reference v;
                if (getAlias((Reference)move.src()) == getAlias((Reference)move.dest())) {
                    v = getAlias((Reference) move.src());
                } else {
                    v = getAlias((Reference) move.dest());
                }
                activeMoves.remove(move);
                frozenMoves.add(move);
                boolean isEmpty = true;
                for (Move move1 : v.moveList) {
                    if (activeMoves.contains(move1) || worklistMoves.contains(move1)) {
                        isEmpty = false;
                        break;
                    }
                }
                if (isEmpty && freezeWorklist.contains(v)) {
                    freezeWorklist.remove(v);
                    simplifyWorklist.add(v);
                }
            }
        }
    }

    private void selectSpill() {

    }

    private void assignColors(FunctionEntity entity) {

    }

    private void rewriteProgram(FunctionEntity entity) {

    }


    /* small utility */
    private class Edge {
        public Reference u, v;

        @Override
        public boolean equals(Object o) {
            Edge edge = (Edge)o;
            return u == edge.u && v == edge.v;
        }
    }

    Edge tmpEdge = new Edge();
    private void addEdge(Reference u, Reference v) {
        if (u == v)
            return;
        tmpEdge.u = u;
        tmpEdge.v = v;
        if (!edgeSet.contains(tmpEdge)) {
            edgeSet.add(tmpEdge);
            if (!u.isPrecolored) {
                u.adjList.add(v);
                u.degree++;
            }
            if (!v.isPrecolored) {
                v.adjList.add(u);
                v.degree++;
            }
        }
    }
}
