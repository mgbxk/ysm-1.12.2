package com.elfmcys.yesstevemodel.mclib.math;

public class Variable implements IValue {
    private final String name;
    private double value;

    public Variable(String name, double value) {
        this.name = name;
        this.value = value;
    }

    public void set(double value) {
        this.value = value;
    }

    @Override
    public double get() {
        return this.value;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
