package com.jme.effekseer;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.Map;
import java.util.WeakHashMap;

import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Caps;
import com.jme3.renderer.RenderContext;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.RendererException;
import com.jme3.renderer.opengl.GL;
import com.jme3.renderer.opengl.GLFbo;
import com.jme3.renderer.opengl.GLRenderer;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;

public class EffekseerUtils{

    private static class State{
        private RenderContext renderContext;
        private GLFbo glfbo;
        private Map<FrameBuffer,FrameBuffer> copyBuffer=new WeakHashMap<FrameBuffer,FrameBuffer>();
        private final ColorRGBA tmpColor=new ColorRGBA();
    }

    private static ThreadLocal<State> stateTl=new ThreadLocal<State>(){
        @Override
        protected State initialValue() {

            return new State();
        }
    };



    private static State  getState(RenderManager rm) {
        if(rm.getRenderer() ==null||!(rm.getRenderer() instanceof GLRenderer)){
            throw new RendererException("Only GLRenderer renderer supported");
        }
        State state=stateTl.get();
        GLRenderer renderer=(GLRenderer)rm.getRenderer();
        try{
            if(state.renderContext == null || state.glfbo == null){
                for(Field f:renderer.getClass().getDeclaredFields()){
                    Class t=f.getType();
                    if(RenderContext.class.isAssignableFrom(t)){
                        f.setAccessible(true);
                        state.renderContext=(RenderContext)f.get(renderer);
                    }else if(GLFbo.class.isAssignableFrom(t)){
                        f.setAccessible(true);
                        state.glfbo=(GLFbo)f.get(renderer);
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return state;
    }

    public static void blitFrameBuffer(RenderManager rm, FrameBuffer src, FrameBuffer dst, boolean copyColor, boolean copyDepth) {
        State state=getState(rm);
        EnumSet<Caps> caps=rm.getRenderer().getCaps();
        GLRenderer renderer=(GLRenderer)rm.getRenderer();

        // Camera cam=rm.getCurrentCamera();
        // int vpX=(int)(cam.getViewPortLeft() * cam.getWidth());
        // int vpY=(int)(cam.getViewPortBottom() * cam.getHeight());
        // int viewX2=(int)(cam.getViewPortRight() * cam.getWidth());
        // int viewY2=(int)(cam.getViewPortTop() * cam.getHeight());
        // int vpW=viewX2 - vpX;
        // int vpH=viewY2 - vpY;

        int vpX=0;
        int vpY=0;
        int vpW;
        int vpH;
        if(src!=null){
            vpW=src.getWidth();
            vpH=src.getHeight();
        }else{
            vpW=dst.getWidth();
            vpH=dst.getHeight();
        }

        if(caps.contains(Caps.FrameBufferBlit)){
            int srcX0=0;
            int srcY0=0;
            int srcX1;
            int srcY1;

            int dstX0=0;
            int dstY0=0;
            int dstX1;
            int dstY1;

            int prevFBO=state.renderContext.boundFBO;

            if(src != null && src.isUpdateNeeded()){
                renderer.updateFrameBuffer(src);
            }

            if(dst != null && dst.isUpdateNeeded()){
                renderer.updateFrameBuffer(dst);
            }

            if(src == null){
                state.glfbo.glBindFramebufferEXT(GLFbo.GL_READ_FRAMEBUFFER_EXT,0);
                srcX0=vpX;
                srcY0=vpY;
                srcX1=vpX + vpW;
                srcY1=vpY + vpH;
            }else{
                state.glfbo.glBindFramebufferEXT(GLFbo.GL_READ_FRAMEBUFFER_EXT,src.getId());
                srcX1=src.getWidth();
                srcY1=src.getHeight();
            }
            if(dst == null){
                state.glfbo.glBindFramebufferEXT(GLFbo.GL_DRAW_FRAMEBUFFER_EXT,0);
                dstX0=vpX;
                dstY0=vpY;
                dstX1=vpX + vpW;
                dstY1=vpY + vpH;
            }else{
                state. glfbo.glBindFramebufferEXT(GLFbo.GL_DRAW_FRAMEBUFFER_EXT,dst.getId());
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
            state. glfbo.glBlitFramebufferEXT(srcX0,srcY0,srcX1,srcY1,dstX0,dstY0,dstX1,dstY1,mask,GL.GL_NEAREST);

            state.glfbo.glBindFramebufferEXT(GLFbo.GL_FRAMEBUFFER_EXT,prevFBO);
        }else{
            throw new RendererException("Framebuffer blitting not supported by the video hardware");
        }
    }




    public static void clearFrameBuffer(RenderManager rm, FrameBuffer fb,boolean color,boolean depth,boolean stencil,ColorRGBA bgColor) {
        State state=getState(rm);
        FrameBuffer ofb=bindFrameBuffer(rm,fb);
        state.tmpColor.set(state.renderContext.clearColor);
        rm.getRenderer().setBackgroundColor(bgColor);
        rm.getRenderer().clearBuffers(color,depth,stencil);    
        rm.getRenderer().setBackgroundColor(state.tmpColor);        
        bindFrameBuffer(rm,ofb);        
    }

    public static FrameBuffer bindFrameBuffer(RenderManager rm, FrameBuffer fb) {
        State state=getState(rm);
        FrameBuffer cfb=state.renderContext.boundFB;
        if(cfb!=fb) rm.getRenderer().setFrameBuffer(fb);
        return cfb;
    }


    public static Texture copyDepthFromFrameBuffer(RenderManager renderManager, FrameBuffer in) {
        assert in!=null;
        State state=getState(renderManager);

        Format depthFormat=in.getDepthBuffer().getFormat();

        int width=in.getWidth();
        int height=in.getHeight();

        FrameBuffer depthTarget=state.copyBuffer.get(in);

        if(depthTarget == null || depthTarget.getWidth() != width || depthTarget.getHeight() != height || depthTarget.getDepthBuffer().getFormat() != depthFormat){
            System.out.println("Create depth target " + in.getWidth() + "x" + in.getHeight());
            if(depthTarget != null) depthTarget.dispose();
            depthTarget=new FrameBuffer(width,height,1);
            depthTarget.setDepthTexture(new Texture2D(width,height,depthFormat));
            state.copyBuffer.put(in,depthTarget);
        }
        EffekseerUtils.blitFrameBuffer(renderManager,in,depthTarget,false,true);
        return depthTarget.getDepthBuffer().getTexture();

    }

}