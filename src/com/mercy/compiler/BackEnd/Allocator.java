package com.mercy.compiler.BackEnd;

import com.mercy.compiler.Entity.FunctionEntity;
import com.mercy.compiler.INS.Instruction;
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
            livenessAnalysis(functionEntity);
            build();
            makeWorklist();
        }
    }

    List<Instruction> sorted;
    Set<Instruction> visited = new HashSet<>();
    private void dfsSort(Instruction ins) {
        sorted.add(ins);
        visited.add(ins);
        for (Instruction pre : ins.predessor()) {
            if (!visited.contains(pre)) {
                dfsSort(pre);
            }
        }
    }

    private void livenessAnalysis(FunctionEntity entity) {
        sorted = new LinkedList<>();

        // generate an iterator. Start just after the last element.
        ListIterator li = entity.ins().listIterator(entity.ins().size());

        // iterate in reverse.
        while (li.hasPrevious()) {
            Instruction pre = (Instruction) li.previous();
            if (!visited.contains(pre))
                dfsSort(pre);
        }

        for (Instruction ins : sorted) {
            ins.calcDefAndUse();
        }

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

        // solve dataflow equation
        boolean modified = true;
        while (modified) {
            modified = false;
            for (Instruction ins : sorted) {
                Set<Reference> newIn = new HashSet<>();
                Set<Reference> right = new HashSet<>(ins.out());
                right.removeAll(ins.def());
                newIn.addAll(ins.use());
                newIn.addAll(right);

                Set<Reference> newOut = new HashSet<>();
                for (Instruction suc : ins.sucessor()) {
                    newOut.addAll(suc.in());
                }

                modified |= !ins.in().equals(newIn) || !ins.out().equals(newOut);

                ins.setIn(newIn);
                ins.setOut(newOut);
            }
        }

        // print Liveness Info
        for (Instruction ins : entity.ins()) {
            err.printf("%-20s in:", ins.toString());
            err.print(ins.in().size());
            /*for (Reference reference : ins.in()) {
                err.print("  " + reference);
            }*/
            err.print("  out: ");
            err.print(ins.out().size());
            /*for (Reference reference : ins.out()) {
                err.print("  " + reference);
            }*/
            err.println();
        }
        err.println();
    }



    private void build() {

    }

    private void makeWorklist() {

    }

}
