package com.jme.effekseer;

import java.util.ArrayList;
import java.util.List;

import com.jme3.math.Transform;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

import Effekseer.swig.EffekseerEffectCore;

public class EffekseerEmitterControl extends AbstractControl{
    protected List<Integer> instances=new ArrayList<Integer>();

    protected EffekseerEffectCore effekt;
    protected EffekseerEmissionDriver driver=new EffekseerEmissionDriverGeneric();

      
    public EffekseerEmitterControl(EffekseerEffectCore e){
        effekt=e;
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



    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {

    }
}