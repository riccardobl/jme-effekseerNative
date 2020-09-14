package com.jme.effekseer;

import java.util.function.Supplier;

import com.jme3.math.Transform;
import com.jme3.scene.Spatial;

public interface EffekseerEmissionDriver{
    public interface DynamicInputSetterFun{
        public void set(int index,float value);
    }
    public interface EmitFun{
        public int emit();
    }
    public void update(float tpf);
    public int tryEmit(EmitFun emitInstanceAndGetHandle);
    public void setDynamicInputs( int handle,DynamicInputSetterFun set);
    public void destroy(int handle);
    public Transform getInstanceTransform(int handle,Spatial sp);
}