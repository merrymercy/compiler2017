package com.mercy.compiler.BackEnd;

import com.mercy.Option;
import com.mercy.compiler.Entity.*;
import com.mercy.compiler.INS.*;
import com.mercy.compiler.INS.Operand.*;
import com.mercy.compiler.IR.IR;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import static com.mercy.Option.FRAME_ALIGNMENT_SIZE;
import static com.mercy.Option.REG_SIZE;

/**
 * Created by mercy on 17-5-4.
 */
public class Translator {
    public static int VIRTUAL_STACK_REG_SIZE = 8;
    public static int ALIGNMENT = 4;
    public static String END_SUFFIX = "_ret";
    public static String GLOBAL_PREFIX = "__global_";
    public static String FUNC_SUFFIX = "__func";


    private List<FunctionEntity> functionEntities;
    private Scope globalScope;
    private List<IR> globalInitializer;

    private FunctionEntity currentFunction;

    private Register rax, rbx, rcx, rdx, rsi, rdi, rsp, rbp;
    private List<Register> registers;
    private List<Register> paraRegister;
    private List<String> asm = new LinkedList<>();

    public Translator(InstructionEmitter emitter, RegisterConfig registerConfig) {
        functionEntities = emitter.functionEntities();
        globalScope = emitter.globalScope();
        globalInitializer = emitter.globalInitializer();

        // load registers
        registers = registerConfig.registers();
        paraRegister = registerConfig.paraRegister();

        rax = registers.get(0); rbx = registers.get(1);
        rcx = registers.get(2); rdx = registers.get(3);
        rsi = registers.get(4); rdi = registers.get(5);
        rbp = registers.get(6); rsp = registers.get(7);
    }

