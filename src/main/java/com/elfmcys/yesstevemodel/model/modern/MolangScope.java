package com.elfmcys.yesstevemodel.model.modern;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.MolangParser;
import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleSupplier;

/** Restores shared parser bindings while preserving entity-local variable values. */
public final class MolangScope implements AutoCloseable {
    private final Map<String, Double> values;
    private final Map<String, DoubleSupplier> previous = new HashMap<>();
    private final Map<String, Boolean> initialized = new HashMap<>();

    public MolangScope(Map<String, Double> values) {
        this.values = values;
        values.keySet().forEach(name -> MolangParser.VARIABLES.computeIfAbsent(name, key -> {
            com.elfmcys.yesstevemodel.geckolib3.core.molang.LazyVariable value = new com.elfmcys.yesstevemodel.geckolib3.core.molang.LazyVariable(key, 0);
            value.unset(); return value;
        }));
        MolangParser.VARIABLES.forEach((name, variable) -> {
            if (scoped(name)) {
                previous.put(name, variable.getSupplier());
                initialized.put(name, variable.isInitialized());
                if (name.startsWith("variable.") && !values.containsKey(name) || name.startsWith("temp.")) variable.unset();
                else variable.set(name.startsWith("variable.") ? values.get(name) : 0d);
            }
        });
    }

    private static boolean scoped(String name) {
        return name.startsWith("variable.") || name.startsWith("temp.") || name.equals("query.anim_time")
                || name.equals("query.state_time") || name.equals("query.all_animations_finished") || name.equals("query.any_animation_finished");
    }

    @Override
    public void close() {
        MolangParser.VARIABLES.forEach((name, variable) -> {
            if (scoped(name)) {
                if (name.startsWith("variable.") && variable.isInitialized()) values.put(name, variable.get());
                variable.restore(previous.getOrDefault(name, () -> 0), initialized.getOrDefault(name, false));
            }
        });
    }
}
