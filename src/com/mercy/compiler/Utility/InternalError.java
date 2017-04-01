package com.mercy.compiler.Utility;

import com.mercy.compiler.AST.Location;

/**
 * Created by mercy on 17-3-23.
 */
public class InternalError extends Error {
    public InternalError(String message) {
        super(message);
    }

    public InternalError(Location loc, String message) {
        super(loc.toString() + message);
    }
}
