package com.jme.effekseer.jme3;

import com.jme.effekseer.EffekseerUtils;
import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.jme3.post.SceneProcessor;
import com.jme3.profile.AppProfiler;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Texture2D;
import com.jme3.texture.Image.Format;
import com.jme3.texture.image.ColorSpace;

public class EffekseerProcessor implements SceneProcessor{
    protected EffekseerRenderer renderer;

    protected boolean initialized;
    protected RenderManager renderManager;
    protected ViewPort vp;
    protected boolean sRGB,isOrthographic,hasDepth;

    public EffekseerProcessor( AssetManager manager, boolean sRGB,boolean isOrthographic,boolean hasDepth){
        renderer=new EffekseerRenderer(manager,sRGB);
        renderer.setOrthographic(isOrthographic);
        this.sRGB=sRGB;
        this.isOrthographic=isOrthographic;
        this.hasDepth=hasDepth;
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


    FrameBuffer renderTarget;
    protected FrameBuffer getRenderTarget(ViewPort viewPort,  FrameBuffer in, boolean sRGB) {
        int width=viewPort.getCamera().getWidth();
        int height=viewPort.getCamera().getHeight();

        Format depthFormat=Format.Depth;
        Format colorFormat=Format.RGB8;

        if(renderTarget == null || renderTarget.getWidth() != width || renderTarget.getHeight() != height || renderTarget.isSrgb()!=sRGB){
            System.out.println("Create render target " + width  + "x" + height);
            if(renderTarget != null) renderTarget.dispose();
            renderTarget=new FrameBuffer(width,height,1);
            renderTarget.setDepthTexture(new Texture2D(width,height,1,depthFormat));
            renderTarget.setColorTexture(new Texture2D(width,height,1,colorFormat));
            if(sRGB){                
                System.out.println("Enable sRGB");
                renderTarget.getColorBuffer().getTexture().getImage().setColorSpace(ColorSpace.sRGB);
                renderTarget.setSrgb(sRGB);
            }
        }

        assert renderTarget != null;

        FrameBuffer ofb=EffekseerUtils.bindFrameBuffer(renderManager,renderTarget);
        EffekseerUtils.clearFrameBuffer(renderManager,renderTarget,false,!hasDepth,true,ColorRGBA.BlackNoAlpha);
        EffekseerUtils.blitFrameBuffer(renderManager,in,renderTarget,true,hasDepth,renderTarget.getWidth(),renderTarget.getHeight());
        EffekseerUtils.bindFrameBuffer(renderManager,ofb);
        return renderTarget;
    }


    @Override
    public void postFrame(FrameBuffer out) {       
        FrameBuffer w=getRenderTarget(this.vp, out,  sRGB);
        renderer.setSceneBuffer(w);    
        renderer.render();    
        EffekseerUtils.blitFrameBuffer(renderManager, w, out, true, hasDepth,vp.getCamera().getWidth(),vp.getCamera().getHeight());
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void setProfiler(AppProfiler profiler) {

    }
    
}