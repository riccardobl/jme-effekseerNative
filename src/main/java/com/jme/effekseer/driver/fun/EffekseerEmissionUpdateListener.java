package com.jme.effekseer.driver.fun;

import com.jme3.export.Savable;
import com.jme3.util.clone.JmeCloneable;

public interface EffekseerEmissionUpdateListener extends Savable,JmeCloneable{
    public void onUpdate(float tpf);
}