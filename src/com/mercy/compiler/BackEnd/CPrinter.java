package com.mercy.compiler.BackEnd;

import com.mercy.compiler.Entity.Entity;
import com.mercy.compiler.Entity.FunctionEntity;
import com.mercy.compiler.Entity.Scope;
import com.mercy.compiler.Entity.VariableEntity;
import com.mercy.compiler.FrontEnd.AST;
import com.mercy.compiler.IR.*;
import com.mercy.compiler.Utility.InternalError;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by mercy on 17-6-10.
 */
public class CPrinter implements IRVisitor<String>{
    private List<FunctionEntity> functionEntities;
    private List<String> buffer = new LinkedList<>();
    private Scope globalScope;

    public CPrinter(AST ast, IRBuilder irBuilder) {
        functionEntities = irBuilder.functionEntities();
        globalScope = ast.scope();
    }

    public void print() throws Exception {
        buffer.add("#include <stdio.h>\n");
        buffer.add("#include <stdlib.h>\n");
        buffer.add("#include <string.h>\n");

        // prototype
        for (Entity entity : globalScope.entities().values()) {
            if (entity instanceof FunctionEntity) {
                addStmt(getPrototype((FunctionEntity)entity));
            }
        }

        // global variable


        // functions
        for (FunctionEntity functionEntity : functionEntities) {
            if (functionEntity.isInlined())
                continue;
            printFunction(functionEntity);
        }

        // library
        pasteLib("c-printer-lib.c");

        // output
        PrintStream os = new PrintStream(new FileOutputStream("out.c"));
        for (String s : buffer) {
            os.print(s);
        }
    }

    private String getPrototype(FunctionEntity entity) {
        String dec;

        if (entity.returnType().isVoid())
            dec = "void ";
        else
            dec = "long ";

        if (entity.name().equals("main"))
            dec = "int ";

        dec += entity.name() + "(";
        for (int i = 0; i < entity.params().size(); i++) {
            dec += "long ";
            dec += entity.params().get(i).name();
            if (i != entity.params().size() - 1)
                dec += ", ";
        }
        dec += ")";
        return dec;
    }

    private int counter = 0;
    private void printFunction(FunctionEntity entity) {
        // prototype
        String dec = getPrototype(entity);
        add(dec + "{");

        // local variable
        for (VariableEntity variableEntity : entity.allLocalVariables()) {
            variableEntity.setName(variableEntity.name() + "_" + counter++);
            addStmt("long " + variableEntity.name());
        }

        // statement
        for (IR ir : entity.IR()) {
            ir.accept(this);
        }
        add("}");
    }

    private void add(String str) {
        buffer.add(str + "\n");
    }
    private void addStmt(String str) {
        buffer.add(str + ";\n");
    }

    public String visit(Addr ir) {
        return ir.entity().name();
    }

    public String visit(Assign ir) {
        String left = visitExpr(ir.left());

        if (ir.left() instanceof Addr) {  //a = 12;
            ;
        } else {                          // *(p + 2) = 12;
            left = "*((long *)" + left + ")";
        }

        addStmt( left + " = " + visitExpr(ir.right()));
        return null;
    }

    public String visit(Binary ir) {
        String op;
        switch (ir.operator()) {
            case ADD:       op = " + ";  break;
            case BIT_AND:   op = " & ";  break;
            case BIT_OR:    op = " | ";  break;
            case BIT_XOR:   op = " ^ ";  break;
            case DIV:       op = " / ";  break;
            case EQ:        op = " == "; break;
            case GE:        op = " >= "; break;
            case GT:        op = " > ";  break;
            case LE:        op = " <= "; break;
            case LOGIC_AND: op = " && "; break;
            case LOGIC_OR:  op = " || "; break;
            case LSHIFT:    op = " << "; break;
            case LT:        op = " < ";  break;
            case MOD:       op = " % ";  break;
            case MUL:       op = " * ";  break;
            case NE:        op = " != "; break;
            case RSHIFT:    op = " >> "; break;
            case SUB:       op = " - "; break;
            default:
                throw new InternalError("invalid operator");
        }
        return "(" + visitExpr(ir.left()) + op + visitExpr(ir.right()) + ")";
    }

    public String visit(Call ir) {
        String ret = ir.entity().name() + "(";

        for (int i = 0; i < ir.args().size(); i++) {
            ret += visitExpr(ir.args().get(i));
            if (i != ir.args().size() - 1)
                ret += ",";
        }
        ret += ")";

        if (depth == 0)
            addStmt(ret);
        return ret;
    }

    public String visit(CJump ir) {
        add("if(" + visitExpr(ir.cond()) + ")");
        addStmt("goto " + ir.trueLabel().name());
        add("else");
        addStmt("goto " + ir.falseLabel().name());
        return null;
    }

    public String visit(IntConst ir) {
        return "(" + ir.value() + ")";
    }

    public String visit(Jump ir) {
        addStmt("goto " + ir.label().name());
        return null;
    }

    public String visit(Label ir) {
        addStmt(ir.name() + ":");
        return null;
    }

    public String visit(Mem ir) {
        return "(*(long *)" + visitExpr(ir.expr()) + ")";
    }

    public String visit(Return ir) {
        addStmt("return " + visitExpr(ir.expr()));
        return null;
    }

    public String visit(StrConst ir) {
        return "((long)" + ir.entity().strValue() + ")";
    }

    public String visit(Unary ir) {
        switch (ir.operator()) {
            case BIT_NOT:
                return "(~" + visitExpr(ir.expr()) + ")";
            case LOGIC_NOT:
                return "(~" + visitExpr(ir.expr()) + ")";
            case MINUS:
                return "(-" + visitExpr(ir.expr()) + ")";
            default:
                throw new InternalError("invalid operator");
        }
    }

    public String visit(Var ir) {
        return "(" + ir.entity().name() + ")";
    }

    private int depth = 0;
    public String visitExpr(Expr ir) {
        depth++;
        String ret = ir.accept(this);
        depth--;
        return ret;
    }

    private void pasteLib(String filename) {
        File f = new File(filename);
        try {
            BufferedReader fin = new BufferedReader(new FileReader(f));
            String line;
            while((line = fin.readLine()) != null)
                add(line);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
