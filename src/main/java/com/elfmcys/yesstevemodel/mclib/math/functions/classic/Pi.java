package com.elfmcys.yesstevemodel.mclib.math.functions.classic;

import com.elfmcys.yesstevemodel.mclib.math.IValue;
import com.elfmcys.yesstevemodel.mclib.math.functions.Function;

public class Pi extends Function {
    public Pi(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public double get() {
        return 3.141592653589793d;
    }
}
