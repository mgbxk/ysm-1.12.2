package com.elfmcys.yesstevemodel.geckolib3.core.molang.functions;

import com.elfmcys.yesstevemodel.mclib.math.IValue;
import com.elfmcys.yesstevemodel.mclib.math.functions.Function;

public class SinDegrees extends Function {
    public SinDegrees(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 1;
    }

    @Override
    public double get() {
        return Math.sin(this.getArg(0) / 180 * Math.PI);
    }
}
