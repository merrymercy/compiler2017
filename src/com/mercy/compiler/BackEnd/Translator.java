package com.mercy.compiler.BackEnd;

import com.mercy.compiler.Entity.*;
import com.mercy.compiler.INS.*;
import com.mercy.compiler.INS.Operand.*;
import com.mercy.compiler.IR.IR;
import com.mercy.compiler.Utility.InternalError;
import com.mercy.compiler.Utility.Pair;
import com.sun.org.apache.regexp.internal.RE;

import java.io.*;
import java.sql.Ref;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by mercy on 17-5-4.
 */
public class Translator {
    public final int VIRTUAL_STACK_REG_SIZE = 8;
    public final int ALIGNMENT = 4;
    public final String END_SUFFIX = "_ret";

    private List<FunctionEntity> functionEntities;
    private Scope globalScope;
    private List<IR> globalInitializer;

    private FunctionEntity currentFunction;

    private Register rax, rcx, rdx, rbx, rsp, rbp, rsi, rdi;
    private List<Register> registers = new ArrayList<>();
    private List<Register> paraRegister = new ArrayList<>();
    private boolean [] regUsed = new boolean[16];
    private boolean [] isCalleeSave = new boolean[16];
    private List<String> asm = new LinkedList<>();

    public Translator(InstructionEmitter emitter) {
        functionEntities = emitter.functionEntities();
        globalScope = emitter.globalScope();
        globalInitializer = emitter.globalInitializer();

        // init registers
        rax = new Register("rax"); registers.add(rax);
        rcx = new Register("rcx"); registers.add(rcx);
        rdx = new Register("rdx"); registers.add(rdx);
        rbx = new Register("rbx"); registers.add(rbx);
        rsp = new Register("rsp"); registers.add(rsp);
        rbp = new Register("rbp"); registers.add(rbp);
        rsi = new Register("rsi"); registers.add(rsi);
        rdi = new Register("rdi"); registers.add(rdi);

        for (int i = 8; i < 16; i++) {
            registers.add(new Register("r" + i));
        }

        // set registers for parameter
        paraRegister.add(rdi); paraRegister.add(rsi);
        paraRegister.add(rdx); paraRegister.add(rcx);
        paraRegister.add(registers.get(8));
        paraRegister.add(registers.get(9));

        // set callee save
        for (int i = 12; i < 16; i++)
            isCalleeSave[i] = true;
        isCalleeSave[3] = isCalleeSave[5] = true;
    }

    public List<String> translate() {
        // set reference for global variable
        int stringCounter = 1;

        // add extern
        add("global main");
        add("extern printf");
        add("extern scanf");
        add("extern puts");
        add("extern gets");
        add("extern sprintf");
        add("extern sscanf");
        add("extern strlen");
        add("extern strcpy");
        add("extern strncpy");
        add("extern malloc");
        add("");

        // add data section
        add("section .data");
        for (Entity entity : globalScope.entities().values()) {
            //System.out.println(entity.name());
            if (entity instanceof VariableEntity) {
                entity.setReference(new Reference(entity.name()));
                addLabel(entity.name());
                add("dq 0");
            } else if (entity instanceof  StringConstantEntity) {
                String name = "__STR_CONST_" + stringCounter++;
                String value = ((StringConstantEntity) entity).strValue();
                entity.setReference(new Reference(name, true));

                add("dd " + value.length());
                addLabel(name);

                StringBuffer sb = new StringBuffer();
                sb.append("db");
                for (int i = 0; i < value.length(); i++) {
                    sb.append(" ");
                    Integer x = new Integer(value.charAt(i));
                    sb.append(x);
                    sb.append(",");
                }
                sb.append(" 0");
                add(sb.toString());
            }
        }
        add("");

        // translate functions
        add("section .text");
        for (FunctionEntity entity : functionEntities) {
            // init
            for (int i = 0; i < regUsed.length; i++)
                regUsed[i] = false;
            currentFunction = entity;
            int frameSize = locateFrame(entity);
            translateFunction(entity, frameSize);
            add("");
        }

        pasteLibfunction();
        return asm;
    }

    // saved reg
    // virtual stack
    // local variable
    // parameter
    // ----------------- <- bp
    // old bp
    // return address

