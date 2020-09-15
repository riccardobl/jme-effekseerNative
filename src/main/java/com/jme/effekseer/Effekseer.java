package com.jme.effekseer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Map.Entry;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLoadException;
import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix4f;
import com.jme3.math.Transform;
import com.jme3.math.Vector2f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.Caps;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.RendererException;
import com.jme3.renderer.opengl.GLRenderer;
import com.jme3.scene.Spatial;
import com.jme3.system.NativeLibraryLoader;
import com.jme3.system.Platform;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ColorSpace;
import com.jme3.util.BufferUtils;


import Effekseer.swig.EffekseerBackendCore;
import Effekseer.swig.EffekseerEffectCore;
import Effekseer.swig.EffekseerManagerCore;
import Effekseer.swig.EffekseerTextureType;
/**
 * Effekseer wrapper. This should not be used directly, unless you have to. EffekseerPostRenderer should be used for general usage.
 * Note: 
 *      This implementation uses only one instance of Effekseer runtime per thread. 
 *      All the methods in this class will create and use the instance local to the caller thread and they should 
 *      always be called from the same thread, generally from jME main thread.
 * @author Riccardo Balbo
 */
public class Effekseer{

    static{
        NativeLibraryLoader.registerNativeLibrary("effekseer",Platform.Linux64,"native/linux/x86_64/libEffekseerNativeForJava.so");
        NativeLibraryLoader.registerNativeLibrary("effekseer",Platform.Windows64,"native/windows/x86_64/EffekseerNativeForJava.dll");
        NativeLibraryLoader.loadNativeLibrary("effekseer",true);
    }

    private static class EmitterState{
        boolean oldVisibleFlag;
    }

    private static class State{
        EffekseerManagerCore core;
        Collection<Spatial> currentSceneParents;
        AssetManager am;
        final float v16[]=new float[16];
        final Matrix4f m4=new Matrix4f();
        final Map<EffekseerEmitterControl,EmitterState> emitters=new WeakHashMap<EffekseerEmitterControl,EmitterState>();
        final List<Spatial> v1SpatialList=new ArrayList<Spatial>(1);

        boolean hasDepth;
        Texture2D sceneData,fakeDepth;
        final Vector2f frustumNearFar=new Vector2f();
        final Vector2f resolution=new Vector2f();
        float particlesHardness,particlesContrast;
    }

    private static ThreadLocal<State> state=new ThreadLocal<State>(){
        @Override
        protected State initialValue() {
            EffekseerBackendCore.InitializeAsOpenGL();
            State state=new State();
            state.core=new EffekseerManagerCore();
            state.core.Initialize(8000);
            return state;
        }
    };


    private static State getState() {
        return state.get();
    }

    public static void setAsync(int threads) {
        State state=getState();
        state.core.LaunchWorkerThreads(threads);
    }

    /**
     * Init and return an effekseer instance
     */
    public static State init(AssetManager am) {
        State state=getState();
        if(state.am != am){
            if(state.am != null){
                state.am.unregisterLoader(EffekseerLoader.class);
            }
            state.am=am;
            am.registerLoader(EffekseerLoader.class,"efkefc");
        }
        return state;
    }

    /**
     * Destroy the effekseer instance
     */
    public static void destroy() {
        State s=getState();
        if(s.am != null){
            s.am.unregisterLoader(EffekseerLoader.class);
            s.am=null;
        }

        s.core.delete();
        EffekseerBackendCore.Terminate();
        state.remove();
    }


    /**
     * Update the effekseer instance
     * @param tpf time in seconds 
     */
    public static void update(float tpf) {
        State state=getState();
        float t=tpf / (1.0f / 60.0f);
        state.core.Update(t);
    }

