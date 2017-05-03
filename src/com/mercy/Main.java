package com.mercy;

import com.mercy.compiler.AST.AST;
import com.mercy.compiler.BackEnd.InstructionEmitter;
import com.mercy.compiler.Entity.Entity;
import com.mercy.compiler.Entity.VariableEntity;
import com.mercy.compiler.FrontEnd.ASTBuilder;
import com.mercy.compiler.BackEnd.IRBuilder;
import com.mercy.compiler.FrontEnd.ParserErrorListener;
import com.mercy.compiler.Parser.MalicLexer;
import com.mercy.compiler.Parser.MalicParser;
import com.mercy.compiler.Type.Type;
import com.mercy.compiler.Utility.LibFunction;
import com.mercy.compiler.Utility.SemanticError;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import static com.mercy.compiler.Type.Type.*;

public class Main {
    public static void main(String[] args) throws Exception {
        InputStream is = new FileInputStream("testcase/test.c");
        try {
            compile(is);
        } catch (SemanticError error) {
            System.err.println(error.getMessage());
            System.exit(1);
        } catch (InternalError error) {
            System.err.println(error.getMessage());
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static List<Entity> getLibrary() {
        List<Entity> ret = new LinkedList<>();

        // lib function
        ret.add(new LibFunction(voidType, "print", new Type[]{stringType}).getEntity());
        ret.add(new LibFunction(voidType, "println", new Type[]{stringType}).getEntity());
        ret.add(new LibFunction(stringType, "getString", null).getEntity());
        ret.add(new LibFunction(integerType, "getInt", null).getEntity());
        ret.add(new LibFunction(stringType, "toString", new Type[]{integerType}).getEntity());
        ret.add(new LibFunction(integerType, "__malloc", new Type[]{integerType}).getEntity());
        // null
        ret.add(new VariableEntity(null, nullType, "null", null));

        return ret;
    }

    public static void compile(InputStream sourceCode) throws Exception {
        ANTLRInputStream input = new ANTLRInputStream(sourceCode);
        MalicLexer lexer = new MalicLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MalicParser parser = new MalicParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new ParserErrorListener());
        ParseTree tree = parser.compilationUnit();

        ParseTreeWalker walker = new ParseTreeWalker();
        ASTBuilder listener = new ASTBuilder();

        walker.walk(listener, tree);  // 0th pass, CST -> AST

        AST ast  = listener.getAST();
        ast.loadLibrary(getLibrary());// load library function
        Type.initializeBuiltinType();

        ast.resolveSymbol();                         // 1st pass, extract info of class and function
        ast.checkType();                             // 2nd pass, check type

        IRBuilder irBuilder = new IRBuilder(ast);
        irBuilder.generateIR();                      // 3rd pass, generate IR, do simple constant folding

                                                     // 4th pass, emit instructions
        InstructionEmitter emitter = new InstructionEmitter(irBuilder);
        emitter.emit();
    }
}
