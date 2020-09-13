package com.jme.effekseer;

import java.util.function.Supplier;

import com.jme3.math.Transform;
import com.jme3.scene.Spatial;

public interface EffekseerEmissionDriver{
    public void update(float tpf);
    public int tryEmit(Supplier<Integer> emitInstanceAndGetHandle);
    public void destroy(int handle);
    public Transform getInstanceTransform(int handle,Spatial sp);
}