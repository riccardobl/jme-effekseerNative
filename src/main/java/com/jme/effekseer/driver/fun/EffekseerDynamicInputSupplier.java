package com.jme.effekseer.driver.fun;

import com.jme3.export.Savable;
import com.jme3.util.clone.JmeCloneable;

public interface EffekseerDynamicInputSupplier extends Savable,JmeCloneable{
    public void set(int handler,EffekseerDynamicInputSetterFun setter);
}