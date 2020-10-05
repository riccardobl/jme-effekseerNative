package com.jme.effekseer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.jme.effekseer.driver.EffekseerEmissionDriverGeneric;
import com.jme.effekseer.driver.fun.EffekseerEmissionDriver;
import com.jme3.asset.AssetManager;
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
    protected boolean play=true;
    protected float scale=1f;
    protected transient int layer=0;

    protected EffekseerEmissionDriver driver=new EffekseerEmissionDriverGeneric();


    public EffekseerEmitterControl(){
        Effekseer.registerEmitter(this);
    }
    
    public EffekseerEmitterControl(AssetManager am,String path){
        this();
        EffekseerEffectKey k=new EffekseerEffectKey(path);
        LoadedEffect ef=am.loadAsset(k);
        setEffect(ef.core);
        setPath(ef.path);
    }

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
        if(spatial==null)return false;
        else if(spatial==parent)return true;
        else {
            Spatial nextS=spatial.getParent();
            if(nextS==null)return false;
            return isChildOf(nextS,parent);
        }
    }

    public void setLayer(int id){
        this.layer=id;
        this.instances.stream().forEach(i->Effekseer.setEffectLayer(i, layer));

    }

    public int getLayer(){
        return this.layer;
    }

    public void setScale(float v){
        scale=v;
    }

    public void stop(){
        if(!play)return;
        this.instances.stream().forEach(i->Effekseer.stopEffect(i));
        this.instances.clear();
        play=false;
    }

    public void pause(){
        if(!play)return;
        this.instances.stream().forEach(i->Effekseer.pauseEffect(i,true));
        play=false;
    }

    

    public void play(){
        if(play)return;
        this.instances.stream().forEach(i->Effekseer.pauseEffect(i,false));
        play=true;
    }


    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for(int i:instances){
            Effekseer.pauseEffect(i, !enabled);
            Effekseer.setEffectVisibility(i, enabled);
        }        
    }


    @Override
    protected void controlUpdate(float tpf) {
        if(!play)return;
        Integer newHandle=driver.tryEmit(()->Effekseer.playEffect(effekt));
        
        if(newHandle!=null){
            instances.add(newHandle);
           Effekseer.setEffectLayer(newHandle, layer);
        }  

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
                Transform tr=driver.getInstanceTransform(handle, spatial,scale);
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
        c.write(play,"playing",true);    
        c.write(scale,"scale",1f);
    }

    public  void read(JmeImporter im) throws IOException{
        InputCapsule c=im.getCapsule(this);
        effektPath=c.readString("path", null);
        driver=(EffekseerEmissionDriver)c.readSavable("driver",null);
        play=c.readBoolean("playing",true);    
        scale=c.readFloat("playing",1f);    
        if(effektPath!=null){
            EffekseerEffectKey k=new EffekseerEffectKey(effektPath);
            LoadedEffect ef=im.getAssetManager().loadAsset(k);
            setEffect(ef.core);
            setPath(ef.path);
            Effekseer.registerEmitter(this);
        }
    }

    @Override
    public Object jmeClone() {
        EffekseerEmitterControl e= new EffekseerEmitterControl();
        e.setEffect(effekt);
        e.setPath(effektPath);
        e.setScale(scale);
        e.play=play;        
        e.driver=driver;
        return e;
    }     

    @Override
    public void cloneFields( Cloner cloner, Object original ) { 
        super.cloneFields(cloner, original);
        this.driver = cloner.clone(driver);
    }
         
    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if(spatial==null){
            stop();
            // Effekseer.unregisterEmitter(this);
        }
    }

    @Override
    public void finalize(){        
        stop();
    }


    public EffekseerEffectCore getEffekt(){
        return effekt;
    }
    
}