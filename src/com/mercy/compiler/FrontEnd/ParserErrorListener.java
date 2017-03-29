package com.mercy.compiler.FrontEnd;

import com.mercy.compiler.AST.Location;
import com.mercy.compiler.Utility.SemanticError;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * Created by mercy on 17-3-29.
 */
public class ParserErrorListener extends BaseErrorListener {
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object symbol, int row, int column, String message, RecognitionException e) {
        throw new SemanticError(new Location(row, column), message);
    }
}
