package com.jme.effekseer.driver.fun;

import com.jme3.export.Savable;
import com.jme3.math.Transform;
import com.jme3.scene.Spatial;
import com.jme3.util.clone.JmeCloneable;

public interface EffekseerEmitterShape extends Savable,JmeCloneable{
    public Transform getTransform(int handler,Spatial sp,float scale);
}