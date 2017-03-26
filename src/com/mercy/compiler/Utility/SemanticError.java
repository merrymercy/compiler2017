package com.mercy.compiler.Utility;

import com.mercy.compiler.AST.Location;

/**
 * Created by mercy on 17-3-23.
 */
public class SemanticError extends Error {
    public SemanticError(Location loc, String message) {
        super(loc.toString() + message);
    }
}