    public List<String> translate() {
        // add extern
        add("global main");
        add("extern printf, scanf, puts, gets, sprintf, sscanf, getchar, strlen, strcmp, strcpy, strncpy, malloc");
        add("");

        // add data section
        add("section .data");
        for (Entity entity : globalScope.entities().values()) {
            if (entity instanceof VariableEntity) {
                addLabel(GLOBAL_PREFIX + entity.name());
                add("dq 0");
            } else if (entity instanceof  StringConstantEntity) {
                String name = ((StringConstantEntity) entity).asmName();
                String value = ((StringConstantEntity) entity).strValue();

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
            if (Option.enableInlineFunction && entity.canbeInlined())
                continue;
            currentFunction = entity;
            locateFrame(entity);
            translateFunction(entity);
            add("");
        }

        pasteLibfunction();
        return asm;
    }

    // virtual stack
    // local variable
    // parameter
    // ----------------- <- bp
    // saved regs
    // return address

    public void locateFrame(FunctionEntity entity) {
        // calc number of callee-save register
        int savedRegNum = 0;
        for (Register register : entity.regUsed()) {
            if (register.calleeSave())
                savedRegNum++;
        }

        int savedRegBase, paraBase, lvarBase, stackBase, total;
        paraBase = 0;

        // locate and set source for para
        lvarBase = paraBase;
        int sourceBase = savedRegNum * REG_SIZE + REG_SIZE;  // + return address
        List<ParameterEntity> params = entity.params();
        for (int i = 0; i < params.size(); i++) {
            ParameterEntity par = params.get(i);
            if (i < paraRegister.size()) {
                par.setSource(new Reference(paraRegister.get(i)));
                if (par.reference().isUnknown()) {
                    lvarBase += par.type().size();
                    par.reference().setOffset(lvarBase, rbp());
                }
            } else {
                par.setSource(new Reference(-sourceBase, rbp()));
                sourceBase += par.type().size();
                if (par.reference().isUnknown()) { // refer to source, to save frame size
                    par.reference().setOffset(par.source().offset(), par.source().reg());
                }
            }
        }

        // locate local variable
        stackBase = lvarBase;
        stackBase += entity.scope().locateLocalVariable(lvarBase, ALIGNMENT);
        for (VariableEntity var : entity.scope().allLocalVariables()) {
            if (var.reference().isUnknown()) {
                var.reference().setOffset(var.offset(), rbp());
            }
        }

        // locate tmpStack
        List<Reference> tmpStack = entity.tmpStack();
        savedRegBase = stackBase;
        for (int i = 0; i < tmpStack.size(); i++) {
            if (tmpStack.get(i).isUnknown()) {
                savedRegBase += REG_SIZE;
                tmpStack.get(i).setOffset(savedRegBase, rbp());
            }
        }

        total = savedRegBase;
        total += (FRAME_ALIGNMENT_SIZE - (total + REG_SIZE  + REG_SIZE * savedRegNum) % FRAME_ALIGNMENT_SIZE)
                 % FRAME_ALIGNMENT_SIZE;
        entity.setFrameSize(total);
    }

    public void translateFunction(FunctionEntity entity) {
        addLabel(entity.asmName());

        int startPos = asm.size();

        /***** body *****/
        for (Instruction instruction : entity.INS()) {
            instruction.accept(this);
        }

        /***** prologue *****/
        List<String> backup = asm, prologue;
        asm = new LinkedList<>();

        // push and pop callee-save registers
        for (Register register : entity.regUsed()) {
            if (register.calleeSave()) {
                add("push", register);
            }
        }
        // set rbp and rsp
        if (entity.regUsed().contains(rbp))
            add("mov", rbp(), rsp());
        if (entity.calls().size() != 0)   // leaf function optimization
            add("sub", rsp(), new Immediate(entity.frameSize()));

        // store parameters
        List<ParameterEntity> params = entity.params();
        for (ParameterEntity param : params) {
            if (!param.reference().equals(param.source())) {  // copy when source and ref are different
                add("mov", param.reference(), param.source());
            }
        }
        add("");

        // insert prologue
        prologue = asm;
        asm = backup;
        asm.addAll(startPos, prologue);

        /***** epilogue *****/
        // restore rsp
        addLabel(entity.endLabelINS().name());
        if (entity.calls().size() != 0)   // leaf function optimization
            add("add", rsp(), new Immediate((entity.frameSize())));

        // pop callee-save regs
        ListIterator iter = entity.regUsed().listIterator(entity.regUsed().size());
        while (iter.hasPrevious()) {
            Register reg = (Register) iter.previous();
            if (reg.calleeSave())
               add("pop", reg);
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
    private int addMove(Register reg, Operand operand) {
        if (operand instanceof Address) {
            if (((Address) operand).base().isRegister()) {
                add("mov", reg, operand);
            } else {
                add("mov", reg, ((Address) operand).base());
                add("mov", reg, new Address(reg));
            }
        } else {
            add("mov", reg, operand);
        }
        return 0;
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
            } else {
                if (right instanceof Address) {    // add regl, [memr]
                    add("mov", rax(), ((Address) right).base());
                    add(name, left, new Address(rax()));
                } else {                           // add regl, memr
                    add(name, left, right);
                }
            }
        } else {
            if (right.isRegister()) {   // add meml, regr
                add("mov", rax(), left);
                add(name, rax(), right);
                add("mov", left, rax());
            } else {                    // add meml, memr
                if (right instanceof Address) {   // add meml, [memr]
                    add("mov", rax(), left);
                    addMove(rdx(), right);
                    add(name, rax(), rdx());
                    add("mov", left, rax());
                } else {
                    add("mov", rax(), left);   // add meml, memr
                    add(name, rax(), right);
                    add("mov", left, rax());
                }
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

    private void genDivision(Operand left, Operand right, Register res) {
        addMove(rax(), left);
        add("cqo");
        if (right instanceof Immediate) {
            addMove(rcx(), right);
            add("idiv", rcx());
        } else {
            if (right instanceof Address) {
                ((Address) right).setShowSize(true);
                if (!((Address) right).base().isRegister()) {
                    addMove(rcx(), right);
                    add("idiv", rcx());
                } else {
                    add("idiv", right);
                }
            } else {
                add("idiv", right);
            }
        }
        add("mov", left, res);
    }

    public void visit(Div ins) {
        genDivision(ins.left(), ins.right(), rax());
    }

    public void visit(Mod ins) {
        genDivision(ins.left(), ins.right(), rdx());
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
        Operand left, right;
        left = ins.left();
        right= ins.right();
        addMove(rcx(), right);
        add("sal" + " " + left.toNASM() + ", " + "cl");
    }
    public void visit(Sar ins) {
        Operand left, right;
        left = ins.left();
        right= ins.right();
        addMove(rcx(), right);
        add("sar" + " " + left.toNASM() + ", " + "cl");
    }
    public void visit(Xor ins) {
        visitBin(ins);
    }

    public void visit(Cmp ins) {
        Operand left, right;
        left = ins.left();
        right= ins.right();
        if (left.isRegister() || right.isRegister()) {
            if (right instanceof Address) {
                if (!((Address) right).base().isRegister()) {
                    addMove(rax(), right);
                    add("cmp", left, rax());
                } else {
                    add("cmp", left, right);
                }
            } else
                add("cmp", left, right);
        } else {                                        // cmp mem, mem
            addMove(rdx(), left);
            if (right instanceof Address) {
                if (!((Address) right).base().isRegister()) {
                    addMove(rax(), right);
                    add("cmp", rdx(), rax());
                } else {
                    add("cmp", rdx(), right);
                }
            } else
                add("cmp", rdx(), right);
        }

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

    private int mem2reg(Address addr, Register reg1, Register reg2) {
        if (addr.index() == null) {
            if (addr.base().isRegister()) {  // [reg]
                return 0;
            } else {                         // [mem]
                addMove(reg1, addr.base());
                addr.setBaseNasm(reg1);
                return 1;
            }
        } else {
            if (addr.base().isRegister()) {
                if (addr.index().isRegister()) {   // [reg + reg * 2]
                    return 0;
                } else {
                    addMove(reg1, addr.index());   // [reg + mem * 2]
                    addr.setIndexNasm(reg1);
                    return 1;
                }
            } else {
                addMove(reg1, addr.base());
                addr.setBaseNasm(reg1);
                if (addr.index().isRegister()) {   // [mem + reg * 2]
                    return 1;
                } else {                           // [mem + mem * 2]
                    addMove(reg2, addr.index());
                    addr.setIndexNasm(reg2);
                    return 2;
                }
            }
        }
    }

    private boolean isAddress(Operand operand) {
        if (operand instanceof Address) {
            return true;
        } else if (operand instanceof Reference) {
            return !operand.isRegister();
        } else {
            return false;
        }
    }

    public void visit(Move ins) {
        boolean isAddrLeft  = isAddress(ins.dest());
        boolean isAddrRight = isAddress(ins.src());

        if (isAddrLeft && isAddrRight) {
            if (ins.src() instanceof Address)
                mem2reg((Address)ins.src(), rax(), rdx());
            add("mov", rdx(), ins.src());
            if (ins.dest() instanceof Address)
                mem2reg((Address)ins.dest(), rax(), rcx());
            add("mov", ins.dest(), rdx());
        } else {
            if (ins.dest() instanceof Address)  // cannot both be true
                mem2reg((Address)ins.dest(), rax(), rcx());
            if (ins.src() instanceof Address)   // cannot both be true
                mem2reg((Address)ins.src(), rax(), rcx());
            add("mov", ins.dest(), ins.src());
        }
    }

    public void visit(Lea ins) {
        mem2reg(ins.addr(), rax(), rdx());
        ins.addr().setShowSize(false);
        if (ins.dest().isRegister()) {
            add("lea", ins.dest(), ins.addr());
        } else {
            add("lea", rcx(), ins.addr());
            add("mov", ins.dest(), rcx());
        }
    }

    public void visit(Call ins) {
        List<Operand> operands = ins.operands();
        for (int i = operands.size() - 1; i >= 0; i--) {
            if (i < paraRegister.size()) {
                addMove(paraRegister.get(i), operands.get(i));
            } else {
                if (operands.get(i).isRegister()) {
                    add("push", operands.get(i));
                } else {
                    addMove(rax(), operands.get(i));
                    add("push", rax());
                }
            }
        }
        if (ins.entity().asmName().equals("printf"))
            add("xor", rax(), rax());
        add("call " + ins.entity().asmName());

        if (ins.ret() != null)
            add("mov", ins.ret(), rax());

        if (operands.size() > paraRegister.size())
            add("add", rsp(), new Immediate(
                    (operands.size() - paraRegister.size()) * VIRTUAL_STACK_REG_SIZE));
    }

    public void visit(Return ins) {
        if (ins.ret() != null)
            addMove(rax, ins.ret());
    }

    public void visit(CJump ins) {
        if (ins.cond().isRegister()) {
            add("test", ins.cond(), ins.cond());
        } else {
            addMove(rax(), ins.cond());
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
        return rax;
    }
    private Register rcx() {
        return rcx;
    }
    private Register rdx() {
        return rdx;
    }
    private Register rbx() {
        return rbx;
    }
    private Register rsp() {
        return rsp;
    }
    private Register rbp() {
        return rbp;
    }
    private Register rsi() {
        return rsi;
    }
    private Register rdi() {
        return rdi;
    }
    private Register R(int i) {
        return registers.get(i);
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
    }

    private void pasteLibfunction() {
        asm.add("\n;========== LIB BEGIN ==========");
        File f = new File("lib.s");
        try {
            BufferedReader fin = new BufferedReader(new FileReader(f));
            String line;
            while((line = fin.readLine()) != null)
                asm.add(line);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

