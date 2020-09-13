package com.jme.effekseer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.jme.effekseer.EmissionDriverGeneric.Callback;
import com.jme.effekseer.EmissionDriverGeneric.CallbackType;
import com.jme3.math.FastMath;

public class EffekseerEmitterEmitFunctions{
    public static Function< Float,Callback> emitOnce(float initialDelay){
            return new  Function< Float,Callback>(){
                boolean emitted=false;
                Float lastTime=null;

				@Override
				public Callback apply(Float t) {
                    if(lastTime==null)  lastTime=t;
                    if(t-lastTime<initialDelay)return null;
                    if(emitted)     return null;
                    emitted=true;
                    return (a,b)->{};
				}
            };
    }

    public static Function< Float,Callback> emitLoop(int maxInstances,float initialDelay,float minDelay,float maxDelay){
        final List<Integer> instances=new ArrayList<Integer>();
        return new Function< Float,Callback> (){
            float delta=initialDelay;
            Float lastTime=null;
            @Override
            public Callback apply(Float t) {
                if(lastTime==null)  lastTime=t;
            
                            
                if(t-lastTime>delta){
                    delta= FastMath.nextRandomFloat()*(maxDelay-minDelay)+minDelay;
                    lastTime=t;
                }else{
                    return null;
                }                

                if(instances.size()>maxInstances)return null;
                return (type,handle)->{
                    if(type==EmissionDriverGeneric.CallbackType.DESTROY_HANDLE){
                        instances.remove(handle);
                    }else  if(type==EmissionDriverGeneric.CallbackType.SET_HANDLE){
                        instances.add(handle);
                    }
                };
            }
    
        };
    }
}