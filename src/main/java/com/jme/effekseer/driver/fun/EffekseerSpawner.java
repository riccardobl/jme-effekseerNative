package com.jme.effekseer.driver.fun;

import com.jme3.export.Savable;
import com.jme3.util.clone.JmeCloneable;

public interface EffekseerSpawner  extends Savable,JmeCloneable{
    public EffekseerEmissionCallback spawn(float tpf);
}