package com.elfmcys.yesstevemodel.model.modern;

import com.google.gson.JsonElement;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/** A duration expression, or a curve of elapsed seconds to the outgoing pose's weight. */
public final class BlendTransition {
    static final BlendTransition NONE = new BlendTransition("0", null, null);
    private final String durationExpression;
    private final double[] times;
    private final double[] weights;

    private BlendTransition(String durationExpression, double[] times, double[] weights) {
        this.durationExpression = durationExpression;
        this.times = times;
        this.weights = weights;
    }

    static BlendTransition parse(JsonElement json) throws IOException {
        if (json == null) return NONE;
        if (json.isJsonPrimitive() && !json.getAsJsonPrimitive().isBoolean()) {
            if (json.getAsJsonPrimitive().isNumber() && !Double.isFinite(json.getAsDouble())) throw new IOException("Non-finite blend duration");
            return new BlendTransition(json.getAsString(), null, null);
        }
        if (!json.isJsonObject()) throw new IOException("blend_transition must be a duration or curve object");
        TreeMap<Double, Double> points = new TreeMap<>();
        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
            double time;
            try { time = Double.parseDouble(entry.getKey()); }
            catch (NumberFormatException e) { throw new IOException("Invalid blend curve time: " + entry.getKey(), e); }
            if (!Double.isFinite(time) || time < 0) throw new IOException("Blend curve times must be finite and non-negative");
            JsonElement value = entry.getValue();
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber() || !Double.isFinite(value.getAsDouble())) {
                throw new IOException("Blend curve weights must be finite numbers");
            }
            // Normalize negative zero so it shares the implicit t=0 key.
            if (time == 0) time = 0;
            if (points.put(time, value.getAsDouble()) != null) throw new IOException("Duplicate blend curve time: " + entry.getKey());
        }
        if (points.isEmpty()) return NONE;
        points.putIfAbsent(0d, 1d);
        double[] times = new double[points.size()], weights = new double[points.size()];
        int i = 0;
        for (Map.Entry<Double, Double> point : points.entrySet()) {
            times[i] = point.getKey(); weights[i++] = point.getValue();
        }
        return new BlendTransition(null, times, weights);
    }

    double duration(ControllerMachine.Evaluation evaluation) {
        if (durationExpression == null) return 0;
        double seconds = evaluation.value(durationExpression);
        return Double.isFinite(seconds) && seconds > 0 ? seconds : 0;
    }

    double incomingWeight(double seconds, double duration) {
        seconds = Math.max(0, seconds);
        if (times == null) return duration <= 0 ? 1 : Math.min(1, seconds / duration);
        int index = Arrays.binarySearch(times, seconds);
        if (index >= 0) return 1 - weights[index];
        int high = -index - 1;
        if (high == 0) return 1 - weights[0];
        if (high == times.length) return 1 - weights[weights.length - 1];
        int low = high - 1;
        double amount = (seconds - times[low]) / (times[high] - times[low]);
        return 1 - (weights[low] + amount * (weights[high] - weights[low]));
    }
}
