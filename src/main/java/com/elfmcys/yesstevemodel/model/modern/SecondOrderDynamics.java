package com.elfmcys.yesstevemodel.model.modern;

import java.util.HashMap;
import java.util.Map;

/** Stable second-order response, keyed per player/model; repeated frame reads are idempotent. */
public final class SecondOrderDynamics {
    private final Map<String, State> states = new HashMap<>();
    private static final class State {
        double input, output, velocity, time, frequency, damping, response;
        boolean initialized;
    }
    public void clear() { states.clear(); }
    public double update(String key, double input, double frequency, double damping, double response, double time) {
        if (!Double.isFinite(input) || !Double.isFinite(time)) return 0;
        if (!Double.isFinite(frequency) || frequency <= 0 || !Double.isFinite(damping) || !Double.isFinite(response)) return input;
        frequency = Math.min(100, frequency); damping = Math.max(0, damping);
        State s = states.computeIfAbsent(key, ignored -> new State());
        double delta = time - s.time;
        if (!s.initialized || delta < 0 || delta > 0.5 || s.frequency != frequency || s.damping != damping || s.response != response) {
            s.initialized = true; s.output = input; s.velocity = 0; s.input = input; s.time = time;
            s.frequency = frequency; s.damping = damping; s.response = response; return s.output;
        }
        if (delta <= 0.000001) return s.output;
        double k1 = damping / (Math.PI * frequency), k2 = 1 / Math.pow(2 * Math.PI * frequency, 2), k3 = response * damping / (2 * Math.PI * frequency);
        double derivative = (input - s.input) / delta;
        int steps = Math.max(1, (int) Math.ceil(delta * 120)); double dt = delta / steps;
        double stable = Math.max(k2, Math.max(dt * dt / 2 + dt * k1 / 2, dt * k1));
        for (int i = 0; i < steps; i++) {
            double x = s.input + (input - s.input) * (i + 1d) / steps;
            s.output += dt * s.velocity;
            s.velocity += dt * (x + k3 * derivative - s.output - k1 * s.velocity) / stable;
        }
        s.input = input; s.time = time;
        if (!Double.isFinite(s.output) || !Double.isFinite(s.velocity)) { s.output = input; s.velocity = 0; }
        return s.output;
    }
}
