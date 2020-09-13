package com.jme.effekseer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.jme3.math.Transform;
import com.jme3.scene.Spatial;

public class EffekseerEmissionDriverGeneric implements EffekseerEmissionDriver{
    public static enum CallbackType{
        SET_HANDLE,
        DESTROY_HANDLE
    }
   public static interface Callback{
        public void call(CallbackType type,Integer handler);
   }    
    protected Map<Integer,Callback > instances=new HashMap<Integer,Callback >();
    protected Function< Float,Callback> emitFunction=EffekseerEmitterEmitFunctions.emitLoop(1,0f, 1f,1f);
    protected BiFunction< Integer,Spatial,Transform> shapeFunction=EffekseerEmitterShapeFunctions.pointFollowingSpatial();
    
    protected float time=0;

    @Override
    public void update(float tpf) {
        time+=tpf;
    }

    public EffekseerEmissionDriverGeneric emitFunction(Function< Float,Callback>  f){
        this.emitFunction=f;
        return this;
    }

    public EffekseerEmissionDriverGeneric shapeFunction( BiFunction< Integer,Spatial,Transform>  f){
        this.shapeFunction=f;
        return this;
    }

    @Override
    public int tryEmit(Supplier<Integer> emitInstanceAndGetHandle) {
        Callback callback=emitFunction.apply(time);
        if(callback!=null){
            int handle=emitInstanceAndGetHandle.get();
            callback.call(CallbackType.SET_HANDLE, handle);
            instances.put(handle,callback);
            return handle;
        }
        return -1;
    }

    @Override
    public void destroy(int handle) {
        Callback callback= instances.remove(handle);
        if(callback!=null){
            callback.call(CallbackType.DESTROY_HANDLE,handle);
        }
    }

    @Override
    public Transform getInstanceTransform(int handle, Spatial sp) {
        return shapeFunction.apply(handle,sp);
    }
    
}