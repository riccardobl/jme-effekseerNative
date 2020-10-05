package com.jme.effekseer.driver.fun;

import com.jme3.export.Savable;
import com.jme3.math.Transform;
import com.jme3.scene.Spatial;
import com.jme3.util.clone.JmeCloneable;

public interface EffekseerEmissionDriver extends Savable,JmeCloneable{
 
    public void update(float tpf);
    public Integer tryEmit(EffekseerEmitFun emitInstanceAndGetHandle);
    public void setDynamicInputs( int handle,EffekseerDynamicInputSetterFun set);
    public void destroy(int handle);
    public Transform getInstanceTransform(int handle,Spatial sp,float scale);
}