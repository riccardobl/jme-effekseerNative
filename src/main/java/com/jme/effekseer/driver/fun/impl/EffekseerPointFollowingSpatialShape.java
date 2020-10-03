package com.jme.effekseer.driver.fun.impl;

import java.io.IOException;

import com.jme.effekseer.driver.fun.EffekseerEmitterShape;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.scene.Spatial;
import com.jme3.util.TempVars;
import com.jme3.util.clone.Cloner;

public class EffekseerPointFollowingSpatialShape implements EffekseerEmitterShape{
    private transient Transform tmp_tr=new Transform();
    private boolean ignoreRot=false;

    public EffekseerPointFollowingSpatialShape ignoreRot(boolean v){
        ignoreRot=v;
        return this;
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule oc=ex.getCapsule(this);
        oc.write(ignoreRot, "ignoreRot", false);
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        InputCapsule oc=im.getCapsule(this);
        ignoreRot=oc.readBoolean( "ignoreRot", false);
    }



    @Override
    public Transform getTransform(int handler, Spatial sp,float scale) {
        tmp_tr.set(sp.getWorldTransform());
        tmp_tr.getScale().multLocal(scale);
        if(ignoreRot)tmp_tr.setRotation(Quaternion.IDENTITY);
        return tmp_tr;
    }

    @Override
    public Object jmeClone() {
        EffekseerPointFollowingSpatialShape cl= new EffekseerPointFollowingSpatialShape();
        cl.ignoreRot=ignoreRot;
        return cl;
    }

    @Override
    public void cloneFields(Cloner cloner, Object original) {

    }
    
}