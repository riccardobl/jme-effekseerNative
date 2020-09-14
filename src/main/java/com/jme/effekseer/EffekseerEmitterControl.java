package com.jme.effekseer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.jme.effekseer.driver.EffekseerEmissionDriverGeneric;
import com.jme.effekseer.driver.fun.EffekseerEmissionDriver;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.math.Transform;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.util.clone.Cloner;

import Effekseer.swig.EffekseerEffectCore;

/**
 * EffekseerEmitterControl: attach it to a spatial and it will start spawning effects.
 * Usually this is created by loading an  efkefc files with the assetManager and not directly.
 * @author Riccardo Balbo
 */
public class EffekseerEmitterControl extends AbstractControl implements Savable{
    protected transient final List<Integer> instances=new ArrayList<Integer>();
    protected transient final Collection<Integer> instancesRO=Collections.unmodifiableCollection(instances);
    protected transient   EffekseerEffectCore effekt;
    protected String effektPath;

    protected EffekseerEmissionDriver driver=new EffekseerEmissionDriverGeneric();


    public EffekseerEmitterControl(){}
    

    protected void setEffect(EffekseerEffectCore core){
        this.effekt=core;
    }

    protected void setPath(String path){
        this.effektPath=path;
    }

    public void setDriver(EffekseerEmissionDriver d){
        driver=d;
    }

    public EffekseerEmissionDriver getDriver(){
        return driver;
    }
    
    public boolean isChildOf(Spatial parent){
       return  isChildOf(spatial,parent);
    }

    protected boolean isChildOf(Spatial spatial,Spatial parent){
        if(spatial==parent)return true;
        else return isChildOf(spatial.getParent(),parent);
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for(int i:instances){
            Effekseer.setEffectVisibility(i, enabled);
            Effekseer.pauseEffect(i, !enabled);
        }        
    }

    @Override
    protected void controlUpdate(float tpf) {
        
        int newHandle=driver.tryEmit(()->Effekseer.playEffect(effekt));
        if(newHandle>=0)   instances.add(newHandle);

        for(int i=0;i<instances.size();i++){
            Integer handle=instances.get(i);
            if(!Effekseer.isEffectAlive(handle)){
                driver.destroy(handle);
                instances.remove(i);
                i--;
            }else{
                driver.setDynamicInputs(handle, (index,value)->{
                    Effekseer.setDynamicInput(handle,index, value);
                });
                Transform tr=driver.getInstanceTransform(handle, spatial);
                Effekseer.setEffectTransform(handle,tr);
            }
        }

        driver.update(tpf);

    }

    public Collection<Integer> getHandles() {
        return instancesRO;
    }
      

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {

    }

    public void write(JmeExporter ex) throws IOException{
        OutputCapsule c=ex.getCapsule(this);
        c.write(effektPath,"path",null);    
        c.write(driver,"driver",null);    
    }

    public  void read(JmeImporter im) throws IOException{
        InputCapsule c=im.getCapsule(this);
        effektPath=c.readString("path", null);
        driver=(EffekseerEmissionDriver)c.readSavable("driver",null);
        if(effektPath!=null)Effekseer.loadEffect(im.getAssetManager(),effektPath,this);
    }

    @Override
    public Object jmeClone() {
        EffekseerEmitterControl e= new EffekseerEmitterControl();
        e.setEffect(effekt);
        e.setPath(effektPath);
        e.driver=driver;
        return e;
    }     

    @Override
    public void cloneFields( Cloner cloner, Object original ) { 
        this.driver = cloner.clone(driver);
    }
         


}