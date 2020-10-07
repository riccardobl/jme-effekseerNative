package com.jme.effekseer.driver.fun;

import java.util.Map;
import java.util.Set;

import com.jme3.export.Savable;
import com.jme3.util.clone.JmeCloneable;

public interface EffekseerEmissionUpdateListener extends Savable,JmeCloneable{
    public void onUpdate(float tpf,Set<Integer> instanceKeysRO);
}