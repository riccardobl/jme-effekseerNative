package com.jme.effekseer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.WeakHashMap;

import com.jme.effekseer.driver.EffekseerEmissionDriverGeneric;
import com.jme.effekseer.driver.fun.impl.EffekseerPointFollowingSpatialShape;
import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Caps;
import com.jme3.renderer.RenderContext;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.RendererException;
import com.jme3.renderer.opengl.GL;
import com.jme3.renderer.opengl.GLFbo;
import com.jme3.renderer.opengl.GLRenderer;
import com.jme3.scene.Spatial;
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
        blitFrameBuffer(rm, src, dst, copyColor, copyDepth,src.getWidth(),src.getHeight());
    }

    public static void blitFrameBuffer(RenderManager rm, FrameBuffer src, FrameBuffer dst, boolean copyColor, boolean copyDepth,int width,int height) {
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
            vpW=width;
            vpH=height;
        }else{
            vpW=width;
            vpH=height;
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
                srcX1=width;
                srcY1=height;
            }
            if(dst == null){
                state.glfbo.glBindFramebufferEXT(GLFbo.GL_DRAW_FRAMEBUFFER_EXT,0);
                dstX0=vpX;
                dstY0=vpY;
                dstX1=vpX + vpW;
                dstY1=vpY + vpH;
            }else{
                state. glfbo.glBindFramebufferEXT(GLFbo.GL_DRAW_FRAMEBUFFER_EXT,dst.getId());
                dstX1=width;
                dstY1=height;
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


    public static Texture copyDepthFromFrameBuffer(RenderManager renderManager, FrameBuffer in,int width,int height) {
        // assert in!=null;
        State state=getState(renderManager);

        Format depthFormat=in!=null?in.getDepthBuffer().getFormat():Format.Depth;

        // int width=in.getWidth();
        // int height=in.getHeight();

        FrameBuffer depthTarget=state.copyBuffer.get(in);

        if(depthTarget == null || depthTarget.getWidth() != width || depthTarget.getHeight() != height || depthTarget.getDepthBuffer().getFormat() != depthFormat){
            System.out.println("Create depth target " + width + "x" + height);
            if(depthTarget != null) depthTarget.dispose();
            depthTarget=new FrameBuffer(width,height,1);
            depthTarget.setDepthTexture(new Texture2D(width,height,depthFormat));
            state.copyBuffer.put(in,depthTarget);
        }
        EffekseerUtils.blitFrameBuffer(renderManager,in,depthTarget,false,true,width,height);
        return depthTarget.getDepthBuffer().getTexture();

    }



    private static Number getNumberData(Spatial sx,String k,Number def){
        Object o=sx.getUserData(k);
        if(o==null)return def;
        if(o instanceof Number)return (Number)o;
        return Float.parseFloat(o.toString());
    }

    private static Boolean getBooleanData(Spatial sx,String k,Boolean def){
        Object o=sx.getUserData(k);
        if(o==null)return def;
        if(o instanceof Boolean)return (Boolean)o;        
        if(o instanceof Number)  return ((Number)o).intValue()==1;        
        String v=o.toString().toLowerCase();
        return v.equals("true")||v.equals("1");        
    }
    



    static byte[] readAll(InputStream is) throws IOException {
        byte chunk[]=new byte[1024*1024];
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        int read;
        while((read=is.read(chunk)) != -1) bos.write(chunk,0,read);
        return bos.toByteArray();
    }

    private static String normalizePath(String ...parts){
        String path="";

        for(String part:parts){
            path+="/"+part.replace("\\", "/");
        }

        path=path.replace("/",File.separator);
        path=Paths.get(path).normalize().toString();
        path=path.replace("\\", "/");

        
        int sStr=0;
        while(path.startsWith("../",sStr))sStr+=3;
        path=path.substring(sStr);

        if(path.startsWith("/"))path=path.substring(1);

        return path;
    }

    private static Collection<String> guessPossibleRelPaths(String root,String opath){
        root=normalizePath(root);

        String path=opath;
        path=normalizePath(path);       
        
        System.out.println("Guesses for "+opath+" normalized "+path+" in root "+root);

        ArrayList<String> paths=new ArrayList<String>();   

        paths.add(path);
        paths.add(normalizePath(root,path));

        ArrayList<String> pathsNoRoot=new ArrayList<String>();   

        while(true){
            int i=path.indexOf("/");
            if(i==-1)break;
            path=path.substring(i+1);
            if(path.isEmpty())break;
            pathsNoRoot.add(path);
            paths.add(normalizePath(root,path));
        }

        paths.addAll(pathsNoRoot);


        for(String p:paths){
            System.out.println(" > "+p);
        }

        return paths;
    }

    static InputStream openStream(AssetManager am, String rootPath,String path) {
        AssetInfo info=null;
        Collection<String> guessedPaths=guessPossibleRelPaths(rootPath,path);
        for(String p:guessedPaths){
            try{
                System.out.println("Try to locate assets "+Paths.get(path).getFileName()+" in "+p);
                info=am.locateAsset(new AssetKey(p));
                if(info!=null){
                    System.out.println("Found in "+p);
                    break;
                }
            }catch(AssetNotFoundException e){
                System.out.println("Not found in "+p);
            }
        }
        if(info==null)throw new AssetNotFoundException(path);
        return info.openStream();
    }



    public static Collection<EffekseerEmitterControl>  loadFromScene(AssetManager am,Spatial scene){
        Collection<EffekseerEmitterControl> emitters=new ArrayList<EffekseerEmitterControl>();
        scene.depthFirstTraversal(sx->{
            if(sx.getUserData("_effekseer")!=null){
                String path=sx.getUserData("_effekseer_path");     
                if(path!=null){
                    Collection<String> paths=guessPossibleRelPaths("", path) ;
                    boolean found=false;
                    for(String p:paths){
                        float scale=getNumberData(sx,"_effekseer_scale",1f).floatValue();
                        boolean ignorerot=getBooleanData(sx,"_effekseer_ignorerot",false);
                        boolean enabled=getBooleanData(sx,"_effekseer_enabled",true);
                        try{
                            EffekseerEmitterControl effekt=(EffekseerEmitterControl)am.loadAsset(p);
                            if(effekt==null)throw new AssetNotFoundException(p);
                            effekt.setScale(scale);
                            effekt.setEnabled(enabled);
                            effekt.setDriver(new EffekseerEmissionDriverGeneric().shape(
                                new EffekseerPointFollowingSpatialShape().ignoreRot(ignorerot)
                            ));
                            sx.addControl(effekt);
                            emitters.add(effekt);
                            found=true;
                            break;
                        }catch(AssetNotFoundException e){
                            System.out.println("Asset not found"+e+"try another path");
                        }
                    }
                    if(!found){
                        throw new AssetNotFoundException(path);
                    }
                }
            }
        });
        return emitters;
    }

}