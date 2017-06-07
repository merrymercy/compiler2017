package com.mercy.compiler.BackEnd;

import com.mercy.Option;
import com.mercy.compiler.Entity.FunctionEntity;
import com.mercy.compiler.Entity.ParameterEntity;
import com.mercy.compiler.Entity.Scope;
import com.mercy.compiler.INS.*;
import com.mercy.compiler.INS.Operand.*;
import com.mercy.compiler.Utility.InternalError;

import java.util.*;

import static java.lang.System.err;

/**
 * Created by mercy on 17-5-23.
 */

// George, Lal; Appel, Andrew W. (May 1996). "Iterated Register Coalescing"
public class Allocator {
    private List<FunctionEntity> functionEntities;
    private List<Register> registers;
    private List<Reference> paraRegisterRef;
    private Set<Reference> callerSaveRegRef;
    private RegisterConfig regConfig;

    private Register rax, rbx, rcx, rdx, rsi, rdi, rsp, rbp;
    private Reference rrax, rrbx, rrcx, rrdx, rrsi, rrdi, rrsp, rrbp;
    private Reference rr10, rr11;
    private List<Register> colors = new LinkedList<>();
    private Scope globalScope;

    public Allocator (InstructionEmitter emitter, RegisterConfig regConfig) {
        functionEntities = emitter.functionEntities();
        this.regConfig = regConfig;
        globalScope = emitter.globalScope();

        // load registers
        rbp = regConfig.rbp();
        registers = regConfig.registers();
        rax = registers.get(0); rbx = registers.get(1);
        rcx = registers.get(2); rdx = registers.get(3);
        rsi = registers.get(4); rdi = registers.get(5);
        rbp = registers.get(6); rsp = registers.get(7);

        rrax = new Reference(rax);
        rr10 = new Reference(registers.get(10));
        rr11 = new Reference(registers.get(11));

        paraRegisterRef = new LinkedList<>();
        for (Register register : regConfig.paraRegister()) {
            paraRegisterRef.add(new Reference(register));
        }
        rrdi = paraRegisterRef.get(0); rrsi = paraRegisterRef.get(1);
        rrdx = paraRegisterRef.get(2); rrcx = paraRegisterRef.get(3);

        // set precolored
        precolored = new LinkedHashSet<>();
        precolored.addAll(paraRegisterRef);
        precolored.add(rrax);
        precolored.add(rr10);
        precolored.add(rr11);
        for (Reference ref : precolored) {
            ref.isPrecolored = true;
            ref.color = ref.reg();
        }

        // set caller save
        callerSaveRegRef = new HashSet<>();
        for (Reference ref : precolored) {
            if (!ref.reg().isCalleeSave()) {
                callerSaveRegRef.add(ref);
            }
        }

        // init colors
        colors.addAll(regConfig.paraRegister());
        colors.add(rax);
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
            if (functionEntity.isInlined())
                continue;
            init(functionEntity);
            loadPrecolord(functionEntity);
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
            if (iter != 1) // if someone is spilled
                functionEntity.regUsed().add(rbp);
            functionEntity.setLocalVariableOffset(localOffset);
        }
    }

    private void init(FunctionEntity entity) {
        // global
        edgeEdgeHashMap  = new HashMap<>();
        edgeSet          = new LinkedHashSet<>();
        simplifiedEdge   = new LinkedHashSet<>();

        // node set (disjoint)
        simplifyWorklist = new LinkedHashSet<>();
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

        initLivenessAnalysis(entity);
        // for spilled node
        localOffset = 0;
    }

    // global
    private Set<Edge> edgeSet;
    private Set<Edge> simplifiedEdge;
    private int K;
    private int localOffset;

    // node set (disjoint)
    private Set<Reference> precolored;
    private Set<Reference> initial;
    private Set<Reference> simplifyWorklist;
    private Set<Reference> freezeWorklist;
    private Set<Reference> spillWorklist;
    private Set<Reference> spilledNodes;
    private Set<Reference> coalescedNodes;
    private Set<Reference> coloredNodes;
    private Set<Reference> selectWorklist;
    private Stack<Reference> selectStack;

    // move set (disjoint)
    private Set<Move> coalescedMoves;
    private Set<Move> constrainedMoves;
    private Set<Move> frozenMoves;
    private Set<Move> worklistMoves;
    private Set<Move> activeMoves;

    private int iter;
    private void allocateFunction(FunctionEntity entity) {
        //err.println("allocate for " + entity.name());
        boolean finish;
        iter = 0;
        do {
            //err.println(" === iter " + iter + " ===");

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

    private List<BasicBlock> sorted;
    private Set<BasicBlock> visited;
    private void dfsSort(BasicBlock bb) {
        sorted.add(bb);
        visited.add(bb);
        for (BasicBlock pre : bb.predecessor()) {
            if (!visited.contains(pre)) {
                dfsSort(pre);
            }
        }
    }

    private void initLivenessAnalysis(FunctionEntity entity) {
        // sort blocks to boost iteration, iterate in reverse
        sorted = new LinkedList<>();
        visited = new HashSet<>();
        ListIterator li = entity.bbs().listIterator(entity.bbs().size());
        while (li.hasPrevious()) {
            BasicBlock pre = (BasicBlock) li.previous();
            if (!visited.contains(pre))
                dfsSort(pre);
        }
    }

    private void livenessAnalysis(FunctionEntity entity) {
        // print Def and Use
        if (Option.printGlobalAllocationInfo) {
            err.println("====== USE & DEF ======");
            for (BasicBlock basicBlock : entity.bbs()) {
                for (Instruction ins : basicBlock.ins()) {
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
            }
        }

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

    private void build(FunctionEntity entity) {
        // init edge and degree
        simplifiedEdge.clear();
        edgeSet.clear();
        initial.removeAll(precolored);
        for (Reference ref : initial) {
            ref.reset();
        }
        for (Reference ref : precolored) {
            ref.reset();
        }

        // build inference graph
        for (BasicBlock basicBlock : entity.bbs()) {
            HashSet<Reference> live = new HashSet<>(basicBlock.liveOut());

            // generate an iterator. Start just after the last element.
            ListIterator li = basicBlock.ins().listIterator(basicBlock.ins().size());
            while (li.hasPrevious()) {
                Set<Reference> tmp;
                Instruction ins = (Instruction) li.previous();

                for (Reference ref : live) {
                    ref.addRefTime();
                }

                tmp= new HashSet<>(); tmp.addAll(live); ins.setOut(tmp);

                if (ins instanceof Move && ((Move) ins).isRefMove()) {
                    live.removeAll(ins.use());
                    for (Reference ref : ins.def())
                        ref.moveList.add((Move)ins);
                    for (Reference ref : ins.use())
                        ref.moveList.add((Move)ins);
                    worklistMoves.add((Move)ins);
                }
                live.addAll(ins.def());
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

        if (Option.printGlobalAllocationInfo) {
            // print Liveness Info
            err.println("====== IN & OUT ======");
            for (BasicBlock basicBlock : entity.bbs()) {
                for (Instruction ins : basicBlock.ins()) {
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

        }

        if (Option.printGlobalAllocationInfo) {
           /* err.println("====== EDGE ======");
            for (Reference u : initial) {
                err.printf("%-10s:", u.name());
                for (Reference v : u.adjList) {
                    err.print( "  " + v.name());
                }
                err.println();
            }
            err.println();*/
        }

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

        move(ref, simplifyWorklist, selectWorklist);
        selectStack.push(ref);

        Set<Reference> backup = new HashSet<>(ref.adjList);

        for (Reference adj : backup) {
            simplifiedEdge.add(new Edge(adj, ref));
            simplifiedEdge.add(new Edge(ref, adj));
            deleteEdge(ref, adj);
        }
    }

    private void decreaseDegree(Reference ref) {
        int d = ref.degree--;
        if (d == K) {
            enableMoves(ref);
            ref.adjList.forEach(this::enableMoves);

            if (spillWorklist.contains(ref)) {
                if (isMoveRelated(ref)) {
                    move(ref, spillWorklist, freezeWorklist);
                } else {
                    move(ref, spillWorklist, simplifyWorklist);
                }
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

    private boolean OK(Reference u, Reference v) {
        for (Reference t : v.adjList) {
            if (!(t.degree < K || precolored.contains(t) || edgeSet.contains(getEdge(t, u))))
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

    private void addWorkList(Reference ref) {
        if (!precolored.contains(ref) && !isMoveRelated(ref) && ref.degree < K) {
            move(ref, freezeWorklist, simplifyWorklist);
        }
    }

    private void combine(Reference u, Reference v) {
        if (freezeWorklist.contains(v)) {
            move(v, freezeWorklist, coalescedNodes);
        } else {
            move(v, spillWorklist, coalescedNodes);
        }

        v.alias = u;
        u.moveList.addAll(v.moveList);
        enableMoves(v);

        Set<Reference> backup = new HashSet<>(v.adjList);

        for (Reference t : backup) {
            deleteEdge(t, v);
            addEdge(u, t);
            if (t.degree >= K && freezeWorklist.contains(t)) {
                move(t, freezeWorklist, spillWorklist);
            }
        }
        if (u.degree >= K && freezeWorklist.contains(u)) {
            move(u, freezeWorklist, spillWorklist);
        }
    }

    private void coalesce() {
        Move move = worklistMoves.iterator().next();
        Reference x = getAlias((Reference) move.src());
        Reference y = getAlias((Reference) move.dest());
        Reference u, v;

        if (precolored.contains(y)) {
            u = y; v = x;
        } else {
            u = x; v = y;
        }

        worklistMoves.remove(move);
        if (u == v) {
            coalescedMoves.add(move);
            addWorkList(u);
        } else if (precolored.contains(v) || edgeSet.contains(getEdge(u, v))) {
            constrainedMoves.add(move);
            addWorkList(u);
            addWorkList(v);
        } else if (precolored.contains(u) && OK(u, v) ||
                !precolored.contains(u) && conservative(u, v)) {
            coalescedMoves.add(move);
            combine(u,v);
            addWorkList(u);
        } else {
            activeMoves.add(move);
        }
    }

    private void freeze() {
        Reference u = freezeWorklist.iterator().next();
        move(u, freezeWorklist, simplifyWorklist);
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
                    move(v, freezeWorklist, simplifyWorklist);
                }
            }
        }
    }

    private Set<String> protect = new HashSet<>();

    private void selectSpill() {
        Iterator<Reference> iter = spillWorklist.iterator();
        Reference toSpill = iter.next();

        // SPILL HEURISTIC HERE
        protect.add("i"); protect.add("j"); protect.add("tmp2"); protect.add("g_chunks");
        while ((protect.contains(toSpill.name()) || toSpill.name().contains("spill")) && iter.hasNext()) {
            toSpill = iter.next();
        }

        move(toSpill, spillWorklist, simplifyWorklist);
        freezeMoves(toSpill);
    }

    private void move(Reference ref, Set<Reference> from, Set<Reference> to) {
        if (from.contains(ref)) {
            if (!to.contains(ref)) {
                from.remove(ref);
                to.add(ref);
            } else {
                throw new InternalError("already exist move " + ref.name() + " from " + from + " to " + to);
            }
        } else {
            throw new InternalError("empty move " + ref.name() + " from " + from + " to " + to);
        }
    }

    private void assignColors(FunctionEntity entity) {
        // restore simplified edges
        for (Edge edge : simplifiedEdge) {
            addEdge(getAlias(edge.u), getAlias(edge.v));
        }

        // begin assign
        LinkedList<Register> okColors = new LinkedList<>();
        while(!selectStack.empty()) {
            Reference n = selectStack.pop();
            okColors.clear();
            for (Register color : colors) {
                okColors.add(color);
            }

            for (Reference w : n.adjList) {
                w = getAlias(w);
                if (coloredNodes.contains(w) || precolored.contains(w)) {
                    okColors.remove(w.color);
                }
            }

            if (okColors.isEmpty()) {
                //err.println("spill " + n.name());
                move(n, selectWorklist, spilledNodes);
                n.color = null;
            } else {
                Register color = okColors.iterator().next();
                //err.println("assign " + n.name() + " -> " + color.name());
                move(n, selectWorklist, coloredNodes);
                n.color = color;
            }
        }

        for (Reference node : coalescedNodes) {
            node.color = getAlias(node).color;
        }

        if (Option.printGlobalAllocationInfo) {
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
    }

    private int spilledCounter = 0;
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
                                Reference tmp = new Reference("spill_" + use.name() + "_" + spilledCounter++, Reference.Type.UNKNOWN);
                                newTemp.add(tmp);
                                newIns.add(new Move(tmp, new Address(rbp, null, 1, use.offset())));
                                ins.replaceAll(use, tmp);
                                stores.add(new Move(new Address(rbp, null, 1, use.offset()), tmp));
                            } else {
                                if (ins instanceof Move && !(((Move) ins).dest()).isAddress() && ((Move) ins).src() == use) {
                                    // optimization for move
                                    ins = new Move(((Move) ins).dest(), new Address(rbp, null, 1, use.offset()));
                                } else {
                                    Reference tmp = new Reference("spill_" + use.name() + "_" + spilledCounter++, Reference.Type.UNKNOWN);
                                    newTemp.add(tmp);
                                    newIns.add(new Move(tmp, new Address(rbp, null, 1, use.offset())));
                                    ins.replaceUse(use, tmp);
                               }
                            }
                        }
                    }

                    for (Reference def : insDef) {
                        if (def.isSpilled) {
                            if (insUse.contains(def)) {
                                ; //already done in previous step
                            } else {
                                if (ins instanceof Move && !(((Move) ins).src()).isAddress() && ((Move) ins).dest() == def) {
                                    // optimization for move
                                    ins = new Move(new Address(rbp, null, 1, def.offset()), ((Move) ins).src());
                                } else {
                                    Reference tmp = new Reference("spill_" + def.name() + "_" + spilledCounter++, Reference.Type.UNKNOWN);
                                    newTemp.add(tmp);
                                    ins.replaceDef(def, tmp);
                                    stores.add(new Move(new Address(rbp, null, 1, def.offset()), tmp));
                                }
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
                    ; // ignore redundant move
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

        if (Option.printGlobalAllocationInfo) {
            err.println("===== REWRITE =====");
            for (BasicBlock basicBlock : entity.bbs()) {
                for (Instruction ins : basicBlock.ins()) {
                    err.println(ins);
                }
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

    // rewrite instructions to satisfy the machine-relevant requirement
    private void loadPrecolord(FunctionEntity entity) {
        for (BasicBlock basicBlock : entity.bbs()) {
            List<Instruction> newIns = new LinkedList<>();
            for (Instruction raw : basicBlock.ins()) {
                if (raw instanceof Call) {
                    Set<Reference> paraRegUsed = new HashSet<>();
                    Call ins = (Call) raw;
                    int i = 0, pushCt = 0;
                    for (Operand operand : ins.operands()) {
                        if (i < paraRegisterRef.size()) {
                            paraRegUsed.add(paraRegisterRef.get(i));
                            newIns.add(new Move(paraRegisterRef.get(i), operand));
                        } else {
                            if (operand instanceof Immediate) {
                                Reference tmp = new Reference("tmp_push", Reference.Type.UNKNOWN);
                                newIns.add(new Move(tmp, operand));
                                operand = tmp;
                            }
                            newIns.add(new Push(operand));
                            pushCt++;
                        }
                        i++;
                    }
                    Call newCall = new Call(ins.entity(), new LinkedList<>());
                    newCall.setCallorsave(callerSaveRegRef);
                    newCall.setUsedParameterRegister(paraRegUsed);
                    newIns.add(newCall);
                    if (pushCt > 0) {
                        newIns.add(new Add(rsp, new Immediate(pushCt * Option.REG_SIZE)));
                    }
                    if (ins.ret() != null) {
                        newIns.add(new Move(ins.ret(), rrax));
                    }
                } else if (raw instanceof Div || raw instanceof Mod) {
                    newIns.add(new Move(rrax, ((Bin)raw).left()));
                    newIns.add(new Move(rrdx, rdx)); // cqo

                    Operand right = ((Bin)raw).right();
                    if (right instanceof Immediate) { // right cannot be immediate
                        newIns.add(new Move(rrcx, right));
                        right = rrcx;
                    }
                    if (raw instanceof Div) {
                        newIns.add(new Div(rrax, right));
                        newIns.add(new Move(rrax, rax)); // refresh
                        newIns.add(new Move(rrdx, rdx));
                        newIns.add(new Move(((Div) raw).left(), rrax));
                    } else {
                        newIns.add(new Mod(rrax, right));
                        newIns.add(new Move(rrax, rax)); // refresh
                        newIns.add(new Move(rrdx, rdx));
                        newIns.add(new Move(((Bin) raw).left(), rrdx));
                    }
                } else if (raw instanceof Return) {
                    if (((Return) raw).ret() != null)
                        newIns.add(new Move(rrax, ((Return) raw).ret()));
                    newIns.add(new Return(null));
                } else if (raw instanceof Label) {
                    if (raw == entity.beginLabelINS()) {
                        int i = 0;
                        for (ParameterEntity par : entity.params()) {   // load parameters
                            if (i < paraRegisterRef.size()) {
                                newIns.add(new Move(par.reference(), paraRegisterRef.get(i)));
                            } else {
                                newIns.add(new Move(par.reference(), par.source()));
                            }
                            i++;
                        }
                    }
                    newIns.add(raw);
                } else if (raw instanceof Sal || raw instanceof Sar) {
                    if (((Bin)raw).right() instanceof Immediate) {
                        newIns.add(raw);
                    } else {
                        newIns.add(new Move(rrcx, ((Bin)raw).right()));
                        if (raw instanceof Sal)
                            newIns.add(new Sal(((Bin)raw).left(), rrcx));
                        else
                            newIns.add(new Sar(((Bin)raw).left(), rrcx));
                    }
                } else if (raw instanceof Cmp) {
                    Operand left = ((Cmp) raw).left();
                    Operand right = ((Cmp) raw).right();
                    transCompare(newIns, raw, left, right);
                } else if (raw instanceof CJump && ((CJump) raw).type() != CJump.Type.BOOL) {
                    Operand left = ((CJump) raw).left();
                    Operand right = ((CJump) raw).right();
                    transCompare(newIns, raw, left, right);
                } else {
                    newIns.add(raw);
                }
            }
            basicBlock.setIns(newIns);
        }
    }

    private void transCompare(List<Instruction> newIns, Instruction raw, Operand left, Operand right) {
        if (left.isAddress() && right.isAddress()) {
            Reference tmp = new Reference("tmp_cmp", Reference.Type.UNKNOWN);
            newIns.add(new Move(tmp, left));
            if (raw instanceof Cmp) {
                ((Cmp) raw).setLeft(tmp);
                newIns.add(raw);
                newIns.add(new Move(left, tmp));
            } else {
                ((CJump)raw).setLeft(tmp);
                newIns.add(raw);
            }
        } else {
            newIns.add(raw);
        }
    }

    /*
     * utility
     */
    private class Edge {
        Reference u, v;

        Edge(Reference u, Reference v) {
            this.u = u;
            this.v = v;
        }

        @Override
        public String toString() {
            return "(" + u + "," + v + ")";
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

    private void deleteEdge(Reference u, Reference v) {
        Edge edge = getEdge(u, v);
        Edge edge2 = getEdge(v, u);

        if (!edgeSet.contains(edge) || !edgeSet.contains(edge2)) {
            throw new InternalError("delete edge error");
        }

        edgeSet.remove(edge);
        edgeSet.remove(edge2);

        if (!edgeEdgeHashMap.containsKey(edge) || !edgeEdgeHashMap.containsKey(edge2)) {
            edgeEdgeHashMap.remove(edge);
            edgeEdgeHashMap.remove(edge2);
        }

        u.adjList.remove(v);
        v.adjList.remove(u);
        decreaseDegree(u);
        decreaseDegree(v);
    }

    private HashMap<Edge, Edge> edgeEdgeHashMap;
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
