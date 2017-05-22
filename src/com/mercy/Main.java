package com.mercy;

import com.mercy.compiler.AST.AST;
import com.mercy.compiler.BackEnd.IRBuilder;
import com.mercy.compiler.BackEnd.InstructionEmitter;
import com.mercy.compiler.BackEnd.Translator;
import com.mercy.compiler.Entity.Entity;
import com.mercy.compiler.Entity.VariableEntity;
import com.mercy.compiler.FrontEnd.ASTBuilder;
import com.mercy.compiler.FrontEnd.ParserErrorListener;
import com.mercy.compiler.Parser.MalicLexer;
import com.mercy.compiler.Parser.MalicParser;
import com.mercy.compiler.Type.Type;
import com.mercy.compiler.Utility.LibFunction;
import com.mercy.compiler.Utility.SemanticError;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import static com.mercy.compiler.Type.Type.*;
import static com.mercy.compiler.Utility.LibFunction.LIB_PREFIX;
import static java.lang.System.exit;

public class Main {

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--print-ins":
                    Option.printInsturction = true;
                    break;
                case "-in":
                    if (i + 1 >= args.length)
                        System.err.println("invalid argument for input file, use default setting instead");
                    else
                        Option.inFile = args[++i];
                    break;
                case "-out":
                    if (i + 1 >= args.length)
                        System.err.println("invalid argument for output file, use default setting instead");
                    else
                        Option.outFile = args[++i];
                    break;
            }
        }

        InputStream is = new FileInputStream(Option.inFile);
        PrintStream os = new PrintStream(new FileOutputStream(Option.outFile));

        try {
            compile(is, os);
        } catch (SemanticError error) {
            System.err.println(error.getMessage());
            exit(1);
        } catch (InternalError error) {
            System.err.println(error.getMessage());
            exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            exit(1);
        }
    }

    public static List<Entity> getLibrary() {
        List<Entity> ret = new LinkedList<>();

        // lib function
        ret.add(new LibFunction(voidType, "print", "printf", new Type[]{stringType}).getEntity());
        ret.add(new LibFunction(voidType, "println", "puts", new Type[]{stringType}).getEntity());
        ret.add(new LibFunction(stringType, "getString", null).getEntity());
        ret.add(new LibFunction(integerType, "getInt", null).getEntity());
        ret.add(new LibFunction(stringType, "toString", new Type[]{integerType}).getEntity());
        ret.add(new LibFunction(integerType, LIB_PREFIX + "printInt", LIB_PREFIX + "printInt", new Type[]{integerType}).getEntity());
        ret.add(new LibFunction(integerType, LIB_PREFIX + "printlnInt", LIB_PREFIX + "printlnInt", new Type[]{integerType}).getEntity());
        ret.add(new LibFunction(integerType, LIB_PREFIX + "malloc", "malloc", new Type[]{integerType}).getEntity());
        // null
        ret.add(new VariableEntity(null, nullType, "null", null));

        return ret;
    }

    public static void compile(InputStream sourceCode, PrintStream asmCode) throws Exception {
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
        if (Option.enableOutputIrrelevantElimination)
            ast.eliminateOutputIrrelevantNode();

        IRBuilder irBuilder = new IRBuilder(ast);
        irBuilder.generateIR();                      // 3rd pass, generate IR, do simple constant folding

        // 4th pass, emit instructions
        InstructionEmitter emitter = new InstructionEmitter(irBuilder);
        emitter.emit();
        if (Option.printInsturction)
            emitter.printSelf(System.out);

        // 5th pass, translate to x86 nasm
        Translator translator = new Translator(emitter);
        List<String> asm = translator.translate();
        //translator.printSelf(System.out);

        for (String s : asm) {
            asmCode.println(s);
        }
    }
}
