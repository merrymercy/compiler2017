package com.mercy.compiler.BackEnd;

import com.mercy.Option;
import com.mercy.compiler.Entity.FunctionEntity;
import com.mercy.compiler.Entity.ParameterEntity;
import com.mercy.compiler.INS.Instruction;
import com.mercy.compiler.INS.Label;
import com.mercy.compiler.INS.Move;
import com.mercy.compiler.INS.Operand.Address;
import com.mercy.compiler.INS.Operand.Reference;
import com.mercy.compiler.INS.Operand.Register;
import com.mercy.compiler.Utility.InternalError;

import java.util.*;

import static java.lang.System.err;

/**
 * Created by mercy on 17-5-23.
 */
public class Allocator {
    List<FunctionEntity> functionEntities;
    RegisterConfig regConfig;
    Set<Register> colors = new HashSet<>();

    Register rbp;

    public Allocator (InstructionEmitter emitter, RegisterConfig regConfig) {
        functionEntities = emitter.functionEntities();
        this.regConfig = regConfig;

        // load registers
        rbp = regConfig.rbp();

        // init colors

        colors.add(regConfig.registers().get(10));
        colors.add(regConfig.registers().get(11));

        colors.add(regConfig.registers().get(1));
        colors.add(regConfig.registers().get(12));
        colors.add(regConfig.registers().get(13));
        colors.add(regConfig.registers().get(14));
        colors.add(regConfig.registers().get(15));
        K = colors.size();
    }

    public void allocate() {
        for (FunctionEntity functionEntity : functionEntities) {
            if (Option.enableInlineFunction && functionEntity.canbeInlined())
                continue;
            init(functionEntity);
            allocateFunction(functionEntity);

            // set register
            Set<Reference> allRef = new HashSet<>();
            Set<Register>  regUsed = new HashSet<>();

            for (BasicBlock basicBlock : functionEntity.bbs()) {
                for (Instruction ins : basicBlock.ins()) {
                    for (Reference ref : ins.allref()) {
                        allRef.add(ref);
                        if (ref.color != null) {
                            ref.setRegister(ref.color);
                            regUsed.add(ref.color);
                        }
                    }
                }
            }

            functionEntity.setAllReference(allRef);
            LinkedList<Register> listRegUse = new LinkedList<>(regUsed);
            functionEntity.setRegUsed(listRegUse);
            functionEntity.regUsed().add(rbp);
            functionEntity.setLocalVariableOffset(localOffset);
        }
    }

    private void init(FunctionEntity entity) {
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
        selectWorklist   = new LinkedHashSet<>();
        selectStack      = new Stack<>();

        // move set (disjoint)
        coalescedMoves   = new LinkedHashSet<>();
        constrainedMoves = new LinkedHashSet<>();
        frozenMoves      = new LinkedHashSet<>();
        worklistMoves    = new LinkedHashSet<>();
        activeMoves      = new LinkedHashSet<>();

        // sort blocks to boost iteration, iterate in reverse
        sorted = new LinkedList<>();
        ListIterator li = entity.bbs().listIterator(entity.bbs().size());
        while (li.hasPrevious()) {
            BasicBlock pre = (BasicBlock) li.previous();
            if (pre.label() == entity.beginLabelINS()) {
                Set<Reference> bringin = new HashSet<>();
                for (ParameterEntity par : entity.params()) {   // load parameters
                    bringin.add(par.reference());
                }
                pre.label().setBringIn(bringin);
            }
            if (!visited.contains(pre))
                dfsSort(pre);
        }

        // for spilled node
        localOffset = 0;
    }

    // aha
    Set<Edge> edgeSet;
    int K;
    int localOffset;

    // node set (disjoint)
    Set<Reference> precolred;
    Set<Reference> initial;
    Set<Reference> simplifyWorklist;
    Set<Reference> freezeWorklist;
    Set<Reference> spillWorklist;
    Set<Reference> spilledNodes;
    Set<Reference> coalescedNodes;
    Set<Reference> coloredNodes;
    Set<Reference> selectWorklist;
    Stack<Reference> selectStack;

    // move set (disjoint)
    Set<Move> coalescedMoves;
    Set<Move> constrainedMoves;
    Set<Move> frozenMoves;
    Set<Move> worklistMoves;
    Set<Move> activeMoves;