    private static Texture2D getSceneData(GLRenderer renderer, Camera cam, float particlesHardness, float particlesContrast,boolean hasDepth) {
        State state=getState();

        boolean rebuildSceneData=false;
        if(state.sceneData == null ||state.hasDepth!=hasDepth ||state.frustumNearFar.x != cam.getFrustumNear() || state.frustumNearFar.y != cam.getFrustumFar() || state.resolution.x != cam.getWidth() || state.resolution.y != cam.getHeight() || state.particlesHardness != particlesHardness || state.particlesContrast != particlesContrast){
            rebuildSceneData=true;
        }

        if(rebuildSceneData){
            state.frustumNearFar.x=cam.getFrustumNear();
            state.frustumNearFar.y=cam.getFrustumFar();
            state.resolution.x=cam.getWidth();
            state.resolution.y=cam.getHeight();
            state.particlesHardness=particlesHardness;
            state.particlesContrast=particlesContrast;
            state.hasDepth=hasDepth;

            if(state.sceneData == null){
                Image img=null;
                if(renderer.getCaps().contains(Caps.FloatTexture)){
                    ByteBuffer data=BufferUtils.createByteBuffer(2/*w*/ * 1 /*h*/ * 3 /*components*/ * 4 /*B x component*/ );
                    img=new Image(Format.RGB32F,2,1,data,ColorSpace.Linear);
                }else{
                    ByteBuffer data=BufferUtils.createByteBuffer(2/*w*/ * 1 /*h*/ * 3 /*components*/ * 2 /*B x component*/ );
                    img=new Image(Format.RGB16F_to_RGB111110F,2,1,data,ColorSpace.Linear);
                }
                state.sceneData=new Texture2D(img);
            }

            if(renderer.getCaps().contains(Caps.FloatTexture)){
                ByteBuffer data=state.sceneData.getImage().getData(0);
                data.rewind();
                data.putFloat(state.resolution.x);
                data.putFloat(state.resolution.y);
                data.putFloat(particlesHardness);
                data.putFloat(state.hasDepth?state.frustumNearFar.x:-1);
                data.putFloat(state.hasDepth?state.frustumNearFar.y:-1);
                data.putFloat(particlesContrast);
                data.rewind();
            }else if(renderer.getCaps().contains(Caps.PackedFloatTexture)){
                ByteBuffer data=state.sceneData.getImage().getData(0);
                data.rewind();
                data.putShort(FastMath.convertFloatToHalf(state.resolution.x));
                data.putShort(FastMath.convertFloatToHalf(state.resolution.y));
                data.putShort(FastMath.convertFloatToHalf(particlesHardness));
                data.putShort(state.hasDepth?FastMath.convertFloatToHalf(state.frustumNearFar.x):-1);
                data.putShort(state.hasDepth?FastMath.convertFloatToHalf(state.frustumNearFar.y):-1);
                data.putShort(FastMath.convertFloatToHalf(particlesContrast));
                data.rewind();
            }else{
                throw new RendererException("Unsupported Platform. FloatTexture or PackedFloatTexture required for jme-effekseerNative.");
            }
            state.sceneData.getImage().setUpdateNeeded();
        }

        return state.sceneData;
    }


    private static Texture2D getFakeDepth() {
        State state=getState();
        if(state.fakeDepth == null){
            ByteBuffer data=BufferUtils.createByteBuffer(1/*w*/ * 1 /*h*/ * 1 /*components*/ * 1 /*B x component*/ );
            data.put((byte)255);
            data.rewind();
            Image img=new Image(Format.Luminance8,1,1,data,ColorSpace.Linear);
            state.fakeDepth=new Texture2D(img);
        }
        return state.fakeDepth;
    }

    public static void beginScene() {
        beginScene((Collection<Spatial>)null);
    }

    /**
     * Select a scene for rendering and update.
     * @param parent Only effects that are child of the parent spatial (or attached to the parent spatial itself) will be updated and rendered
     */
    public static void beginScene(Spatial parent) {
        State state=getState();
        state.v1SpatialList.set(0,parent);
        beginScene(state.v1SpatialList);
    }

