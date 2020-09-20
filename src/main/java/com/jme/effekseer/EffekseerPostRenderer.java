package com.jme.effekseer;

import java.lang.reflect.Field;
import java.util.EnumSet;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.post.Filter;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.Caps;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.opengl.GLRenderer;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;

/**
 * EffekseerPostRenderer does all the rendering stuff for you, just add it to your FilterPostProcessor and it will just work.
 * @author Riccardo Balbo
 */
public class EffekseerPostRenderer extends Filter{
    protected FrameBuffer renderTarget;
    protected float tpf;
    protected Boolean is2D=null;
    private Field linearizeSrgbImageF;
    private float particlesHardness=0.1f;
    private float particlesContrast=2f;
    public EffekseerPostRenderer(AssetManager manager,Boolean is2D){
        Effekseer.init(manager);
        this.is2D=is2D;
    }

    public EffekseerPostRenderer(AssetManager manager){
        this(manager,null);
    }

    public void setAsync(int nThreads) {
        Effekseer.setAsync(nThreads);
    }

    public void setSoftParticles(float hardness,float contrast){
        particlesHardness=hardness;
        particlesContrast=contrast;
    }

    public void setHardParticles(){
        particlesHardness=1000;
        particlesContrast=1f;
    }

    protected void preInit(ViewPort vp) {
        boolean detectGui=false;

        if(is2D == null){
            is2D=false;
            detectGui=true;
        }
        if(detectGui && vp != null){
            if(vp.getName().equals("Gui Default")){ // Detect when attached to default guiViewPort in jme.
                is2D=true;
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
                    System.out.println("Found viewport " + vp != null?vp.getName():vp);
                    break;
                }
            }
        }catch(Exception e){
            System.err.println(e);
        }
        if(vp != null){
            preInit(vp);
        }
    }

    @Override
    protected void initFilter(AssetManager manager, RenderManager renderManager, ViewPort vp, int w, int h) {
        assert manager != null;
        this.material=new Material(manager,"Effekseer/Composer.j3md");
        preInit(vp);
        
        try{
            linearizeSrgbImageF=GLRenderer.class.getDeclaredField("linearizeSrgbImages");
            linearizeSrgbImageF.setAccessible(true);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    protected boolean getLinearizeSrgbImages(RenderManager rm){
        GLRenderer r=(GLRenderer)rm.getRenderer();
        if(linearizeSrgbImageF != null){
            try{
                return (boolean)linearizeSrgbImageF.get(r);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    protected void preFrame(float tpf) {
        this.tpf=tpf;
    }





   

    protected FrameBuffer getRenderTarget(RenderManager renderManager, FrameBuffer in, boolean is2D) {
        if(!is2D) return in;

        EnumSet<Caps> caps=renderManager.getRenderer().getCaps();

        Format depthFormat=Format.Depth;
        if(caps.contains(Caps.FloatDepthBuffer)){
            depthFormat=Format.Depth32F;
        }

        Format colorFormat=Format.RGBA8;
        if(caps.contains(Caps.FloatTexture)){
            colorFormat=Format.RGBA16F;
        }

        int width;
        int height;
        if(in != null){
            width=in.getWidth();
            height=in.getHeight();
        }else{
            Camera cam=renderManager.getCurrentCamera();
            width=cam.getWidth();
            height=cam.getHeight();
        }
        if(renderTarget == null || renderTarget.getWidth() != width || renderTarget.getHeight() != height){
            System.out.println("Create render target " + in.getWidth() + "x" + in.getHeight());
            if(renderTarget != null) renderTarget.dispose();
            renderTarget=new FrameBuffer(width,height,1);
            renderTarget.setDepthTexture(new Texture2D(width,height,depthFormat));
            renderTarget.setColorTexture(new Texture2D(width,height,colorFormat));
            this.material.setTexture("BlendTexture",renderTarget.getColorBuffer().getTexture());
        }
        FrameBuffer ofb=EffekseerUtils.bindFrameBuffer(renderManager,renderTarget);
        renderManager.getRenderer().clearBuffers(true,true,true);
        EffekseerUtils.bindFrameBuffer(renderManager,ofb);

        return renderTarget;
    }


    @Override
    protected void postFrame(RenderManager renderManager, ViewPort viewPort, FrameBuffer prevFilterBuffer, FrameBuffer sceneBuffer) {
        assert sceneBuffer != null;

        Camera cam=renderManager.getCurrentCamera();

        Texture depth=null;
        if(!is2D){
            depth=EffekseerUtils.copyDepthFromFrameBuffer(renderManager,sceneBuffer);
        }
        
        boolean linearizeSrgbImages=getLinearizeSrgbImages(renderManager);      
        sceneBuffer=getRenderTarget(renderManager,sceneBuffer,is2D);
        
        FrameBuffer oldFb=EffekseerUtils.bindFrameBuffer(renderManager,sceneBuffer);
 
        Effekseer.beginScene(viewPort.getScenes());
        Effekseer.update(tpf);

        Effekseer.render(renderManager.getRenderer(), cam,sceneBuffer,depth,particlesHardness,particlesContrast,is2D ,linearizeSrgbImages );
        Effekseer.endScene();        
       
        EffekseerUtils.bindFrameBuffer(renderManager,oldFb);

    }

    @Override
    protected boolean isRequiresDepthTexture() {
        return false;
    }


    @Override
    protected Material getMaterial() {
        return material;
    }
    
}