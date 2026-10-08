package com.elfmcys.yesstevemodel.model.modern;

import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.EventKeyFrame;
import java.util.List;
import java.util.function.Consumer;

/** Emits crossed frames once. On large time jumps only the current loop is replayed. */
public final class AnimationEvents {
    private AnimationEvents() {}

    public static void emit(Animation animation, List<EventKeyFrame<String>> frames, double previous, double elapsed, Consumer<String> event) {
        double length = animation.animationLength;
        boolean looping = length > 0 && Double.isFinite(length) && animation.loop.isRepeatingAfterEnd();
        double tick = AnimationSampler.localTick(animation, elapsed);
        double before = previous < 0 ? -1 : AnimationSampler.localTick(animation, previous);
        if (looping && previous >= 0 && Math.floor(elapsed / length) != Math.floor(previous / length)) before = -1;
        for (EventKeyFrame<String> frame : frames) {
            if (frame.getStartTick() > before && frame.getStartTick() <= tick) event.accept(frame.getEventData());
        }
    }
}