    public int locateFrame(FunctionEntity entity) {
        int savedRegBase, paraBase, lvarBase, stackBase, total;

        paraBase = 0;

        // locate parameter
        lvarBase = paraBase;
        for (ParameterEntity parameterEntity : entity.params()) {
            lvarBase += parameterEntity.type().size();
            parameterEntity.setOffset(lvarBase);
            parameterEntity.setReference(new Reference(lvarBase, rbp()));
        }

        // locate local variable
        stackBase = lvarBase;
        stackBase += entity.scope().locateLocalVariable(lvarBase, ALIGNMENT);
        for (VariableEntity variableEntity : entity.scope().allLocalVariables()) {
            variableEntity.setReference(new Reference(variableEntity.offset(),
                    rbp()));
        }

        int[] toAllocate = {12, 13, 14, 15, 3};//, 10, 11};

        // locate tmpStack
        savedRegBase = stackBase;
        List<Reference> tmpStack = entity.tmpStack();
        for (int i = 0; i < tmpStack.size(); i++) {
            if (i < toAllocate.length) {
                tmpStack.get(i).setRegister(registers.get(toAllocate[i]));
                regUsed[toAllocate[i]] = true;
            } else {
                savedRegBase += VIRTUAL_STACK_REG_SIZE;
                tmpStack.get(i).setOffset(savedRegBase, rbp());
            }
        }

        // locate callee-save register
        total = savedRegBase;
        return total;
    }

    public void translateFunction(FunctionEntity entity, int frameSize) {
        addLabel(entity.name());

        int startPos = asm.size();

        /***** body *****/
        for (Instruction instruction : entity.INS()) {
            instruction.accept(this);
        }

        /***** prologue *****/
        List<String> backup = asm, prologue;
        asm = new LinkedList<>();
        // push and pop callee-save registers
        int calleeSaveRegSize = 0;
        for (int i = 0; i < regUsed.length; i++) {
            if (regUsed[i] && isCalleeSave[i]) {
                add("push", registers.get(i));
                calleeSaveRegSize += VIRTUAL_STACK_REG_SIZE;
            }
        }
        // set rbp and rsp
        if (regUsed[5])
            add("mov", rbp(), rsp());
        frameSize += (16 - (frameSize + 8 + calleeSaveRegSize) % 16) % 16;
        add("sub", rsp(), new Immediate((frameSize)));
        // store parameters
        List<ParameterEntity> params = entity.params();
        for (int i = 0; i < params.size(); i++) {
            if (i < paraRegister.size()) {
                add("mov", params.get(i).reference(), paraRegister.get(i));
            } else {
                add("pop", rax());
                add("mov", params.get(i).reference(), rax());
            }
        }
        add("");

        // insert prologue
        prologue = asm;
        asm = backup;
        asm.addAll(startPos, prologue);

        /***** epilogue *****/
        addLabel(entity.name() + END_SUFFIX);
        add("add", rsp(), new Immediate((frameSize)));
        for (int i = regUsed.length - 1; i >= 0; i--) {
            if (regUsed[i] && isCalleeSave[i]) {
                add("pop", registers.get(i));
            }
        }
        add("ret");
    }

    private void add(String op, Operand l, Operand r) {
        asm.add("\t" + op + " " + l.toNASM() + ", " + r.toNASM());
    }
    private void add(String op, Operand l) {
        asm.add("\t" + op + " " + l.toNASM());
    }
    private void add(String op) {
        asm.add("\t" + op);
    }
    private void addLabel(String name) {
        asm.add(name + ":");
    }
    private void addJump(String name) {
        asm.add("\tjmp" + " " + name);
    }
    private void addComment(String comment) {
        asm.add("\t;" + comment);
    }

    /*
     * INS visitor
     */
    public void visitBin(Bin ins) {
        Operand left, right;
        String name;
        left = ins.left();
        right= ins.right();
        name = ins.name();
        if (left.isRegister()) {
            if (right.isRegister()) {  // add regl, regr
                add(name, left, right);
            } else {                   // add regl, memr
                add(name, left, right);
            }
        } else {
            if (right.isRegister()) {   // add meml, regr
                add("mov", rax(), left);
                add(name, rax(), right);
                add("mov", left, rax());
            } else {                    // add meml, memr
                add("mov", rax(), left);
                add(name, rax(), right);
                add("mov", left, rax());
            }
        }
    }

    public void visit(Add ins) {
        visitBin(ins);
    }
    public void visit(Sub ins) {
        visitBin(ins);
    }
    public void visit(Mul ins) {
        visitBin(ins);
    }

    public void visit(Div ins) {
        Operand left, right;
        left = ins.left();
        right= ins.right();
        add("mov", rax(), left);
        add("cqo");
        add("idiv", right);
        add("mov", left, rax());
    }

    public void visit(Mod ins) {
        Operand left, right;
        left = ins.left();
        right= ins.right();
        add("mov", rax(), left);
        add("cqo");
        add("idiv", right);
        add("mov", left, rdx());
    }

    public void visit(Neg ins) {
        add("neg", ins.operand());
    }

    public void visit(Not ins) {
        add("not", ins.operand());
    }

    public void visit(And ins) {
        visitBin(ins);
    }
    public void visit(Or ins) {
        visitBin(ins);
    }
    public void visit(Sal ins) {
        visitBin(ins);
    }
    public void visit(Sar ins) {
        visitBin(ins);
    }
    public void visit(Xor ins) {
        visitBin(ins);
    }

