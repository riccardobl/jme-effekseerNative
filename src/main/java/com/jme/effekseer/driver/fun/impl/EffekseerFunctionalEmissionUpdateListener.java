package com.jme.effekseer.driver.fun.impl;

import java.io.IOException;
import java.util.Set;
import java.util.function.BiConsumer;

import com.jme.effekseer.driver.fun.EffekseerEmissionUpdateListener;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.util.clone.Cloner;

public class EffekseerFunctionalEmissionUpdateListener implements EffekseerEmissionUpdateListener,Cloneable{
    private final BiConsumer<Float,Set<Integer>> onUpdateFun;

    public EffekseerFunctionalEmissionUpdateListener(BiConsumer<Float,Set<Integer>> onUpdateFun){
        this.onUpdateFun=onUpdateFun;
    }

    @Override
    public void write(JmeExporter ex) throws IOException {

    }

    @Override
    public void read(JmeImporter im) throws IOException {

    }

    @Override
    public Object jmeClone() {
        try{
            return super.clone();
        }catch(CloneNotSupportedException e){
           return null;
        }
    }

    @Override
    public void cloneFields(Cloner cloner, Object original) {

    }

    @Override
    public void onUpdate(float tpf, Set<Integer> instanceKeysRO) {
        onUpdateFun.accept(tpf,instanceKeysRO);
    }
    
}