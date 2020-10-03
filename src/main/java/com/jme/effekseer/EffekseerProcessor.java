package com.jme.effekseer;

import com.jme3.asset.AssetManager;
import com.jme3.post.SceneProcessor;
import com.jme3.profile.AppProfiler;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.texture.FrameBuffer;

public class EffekseerProcessor implements SceneProcessor{
    protected EffekseerRenderer renderer;

    protected boolean initialized,offscreen;
    protected RenderManager renderManager;
    protected ViewPort vp;

    public EffekseerProcessor( AssetManager manager, boolean sRGB,boolean is2D,boolean offscreen){
        renderer=new EffekseerRenderer(manager,sRGB);
        this.offscreen=offscreen;
        renderer.set2D(is2D);
        renderer.useOffscreenSamples(offscreen?1:-1);
    }



    public EffekseerRenderer getRenderer(){
        return renderer;
    }

    @Override
    public void initialize(RenderManager rm, ViewPort vp) {
        initialized=true;
        renderManager=rm;
        renderer.setRenderManager(rm);
        renderer.setViewPort(vp);
        this.vp=vp;

    }

    @Override
    public void reshape(ViewPort vp, int w, int h) {

    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }




    @Override
    public void preFrame(float tpf) {
        renderer.setTpf(tpf);

    }

    @Override
    public void postQueue(RenderQueue rq) {

    }

    @Override
    public void postFrame(FrameBuffer out) {
        renderer.setSceneBuffer(out);        
        FrameBuffer fb=renderer.render();
        if(offscreen){
            assert fb!=null;
            EffekseerUtils.blitFrameBuffer(renderManager, fb, out, true, false,vp.getCamera().getWidth(),vp.getCamera().getHeight());
        }
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void setProfiler(AppProfiler profiler) {

    }
    
}