package com.elfmcys.yesstevemodel.mclib.math.functions.limit;

import com.elfmcys.yesstevemodel.mclib.math.IValue;
import com.elfmcys.yesstevemodel.mclib.math.functions.Function;
import net.minecraft.util.math.MathHelper;

public class Clamp extends Function {
    public Clamp(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 3;
    }

    @Override
    public double get() {
        return MathHelper.clamp(this.getArg(0), this.getArg(1), this.getArg(2));
    }
}
