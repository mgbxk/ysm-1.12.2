/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */
package com.elfmcys.yesstevemodel.geckolib3.core;

import com.elfmcys.yesstevemodel.geckolib3.core.manager.AnimationData;
import com.elfmcys.yesstevemodel.geckolib3.core.manager.AnimationFactory;

/**
 * 任何想要附加动画的模型，都需要继承此接口
 */
public interface IAnimatable {
    /**
     * 注册动画控制器
     *
     * @param data 数据
     */
    void registerControllers(AnimationData data);

    /**
     * 动画实例构造
     *
     * @return AnimationFactory
     */
    AnimationFactory getFactory();
}
