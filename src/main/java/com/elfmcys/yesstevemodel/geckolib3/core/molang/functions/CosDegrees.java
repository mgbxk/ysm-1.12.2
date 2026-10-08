package com.elfmcys.yesstevemodel.geckolib3.core.molang.functions;

import com.elfmcys.yesstevemodel.mclib.math.IValue;
import com.elfmcys.yesstevemodel.mclib.math.functions.Function;

public class CosDegrees extends Function {
    public CosDegrees(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 1;
    }

    @Override
    public double get() {
        return Math.cos(this.getArg(0) / 180 * Math.PI);
    }
}
