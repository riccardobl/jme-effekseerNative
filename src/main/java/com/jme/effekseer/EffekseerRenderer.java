package com.jme.effekseer;

import java.lang.reflect.Field;
import java.util.EnumSet;

import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.post.Filter;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.SceneProcessor;
import com.jme3.profile.AppProfiler;
import com.jme3.renderer.Camera;
import com.jme3.renderer.Caps;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.opengl.GLRenderer;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.image.ColorSpace;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;

public class EffekseerRenderer {
    public static EffekseerRenderer addToViewPort(AppStateManager stateManager,ViewPort vp,AssetManager am,boolean sRGB){
        
        return addToViewPort(stateManager,vp,am,sRGB,false,false);
    }

    public static EffekseerRenderer addToViewPort(AppStateManager stateManager,ViewPort vp,AssetManager am,boolean sRGB,boolean is2D){
        return addToViewPort(stateManager,vp,am,sRGB,is2D,false);
    }

    public static EffekseerRenderer addToViewPort(AppStateManager stateManager,ViewPort vp,AssetManager am,boolean sRGB,boolean is2D,boolean useOffscreen){
        if(stateManager.getState(EffekseerUpdater.class)==null)    stateManager.attach(new EffekseerUpdater());

        FilterPostProcessor fpp=null;

        if(!vp.getName().equals("Gui Default")){ // Detect when attached to default guiViewPort in jme.
            for(SceneProcessor p:vp.getProcessors()){
                if(p instanceof FilterPostProcessor){
                    fpp=(FilterPostProcessor)p;
                    break;
                }
            }

        }else{
            System.out.println("Detected default Gui View Port");
            useOffscreen=true;
            is2D=true;
        }

        if(fpp!=null){
            EffekseerFilter filter=new EffekseerFilter(am, sRGB);
            fpp.addFilter(filter);
            return filter.getRenderer();
        }else{
            EffekseerProcessor p=new EffekseerProcessor(am, sRGB, is2D, useOffscreen);
            vp.addProcessor(p);
            return p.getRenderer();
        }
    }

    protected FrameBuffer renderTarget;
    protected float tpf;
    private float particlesHardness=0.1f;
    private float particlesContrast=2f;
    private ViewPort viewPort;
    private   RenderManager renderManager;
    private FrameBuffer sceneBuffer;
    private boolean sRGB;
    boolean is2D;
    int offscreenSamples;

    public EffekseerRenderer( AssetManager manager, boolean sRGB){
        Effekseer.init(manager,sRGB);
        this.sRGB=sRGB;
    }

    public void setAsync( int nThreads) {
        Effekseer.setAsync(nThreads);
    }

    public void setSoftParticles( float hardness,  float contrast) {
        particlesHardness=hardness;
        particlesContrast=contrast;
    }

    public void setHardParticles() {
        particlesHardness=1000;
        particlesContrast=1f;
    }

    protected void setViewPort( ViewPort vp) {
        viewPort=vp;

    }


    public void setRenderManager(RenderManager rm){
        renderManager=rm;

    }

    public Texture getTexture(){
        if(renderTarget==null)return null;
        return renderTarget.getColorBuffer().getTexture();
    }
    
    public void setTpf(final float tpf) {
        this.tpf=tpf;
    }

    public void setSceneBuffer(FrameBuffer sceneBuffer){
        this.sceneBuffer=sceneBuffer;
    }

    protected FrameBuffer getRenderTarget(  FrameBuffer in,  int offscreen,boolean sRGB) {
        int width=viewPort.getCamera().getWidth();
        int height=viewPort.getCamera().getHeight();

        if(offscreen<=0) return in;

         EnumSet<Caps> caps=renderManager.getRenderer().getCaps();

        Format depthFormat=Format.Depth;
        if(caps.contains(Caps.FloatDepthBuffer)){
            depthFormat=Format.Depth32F;
        }

        Format colorFormat=Format.RGBA8;
        if(caps.contains(Caps.FloatTexture)){
            colorFormat=Format.RGBA16F;
        }

        if(renderTarget == null || renderTarget.getWidth() != width || renderTarget.getHeight() != height || renderTarget.isSrgb()!=sRGB){
            System.out.println("Create render target " + width  + "x" + height);
            if(renderTarget != null) renderTarget.dispose();
            renderTarget=new FrameBuffer(width,height,offscreen);
            renderTarget.setDepthTexture(new Texture2D(width,height,offscreen,depthFormat));
            renderTarget.setColorTexture(new Texture2D(width,height,offscreen,colorFormat));
            if(sRGB){
                System.out.println("Enable sRGB");
                renderTarget.getColorBuffer().getTexture().getImage().setColorSpace(ColorSpace.sRGB);
                renderTarget.setSrgb(sRGB);
            }
        }
        assert renderTarget != null;

        FrameBuffer ofb=EffekseerUtils.bindFrameBuffer(renderManager,renderTarget);
        EffekseerUtils.clearFrameBuffer(renderManager,renderTarget,false,true,true,ColorRGBA.BlackNoAlpha);
        EffekseerUtils.blitFrameBuffer(renderManager,in,renderTarget,true,false,renderTarget.getWidth(),renderTarget.getHeight());
        EffekseerUtils.bindFrameBuffer(renderManager,ofb);
        return renderTarget;
    }



    public void set2D(boolean v){
        is2D=v;
    }


    public void useOffscreenSamples(boolean v){
        useOffscreenSamples(v?1:-1);
    }

    public void useOffscreenSamples(int samples){
        this.offscreenSamples=samples;
    }

    protected FrameBuffer render() {

         Camera cam=renderManager.getCurrentCamera();


        int width=viewPort.getCamera().getWidth();
        int height=viewPort.getCamera().getHeight();
    

        Texture depth=null;
        if(!is2D) depth=EffekseerUtils.copyDepthFromFrameBuffer(renderManager,sceneBuffer,width,height);
        
        sceneBuffer=getRenderTarget(sceneBuffer,offscreenSamples,sRGB);

         FrameBuffer oldFb=EffekseerUtils.bindFrameBuffer(renderManager,sceneBuffer);

        Effekseer.beginRender(viewPort.getScenes());
        Effekseer.render(renderManager.getRenderer(),cam,sceneBuffer,depth,particlesHardness,particlesContrast,is2D);
        Effekseer.endRender();

        EffekseerUtils.bindFrameBuffer(renderManager,oldFb);

        return sceneBuffer;
    }
}