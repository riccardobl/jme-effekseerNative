package com.jme.effekseer.driver.fun.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.jme.effekseer.driver.fun.EffekseerDynamicInputSetterFun;
import com.jme.effekseer.driver.fun.EffekseerDynamicInputSupplier;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.util.clone.Cloner;

public class EffekseerGenericDynamicInputSupplier implements EffekseerDynamicInputSupplier{
    public ArrayList<Float> inputs=new ArrayList<Float>();

    public EffekseerGenericDynamicInputSupplier set(int index, Float value) {
        while(index>=inputs.size())inputs.add(Float.NaN);
        if(value == null){
            inputs.set(index,Float.NaN);
        }else{
            inputs.set(index,value);
        }
        while(Float.isNaN(inputs.get(inputs.size()-1)))inputs.remove(inputs.size()-1);        
        inputs.trimToSize();
        return this;
    }

    public float get(int index) {
        return inputs.get(index);
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule c=ex.getCapsule(this);
        float inputsA[]=new float[inputs.size()];
        for(int i=0;i<inputsA.length;i++) inputsA[i]=inputs.get(i);
        c.write(inputsA,"inputs",null);        
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        InputCapsule c=im.getCapsule(this);
        float inputsA[]=c.readFloatArray("inputs",null);
        for(int i=0;i<inputsA.length;i++){
            set(i,inputsA[i]);
        }
    }

    @Override
    public Object jmeClone() {
        EffekseerGenericDynamicInputSupplier clone=new EffekseerGenericDynamicInputSupplier();
        clone.inputs=(ArrayList<Float>)inputs.clone();
        return clone;
    }

    @Override
    public void cloneFields(Cloner cloner, Object original) {

    }

    @Override
    public void set(int handler, EffekseerDynamicInputSetterFun setter) {
        for(int i=0;i<inputs.size();i++){
            float v=inputs.get(i);
            if(Float.isNaN(v)){
                setter.set(i,v);
            }
        }
    }
    
}