    /**
     * Select a scene for rendering and update. Same as beginScene(Spatial) but accepts multiple parents.
     * @param parent Only effects that are child of the parent spatials (or attached to the parent spatials) will be updated and rendered
     */
    public static void beginScene(Collection<Spatial> parents) {
        State state=getState();
        state.currentSceneParents=parents;
        if(parents==null)return;        
        for(Entry<EffekseerEmitterControl,EmitterState> e:state.emitters.entrySet()){
            EffekseerEmitterControl emitter=e.getKey();
            EmitterState emitterState=e.getValue();            
            emitterState.oldVisibleFlag=emitter.isEnabled();
            boolean currentFlag= parents.stream().anyMatch(p->emitter.isChildOf(p));  
            if(emitter.isEnabled()!=currentFlag){
                emitter.setEnabled(currentFlag);
            }
        }
    }

    /**
     * This must be called after the rendering, to deselect the scene and reset temporary states.
     */
    public static void endScene(){
        State state=getState();
        if(state.currentSceneParents==null)return;
        for(Entry<EffekseerEmitterControl,EmitterState> e:state.emitters.entrySet()){
            EffekseerEmitterControl emitter=e.getKey();
            EmitterState emitterState=e.getValue();
            if(emitter.isEnabled()!=emitterState.oldVisibleFlag){
                emitter.setEnabled(emitterState.oldVisibleFlag);               
                emitterState.oldVisibleFlag=emitter.isEnabled();
            }
        }
    }

    public static void render(Renderer renderer,Camera cam,FrameBuffer renderTarget,Texture sceneDepth) {
        render( renderer, cam, renderTarget, sceneDepth,0.1f,2.0f,false) ;

    }

    /**
     * Render the scene
     * @param renderer GLRenderer
     * @param cam Camera
     * @param renderTarget framebuffer to which the particles will be rendered
     * @param sceneDepth depth of the scene, used for culling and soft particles
     */
    public static void render(Renderer renderer,Camera cam,FrameBuffer renderTarget,Texture sceneDepth,boolean is2D) {
        render( renderer, cam, renderTarget, sceneDepth,0.1f,2.0f,is2D) ;
    }
    
     /**
     * Render the scene
     * @param renderer GLRenderer
     * @param cam Camera
     * @param renderTarget framebuffer to which the particles will be rendered
     * @param sceneDepth depth of the scene, used for culling and soft particles
     * @param particlesHardness lower values will make soft particles softer
     * @param particlesContrast higher values will make the soft particles transition (gradient between soft and hard particle) shorter 
     */
    public static void render(Renderer renderer,Camera cam,FrameBuffer renderTarget,Texture sceneDepth,float particlesHardness,float particlesContrast,boolean is2d) {
        boolean hasDepth=true;
        if(sceneDepth==null){
            sceneDepth=getFakeDepth();
            hasDepth=false;
        }
        State  state=getState();
      
        if(!(renderer instanceof GLRenderer)){
            throw new RuntimeException("Only GLRenderer supported at this moment");
        }

        GLRenderer gl=(GLRenderer)renderer; 
         
        gl.setFrameBuffer(renderTarget);
        gl.setBackgroundColor(ColorRGBA.BlackNoAlpha);
        gl.clearBuffers(true, true, false);

        gl.setTexture(12, sceneDepth);
        gl.setTexture(13, getSceneData(gl,cam,particlesHardness,particlesContrast,hasDepth));
                        
        if(is2d){
            state.core.SetViewProjectionMatrixWithSimpleWindow(cam.getWidth(),cam.getHeight());
        }else{
            cam.getProjectionMatrix().get(state.v16,true);
            state.core.SetProjectionMatrix(state.v16[0],state.v16[1],state.v16[2],state.v16[3],state.v16[4],state.v16[5],state.v16[6],state.v16[7],state.v16[8],state.v16[9],state.v16[10],state.v16[11],state.v16[12],state.v16[13],state.v16[14],state.v16[15] );
            
            cam.getViewMatrix().get(state.v16,true);
            state.core.SetCameraMatrix(state.v16[0],state.v16[1],state.v16[2],state.v16[3],state.v16[4],state.v16[5],state.v16[6],state.v16[7],state.v16[8],state.v16[9],state.v16[10],state.v16[11],state.v16[12],state.v16[13],state.v16[14],state.v16[15]  );
        }
        state.core.DrawBack();
        state.core.DrawFront();

    }

