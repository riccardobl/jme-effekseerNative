package com.jme.effekseer;

import com.jme3.asset.AssetKey;


public class EffekseerEffectKey extends AssetKey<LoadedEffect>{
    
    public EffekseerEffectKey(String path){
        super(path);
    }

    @Override
    public boolean equals(Object other){
        return other instanceof EffekseerEffectKey&&super.equals(other);
    }


}