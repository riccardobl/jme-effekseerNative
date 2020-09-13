package com.jme.effekseer;

import java.util.function.BiFunction;

import com.jme3.math.Transform;
import com.jme3.scene.Spatial;

public class EffekseerEmitterShapeFunctions{
    private static BiFunction< Integer,Spatial,Transform> pointFollowingSpatial=(handle,spatial)->{
        return spatial.getWorldTransform();
    };
    
    public static BiFunction< Integer,Spatial,Transform> pointFollowingSpatial(){
        return pointFollowingSpatial;
    }


}