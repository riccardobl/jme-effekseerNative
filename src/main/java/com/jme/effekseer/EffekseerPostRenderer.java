package com.jme.effekseer;

import java.lang.reflect.Field;
import java.util.EnumSet;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.post.Filter;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.Caps;
import com.jme3.renderer.RenderContext;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.RendererException;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.opengl.GL;
import com.jme3.renderer.opengl.GLFbo;
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
    protected FrameBuffer depthTarget;
    private Field linearizeSrgbImageF;

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

    private RenderContext renderContext;
    private GLFbo glfbo;

    protected void initGl(RenderManager rm) {
        GLRenderer renderer=(GLRenderer)rm.getRenderer();
        try{
            if(renderContext == null || glfbo == null){
                for(Field f:renderer.getClass().getDeclaredFields()){
                    Class t=f.getType();
                    if(RenderContext.class.isAssignableFrom(t)){
                        f.setAccessible(true);
                        renderContext=(RenderContext)f.get(renderer);
                    }else if(GLFbo.class.isAssignableFrom(t)){
                        f.setAccessible(true);
                        glfbo=(GLFbo)f.get(renderer);
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    protected FrameBuffer bindFrameBuffer(RenderManager rm, FrameBuffer fb) {
        initGl(rm);
        FrameBuffer cfb=renderContext.boundFB;
        rm.getRenderer().setFrameBuffer(fb);
        return cfb;
    }

    protected void blitFrameBuffer(RenderManager rm, FrameBuffer src, FrameBuffer dst, boolean copyColor, boolean copyDepth) {
        initGl(rm);
        EnumSet<Caps> caps=rm.getRenderer().getCaps();
        GLRenderer renderer=(GLRenderer)rm.getRenderer();

        Camera cam=rm.getCurrentCamera();
        int vpX=(int)(cam.getViewPortLeft() * cam.getWidth());
        int vpY=(int)(cam.getViewPortBottom() * cam.getHeight());
        int viewX2=(int)(cam.getViewPortRight() * cam.getWidth());
        int viewY2=(int)(cam.getViewPortTop() * cam.getHeight());
        int vpW=viewX2 - vpX;
        int vpH=viewY2 - vpY;

        if(caps.contains(Caps.FrameBufferBlit)){
            int srcX0=0;
            int srcY0=0;
            int srcX1;
            int srcY1;

            int dstX0=0;
            int dstY0=0;
            int dstX1;
            int dstY1;

            int prevFBO=renderContext.boundFBO;

            if(src != null && src.isUpdateNeeded()){
                renderer.updateFrameBuffer(src);
            }

            if(dst != null && dst.isUpdateNeeded()){
                renderer.updateFrameBuffer(dst);
            }

            if(src == null){
                glfbo.glBindFramebufferEXT(GLFbo.GL_READ_FRAMEBUFFER_EXT,0);
                srcX0=vpX;
                srcY0=vpY;
                srcX1=vpX + vpW;
                srcY1=vpY + vpH;
            }else{
                glfbo.glBindFramebufferEXT(GLFbo.GL_READ_FRAMEBUFFER_EXT,src.getId());
                srcX1=src.getWidth();
                srcY1=src.getHeight();
            }
            if(dst == null){
                glfbo.glBindFramebufferEXT(GLFbo.GL_DRAW_FRAMEBUFFER_EXT,0);
                dstX0=vpX;
                dstY0=vpY;
                dstX1=vpX + vpW;
                dstY1=vpY + vpH;
            }else{
                glfbo.glBindFramebufferEXT(GLFbo.GL_DRAW_FRAMEBUFFER_EXT,dst.getId());
                dstX1=dst.getWidth();
                dstY1=dst.getHeight();
            }
            int mask=0;
            if(copyColor){
                mask|=GL.GL_COLOR_BUFFER_BIT;
            }
            if(copyDepth){
                mask|=GL.GL_DEPTH_BUFFER_BIT;
            }
            glfbo.glBlitFramebufferEXT(srcX0,srcY0,srcX1,srcY1,dstX0,dstY0,dstX1,dstY1,mask,GL.GL_NEAREST);

            glfbo.glBindFramebufferEXT(GLFbo.GL_FRAMEBUFFER_EXT,prevFBO);
        }else{
            throw new RendererException("Framebuffer blitting not supported by the video hardware");
        }
    }

    protected FrameBuffer getDepthTarget(RenderManager renderManager, FrameBuffer in) {
        Format depthFormat=in.getDepthBuffer().getFormat();

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
        if(depthTarget == null || depthTarget.getWidth() != width || depthTarget.getHeight() != height){
            System.out.println("Create depth target " + in.getWidth() + "x" + in.getHeight());
            if(depthTarget != null) depthTarget.dispose();
            depthTarget=new FrameBuffer(width,height,1);
            depthTarget.setDepthTexture(new Texture2D(width,height,depthFormat));
        }
        blitFrameBuffer(renderManager,in,depthTarget,false,true);
        return depthTarget;
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
        FrameBuffer ofb=bindFrameBuffer(renderManager,renderTarget);
        renderManager.getRenderer().clearBuffers(true,true,true);
        bindFrameBuffer(renderManager,ofb);

        return renderTarget;
    }


    @Override
    protected void postFrame(RenderManager renderManager, ViewPort viewPort, FrameBuffer prevFilterBuffer, FrameBuffer sceneBuffer) {
        assert sceneBuffer != null;

        Camera cam=renderManager.getCurrentCamera();

        Texture depth=null;
        if(!is2D){
            FrameBuffer dest=getDepthTarget(renderManager,sceneBuffer);
            depth=dest.getDepthBuffer().getTexture();
        }

        
        boolean linearizeSrgbImages=getLinearizeSrgbImages(renderManager);      
        sceneBuffer=getRenderTarget(renderManager,sceneBuffer,is2D);
        
        FrameBuffer oldFb=bindFrameBuffer(renderManager,sceneBuffer);
 
        Effekseer.beginScene(viewPort.getScenes());
        Effekseer.update(tpf);
        Effekseer.render(renderManager.getRenderer(), cam,sceneBuffer,depth,is2D ,linearizeSrgbImages );
        Effekseer.endScene();        
       
        bindFrameBuffer(renderManager,oldFb);

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