package com.elfmcys.yesstevemodel.mclib.math.functions.utility;

import com.elfmcys.yesstevemodel.mclib.math.IValue;
import com.elfmcys.yesstevemodel.mclib.math.functions.Function;
import com.elfmcys.yesstevemodel.mclib.utils.Interpolations;

public class Lerp extends Function {
    public Lerp(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 3;
    }

    @Override
    public double get() {
        return Interpolations.lerp(this.getArg(0), this.getArg(1), this.getArg(2));
    }
}
