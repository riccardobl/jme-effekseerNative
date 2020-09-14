package com.jme.effekseer.driver.fun.impl;

import java.io.IOException;

import com.jme.effekseer.driver.fun.EffekseerEmitterShape;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.math.Transform;
import com.jme3.scene.Spatial;
import com.jme3.util.clone.Cloner;

public class EffekseerPointFollowingSpatialShape implements EffekseerEmitterShape{

    @Override
    public void write(JmeExporter ex) throws IOException {

    }

    @Override
    public void read(JmeImporter im) throws IOException {

    }

    @Override
    public Transform getTransform(int handler, Spatial sp) {
        return sp.getWorldTransform();
    }

    @Override
    public Object jmeClone() {
        return new EffekseerPointFollowingSpatialShape();
    }

    @Override
    public void cloneFields(Cloner cloner, Object original) {

    }
    
}