package com.jme.effekseer;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.post.Filter;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Texture2D;
import com.jme3.texture.Image.Format;

/**
 * EffekseerPostRenderer does all the rendering stuff for you, just add it to your FilterPostProcessor and it will just work.
 * @author Riccardo Balbo
 */
public class EffekseerPostRenderer extends Filter{
    protected FrameBuffer renderTarget;
    protected Texture2D particlesColor,particlesDepth;
    protected float tpf;

    public EffekseerPostRenderer(AssetManager manager){
        Effekseer.init(manager);
    }
    

    public void setAsync(int nThreads){
        Effekseer.setAsync(nThreads);
    }


    @Override
    protected void initFilter(AssetManager manager, RenderManager renderManager, ViewPort vp, int w, int h) {
        assert manager!=null;
        this.material= new Material(manager,"Effekseer/Composer.j3md");
    }
    
    @Override
    protected void preFrame(float tpf) {
        this.tpf=tpf;
    }

    protected FrameBuffer getRenderTarget(int width,int height,int samples){
        if(
            particlesColor==null||
            particlesColor.getImage().getMultiSamples()!=samples||
            particlesColor.getImage().getWidth()!=width||
            particlesColor.getImage().getHeight()!=height
        ){
            if(particlesColor!=null){
                particlesColor.getImage().dispose();
                particlesDepth.getImage().dispose();
            }
            if(renderTarget!=null){
                renderTarget.dispose();
            }
            assert samples<=1;
            particlesColor=new Texture2D(width,height,samples,Format.RGBA16F);
            particlesDepth=new Texture2D(width,height,samples,Format.Depth);
            renderTarget=new FrameBuffer(width,height,samples);
            renderTarget.addColorTexture(particlesColor);
            renderTarget.setDepthTexture(particlesDepth);
            this.material.setTexture("ParticlesColor", particlesColor);
        }
        return renderTarget;
    }

    @Override
    protected void postFrame(RenderManager renderManager, ViewPort viewPort, FrameBuffer prevFilterBuffer, FrameBuffer sceneBuffer) {
        Camera cam=renderManager.getCurrentCamera();
        Effekseer.beginScene(viewPort.getScenes());
        Effekseer.update(tpf);
        Effekseer.render(renderManager.getRenderer(), cam, getRenderTarget(cam.getWidth(),cam.getHeight(),sceneBuffer.getSamples()),sceneBuffer.getDepthBuffer() .getTexture());
        Effekseer.endScene();        
    }

    @Override
    protected boolean isRequiresDepthTexture() {
        return true;
    }


    @Override
    protected Material getMaterial() {
        return material;
    }
    
}