    int iter;
    public void allocateFunction(FunctionEntity entity) {
        err.println("allocate for " + entity.name());
        boolean finish = false;
        iter = 0;
        do {
            err.println(" === iter " + iter + " ===");
            livenessAnalysis(entity);
            build(entity);
            makeWorklist();
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
            finish = spilledNodes.isEmpty();
            rewriteProgram(entity);
            iter++;
        } while (!finish);

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
        // print Def and Use
        /*if (Option.printUseDefInfo) {
            err.println("====== USE & DEF ======");
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
        }*/

        /***** solve dataflow equation *****/
        // in block
        for (BasicBlock basicBlock : entity.bbs()) {
            Set<Reference> def = basicBlock.def();
            Set<Reference> use = basicBlock.use();
            basicBlock.liveIn().clear();
            basicBlock.liveOut().clear();
            def.clear();
            use.clear();
            for (Instruction ins : basicBlock.ins()) {
                for (Reference ref : ins.use()) {
                    if (!def.contains(ref)) {
                        use.add(ref);
                    }
                }
                for (Reference ref : ins.def()) {
                    def.add(ref);
                }

                if (iter == 0) { // first iteration
                    initial.addAll(ins.allref());
                }
            }
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
        // init edge and degree
        edgeSet.clear();
        initial.removeAll(precolred);
        for (Reference ref : initial) {
            ref.reset();
        }
        for (Reference ref : precolred) {
            ref.reset();
        }

        // make inference graph
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

        if (Option.printUseDefInfo) {
            // print Liveness Info
            /*err.println("====== IN & OUT ======");
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
            err.println();*/
        }

        err.println("====== EDGE ======");
        for (Reference u : initial) {
            err.printf("%-10s:", u.name());
            for (Reference v : u.adjList) {
                err.print( "  " + v.name());
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
        selectWorklist.add(ref);
        selectStack.push(ref);
        for (Reference adj : ref.adjList) {
            if (!selectWorklist.contains(adj))
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
                if (ref.type() == Reference.Type.GLOBAL)
                    err.print("NIMA");
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
            if (!(t.degree < K || precolred.contains(t) || edgeSet.contains(getEdge(t, u))))
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
            return ref.alias = getAlias(ref.alias);
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

        worklistMoves.remove(move);
        if (u == v) {
            coalescedMoves.add(move);
            addWorkList(u);
        } else if (precolred.contains(v) || edgeSet.contains(getEdge(u, v))) {
            constrainedMoves.add(move);
            addWorkList(u);
            addWorkList(v);
        } else if (precolred.contains(u) && OK(u, v) ||
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
        // SPILL HEURISTIC HERE
        Reference ref = spillWorklist.iterator().next();
        spillWorklist.remove(ref);
        simplifyWorklist.add(ref);
        freezeMoves(ref);
    }

    Set<Register> okColors = new HashSet<>();
    private void assignColors(FunctionEntity entity) {
        while(!selectStack.empty()) {
            Reference n = selectStack.pop();
            okColors.clear();
            for (Register color : colors) {
                okColors.add(color);
            }

            for (Reference w : n.adjList) {
                if (coloredNodes.contains(w) || precolred.contains(w)) {
                    okColors.remove(getAlias(w).color);
                }
            }

            if (okColors.isEmpty()) {
                err.println("spill " + n.name());
                spilledNodes.add(n);
                n.color = null;
            } else {
                Register color = okColors.iterator().next();
                err.println("assign " + n.name() + " -> " + color.name());
                coloredNodes.add(n);
                n.color = color;
            }
        }

        for (Reference node : coalescedNodes) {
            node.color = getAlias(node).color;
            if (node.color == null) {
                err.println("TMD");
                err.flush();
                throw new InternalError(entity.name());
            }
        }

        err.println("=== Assign Result ===");
        err.print("colored :");
        for (Reference ref : coloredNodes) {
            err.print("  " + ref.name() + "(" + ref.color.name() + ")");
        }
        err.print("\ncoalesced :");
        for (Reference ref : coalescedNodes) {
            err.print("  " + ref.name() + "(" + getAlias(ref).name() + ")");
        }
        err.print("\nspilled :");
        for (Reference ref : spilledNodes) {
            err.print("  " + ref.name());
        }
        err.println();
    }

    int spilledCounter = 0;
    private void rewriteProgram(FunctionEntity entity) {
        Set<Reference> newTemp = new HashSet<>();
        List<Instruction> newIns;

        // allocate memory offset for spilled nodes
        for (Reference ref : spilledNodes) {
            ref.isSpilled = true;
            localOffset += Option.REG_SIZE;
            ref.setOffset(-localOffset, rbp);
        }

        // path compression
        for (Reference ref : coalescedNodes) {
            getAlias(ref);
        }

        // rewrite program
        List<Instruction> stores = new LinkedList<>();
        for (BasicBlock basicBlock : entity.bbs()) {
            newIns = new LinkedList<>();
            for (Instruction ins : basicBlock.ins()) {
                Set<Reference> insUse = ins.use();
                Set<Reference> insDef = ins.def();

                stores.clear();
                if (!(ins instanceof Label)) {
                    for (Reference use : insUse) {
                        if (use.isSpilled) {
                            if (insDef.contains(use)) {
                                Reference tmp = new Reference("spill_add_" + spilledCounter++, Reference.Type.UNKNOWN);
                                newTemp.add(tmp);
                                newIns.add(new Move(tmp, new Address(rbp, null, 1, use.offset())));
                                ins.replaceUse(use, tmp);
                                ins.replaceDef(use, tmp);
                                stores.add(new Move(new Address(rbp, null, 1, use.offset()), tmp));
                            } else {
                                Reference tmp = new Reference("spill_add_" + spilledCounter++, Reference.Type.UNKNOWN);
                                newTemp.add(tmp);
                                newIns.add(new Move(tmp, new Address(rbp, null, 1, use.offset())));
                                ins.replaceUse(use, tmp);
                            }
                        }
                    }
                    for (Reference def : insDef) {
                        if (def.isSpilled) {
                            if (insUse.contains(def)) {
                                ; //already done in previous step
                            } else {
                                Reference tmp = new Reference("spill_add_" + spilledCounter++, Reference.Type.UNKNOWN);
                                newTemp.add(tmp);
                                ins.replaceDef(def, tmp);   // improve replace to be able to replace operand
                                stores.add(new Move(new Address(rbp, null, 1, def.offset()), tmp));
                            }
                        }
                    }
                }

                for (Reference ref : ins.allref()) {
                    if (coalescedNodes.contains(ref)) {
                        ins.replaceAll(ref, getAlias(ref));
                    }
                }
                ins.initDefAndUse();
                ins.calcDefAndUse();
                if (ins instanceof Move && ((Move) ins).isRefMove() && ((Move) ins).dest() == ((Move) ins).src())
                    ;
                else
                    newIns.add(ins);
                newIns.addAll(stores);
            }
            basicBlock.setIns(newIns);
        }

        for (ParameterEntity par : entity.params()) {
            if (coalescedNodes.contains(par.reference())) {
                par.setReference(getAlias(par.reference()));
            }
            if (coloredNodes.contains(par.reference())) {
                par.reference().setRegister(par.reference().color);
            }
        }


        err.println("===== REWRITE =====");
        for (BasicBlock basicBlock : entity.bbs()) {
            for (Instruction ins : basicBlock.ins()) {
                err.println(ins);
            }
        }

        // restart
        selectStack.clear();
        selectWorklist.clear();
        spilledNodes.clear();
        initial.clear();
        initial.addAll(coloredNodes);
        initial.addAll(coalescedNodes);
        initial.addAll(newTemp);
        coloredNodes.clear();
        coalescedNodes.clear();
    }

    /* small utility */
    private class Edge {
        public Reference u, v;

        public Edge(Reference u, Reference v) {
            this.u = u;
            this.v = v;
        }

        @Override
        public int hashCode() {
            return u.hashCode() + v.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            Edge edge = (Edge)o;
            return u == edge.u && v == edge.v;
        }
    }

    private void addEdge(Reference u, Reference v) {
        if (u == v)
            return;
        Edge edge = getEdge(u, v);
        if (!edgeSet.contains(edge)) {
           // err.println("add edge " + u.name() + "  " + v.name());
            edgeSet.add(edge);
            edgeSet.add(getEdge(v, u));
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

    HashMap<Edge, Edge> edgeEdgeHashMap = new HashMap<>();
    private Edge getEdge(Reference u, Reference v) {
        Edge tempEdge = new Edge(u, v);
        Edge find = edgeEdgeHashMap.get(tempEdge);
        if (find == null) {
            edgeEdgeHashMap.put(tempEdge, tempEdge);
            return tempEdge;
        } else {
            return find;
        }
    }
}
