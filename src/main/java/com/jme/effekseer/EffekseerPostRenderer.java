package com.jme.effekseer;

import java.lang.reflect.Field;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.post.Filter;
import com.jme3.post.FilterPostProcessor;
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
    protected Boolean needsDepth=null;
    protected Boolean is2D=null;

    public EffekseerPostRenderer(AssetManager manager,Boolean withDepth){
        this(manager,withDepth,null);
    }
    public EffekseerPostRenderer(AssetManager manager,Boolean withDepth,Boolean is2D){
        Effekseer.init(manager);
        this.needsDepth=withDepth;
        this.is2D=is2D;
    }
    public EffekseerPostRenderer(AssetManager manager){
        this(manager,null);
    }
    

    public void setAsync(int nThreads){
        Effekseer.setAsync(nThreads);
    }

    protected void preInit(ViewPort vp){
        boolean detectGui=false;
        if(needsDepth==null){
            needsDepth=true;
            detectGui=true;
        }
        if(is2D==null){
            is2D=false;
            detectGui=true;
        }
        if(detectGui&&vp!=null){
            if(vp.getName().equals("Gui Default")){ // Detect when attached to default guiViewPort in jme.
                is2D=true;
                needsDepth=false;
                System.out.println("Detected default Gui View Port");
            }
        }
    }


    protected void setProcessor(FilterPostProcessor proc) {
        super.setProcessor(proc);
        Field fields[]=proc.getClass().getDeclaredFields();        
        ViewPort vp=null;
        try{
            for(Field f:fields){
                if(ViewPort.class.isAssignableFrom(f.getType())){
                    f.setAccessible(true);
                    vp=(ViewPort)f.get(proc);
                    System.out.println("Found viewport "+vp!=null?vp.getName():vp);
                    break;
                }
            }
        }catch(Exception e){
            System.err.println(e);
        }
        if(vp!=null){
            preInit(vp);            
        }
    }

    @Override
    protected void initFilter(AssetManager manager, RenderManager renderManager, ViewPort vp, int w, int h) {
        assert manager!=null;
        this.material= new Material(manager,"Effekseer/Composer.j3md");
        preInit(vp);
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
        Effekseer.render(renderManager.getRenderer(), cam, getRenderTarget(cam.getWidth(),cam.getHeight(),sceneBuffer.getSamples()),
            isRequiresDepthTexture()?sceneBuffer.getDepthBuffer() .getTexture():null,is2D
        );
        Effekseer.endScene();        
    }

    @Override
    protected boolean isRequiresDepthTexture() {
        preInit(null);
        return needsDepth;
    }


    @Override
    protected Material getMaterial() {
        return material;
    }
    
}