    public static int playEffect(EffekseerEffectCore e){
        State state=getState();
        return state.core.Play(e);
    }

    public static void pauseEffect(int e,boolean v){
        State state=getState();
         state.core.SetPaused(e,v);
    }

    public static  void setEffectVisibility(int e,boolean v){
        State state=getState();
         state.core.SetShown(e,v);
    }

    public static boolean isEffectAlive(int e){
        State state=getState();
        return state.core.Exists(e);
    }

    public static void setDynamicInput(int e,int index,float value){
        State state=getState();
        state.core.SetDynamicInput(e,index,value);
    }


    public static void setEffectTransform(int handler,Transform tr){
        State state=getState();

        state.m4.setTranslation(tr.getTranslation());
        state.m4.setRotationQuaternion(tr.getRotation());
        state.m4.setScale(tr.getScale());

        state.m4.get(state.v16, true);
        state.core.SetEffectTransformMatrix(handler, state.v16[0],state.v16[1],state.v16[2],state.v16[3],state.v16[4],state.v16[5],state.v16[6],state.v16[7],state.v16[8],state.v16[9],state.v16[10],state.v16[11] );
    }

    private static byte[] readAll(InputStream is) throws IOException {
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

    private static InputStream openStream(AssetManager am, String rootPath,String path) {
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

    public static EffekseerEmitterControl loadEffect(AssetManager am, String path,EffekseerEmitterControl dest) throws IOException {
        InputStream is=openStream(am,"",path);
        return loadEffect(am,path,is,dest);
    }

    public static EffekseerEmitterControl loadEffect(AssetManager am, String path, InputStream is,EffekseerEmitterControl dest) throws IOException {
        byte data[]=readAll(is);
        return loadEffect(am,path,data,dest);
    }
    
    public static EffekseerEmitterControl loadEffect(AssetManager am, String path, byte[] data, EffekseerEmitterControl dest) throws IOException {
        
        byte bytes[]=data;

        String root=path.contains("/")?path.substring(0,path.lastIndexOf("/")):"";
        assert !path.endsWith("/");

        EffekseerEffectCore effectCore=new EffekseerEffectCore();
        if(!effectCore.Load(bytes,bytes.length,1f)){
            throw new AssetLoadException("Can't load effect "+path);
        }

        EffekseerTextureType[] textureTypes=new EffekseerTextureType[]{
            EffekseerTextureType.Color,EffekseerTextureType.Normal,EffekseerTextureType.Distortion};

        // Textures
        for(int t=0;t < 3;t++){
            for(int i=0;i < effectCore.GetTextureCount(textureTypes[t]);i++){
                String p=effectCore.GetTexturePath(i,textureTypes[t]);
                InputStream iss=openStream(am,root,p);
                bytes=readAll(iss);
                if(!effectCore.LoadTexture(bytes,bytes.length,i,textureTypes[t]) ){
                    throw new AssetLoadException("Can't load effect texture "+p);
                }  else{
                    System.out.println("Load textures "+bytes.length+" bytes");
                }
            }
        }

        // Models
        for(int i=0;i < effectCore.GetModelCount();i++){
            String p=effectCore.GetModelPath(i);
            InputStream iss=openStream(am,root,p);
            bytes=readAll(iss);
            if(!effectCore.LoadModel(bytes,bytes.length,i) ){
                throw new AssetLoadException("Can't effect load model "+p);
            }        
        }

        // Materials
        for(int i=0;i < effectCore.GetMaterialCount();i++){   
            String p= effectCore.GetMaterialPath(i); 
            InputStream iss=openStream(am,root,p);            
            bytes=readAll(iss);
            if(!effectCore.LoadMaterial(bytes,bytes.length,i) ){
                throw new AssetLoadException("Can't load effect material "+p);
            }        
        }


        // Sounds?
        
        State state=getState();
        if(dest==null){
            dest= new EffekseerEmitterControl();
        }
        dest.setEffect(effectCore);
        dest.setPath(path);
        state.emitters.put(dest,new EmitterState());

        return dest;
    }

}