    public void visit(Cmp ins) {
        Operand left, right;
        left = ins.left();
        right= ins.right();
        add("cmp", left, right);
        String set = "";
        switch (ins.operator()) {
            case EQ: set = "sete";  break;
            case NE: set = "setne"; break;
            case GE: set = "setge"; break;
            case GT: set = "setg";  break;
            case LE: set = "setle"; break;
            case LT: set = "setl";  break;
        }
        add(set + " al");
        add("movzx " +  "rax" + ", " + "al");
        add("mov", left, rax());
    }

    private Pair<Operand, Boolean> dealOneSize(Register tmp, Operand operand) {
        if (operand instanceof Address) {
            Address addr = (Address)operand;
            Reference ref;

            switch (addr.type()) {
                case OPERAND:
                    ref = (Reference) addr.operand();
                    if (ref.isRegister()) {
                        return new Pair<>(operand, true);
                    } else {
                        add("mov", tmp, ref);
                        return new Pair<>(new Address(tmp), true);
                    }
                case ENTITY:
                    return new Pair<>(operand, true);
                default:
                    throw new InternalError("invalid addr type " + addr.type());
            }
        } else if (operand instanceof Reference) {
            Reference ref = (Reference) operand;
            return new Pair<>(operand, !ref.isRegister());
        } else {
            return new Pair<>(operand, false);
        }
    }

    public void visit(Move ins) {
        Pair<Operand, Boolean>  dest = dealOneSize(rax(), ins.dest());
        Pair<Operand, Boolean>  src  = dealOneSize(rdx(), ins.src());
        if (dest.second && src.second) {
            add("mov", rcx(), src.first);
            add("mov", dest.first, rcx());
        } else {
            add("mov", dest.first, src.first);
        }
        // ATTENTION: HERE , double memory
    }

    public void visit(Call ins) {
        List<Operand> operands = ins.operands();
        for (int i = 0; i < operands.size(); i++) {
            if (i < paraRegister.size()) {
                add("mov", paraRegister.get(i), operands.get(i));
            } else {
                if (operands.get(i).isRegister()) {
                    add("push", operands.get(i));
                } else {
                    add("mov", rax(), operands.get(i));
                    add("push", rax());
                }
            }
        }
        if (ins.entity().asmName().equals("printf"))
            add("xor", rax(), rax());
        add("call " + ins.entity().asmName());
        if (ins.ret() != null)
            add("mov", ins.ret(), rax());
    }

    public void visit(Return ins) {
        if (ins.ret() != null)
            add("mov", rax(), ins.ret());
        addJump(currentFunction.name() + END_SUFFIX);
    }

    public void visit(CJump ins) {
        if (ins.cond().isRegister()) {
            add("test", ins.cond(), ins.cond());
        } else {
            add("mov", rax(), ins.cond());
            add("test", rax(), rax());
        }
        add("jz " + ins.falseLabel().name());
        addJump(ins.trueLabel().name());
    }

    public void visit(Jmp ins) {
        addJump(ins.dest().name());
    }

    public void visit(Label ins) {
        addLabel(ins.name());
    }

    public void visit(Comment ins) {
        add(";" + ins);
    }

    /*
     * register getter
     */
    private Register rax() {
        regUsed[0] = true; return rax;
    }
    private Register rcx() {
        regUsed[1] = true; return rcx;
    }
    private Register rdx() {
        regUsed[2] = true; return rdx;
    }
    private Register rbx() {
        regUsed[3] = true; return rbx;
    }
    private Register rsp() {
        regUsed[4] = true; return rsp;
    }
    private Register rbp() {
        regUsed[5] = true; return rbp;
    }
    private Register rsi() {
        regUsed[6] = true; return rsi;
    }
    private Register rdi() {
        regUsed[7] = true; return rdi;
    }
    private Register R(int i) {
        regUsed[i] = true; return registers.get(i);
    }

    /********** DEBUG TOOL **********/
    private void printFunction(PrintStream out, FunctionEntity entity) {
        out.println("========== OFFSET ==========");
        for (ParameterEntity parameterEntity : entity.params()) {
            out.println(parameterEntity.name() + " " + parameterEntity.offset());
        }

        for (VariableEntity variableEntity : entity.allLocalVariables()) {
            out.println(variableEntity.name() + " " + variableEntity.offset());
        }
    }

    public void printSelf(PrintStream out) {
        for (FunctionEntity functionEntity : functionEntities) {
            printFunction(out, functionEntity);
        }
        /*out.println("========== NASM ==========");
        for (String s : asm) {
            out.println(s);
        }*/
    }

    private void pasteLibfunction() {
        asm.add("\n;========== LIB BEGIN ==========");
        File f = new File("lib.s");
        try {
            //FileWriter fout = new FileWriter(f);
            //fout.write("aha");
            //fout.close();
            BufferedReader fin = new BufferedReader(new FileReader(f));
            String line;
            while((line = fin.readLine()) != null)
                asm.add(line);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

