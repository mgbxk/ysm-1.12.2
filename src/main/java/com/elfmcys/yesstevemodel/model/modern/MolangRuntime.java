package com.elfmcys.yesstevemodel.model.modern;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.MolangParser;
import java.util.Map;

/** The active player's function library and dynamics; restored on nested renders. */
public final class MolangRuntime implements AutoCloseable {
    private static final ThreadLocal<MolangRuntime> CURRENT = new ThreadLocal<>();
    private final MolangRuntime previous;
    private final Map<String, MolangProgram> functions;
    private final SecondOrderDynamics dynamics;
    private final double time;
    public MolangRuntime(Map<String, MolangProgram> functions, SecondOrderDynamics dynamics, double time) {
        this.previous = CURRENT.get(); this.functions = functions; this.dynamics = dynamics; this.time = time; CURRENT.set(this);
    }
    public static MolangProgram.Environment environment(MolangParser parser) {
        final MolangRuntime runtime = CURRENT.get();
        return new MolangProgram.Environment() {
            @Override public double read(String name) { return parser.readNumericQuery(name); }
            @Override public Object readValue(String name) { return parser.readQueryValue(name); }
            @Override public boolean isDefined(String name) { return parser.isQueryDefined(name); }
            @Override public void write(String name, double value) { parser.setValue(name, () -> value); }
            @Override public double function(String name, double[] arguments) {
                MolangProgram program = runtime == null ? null : runtime.functions.get(name);
                if (program == null) throw new IllegalArgumentException("Unknown model function: fn." + name);
                return program.evaluate(this, arguments);
            }
            @Override public double secondOrder(String key, double value, double frequency, double damping, double response) {
                return runtime == null ? value : runtime.dynamics.update(key, value, frequency, damping, response, runtime.time);
            }
        };
    }
    @Override public void close() { if (previous == null) CURRENT.remove(); else CURRENT.set(previous); }
}
