package com.jme.effekseer;

import java.lang.reflect.Field;
import java.util.EnumSet;

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
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;

/**
 * EffekseerPostRenderer does all the rendering stuff for you, just add it to your FilterPostProcessor and it will just work.
 * @author Riccardo Balbo
 */
public class EffekseerFilter extends Filter {
    EffekseerRenderer renderer;

    public EffekseerFilter( AssetManager manager, boolean sRGB){
        this(manager,sRGB,false);
    }
    
    public EffekseerFilter( AssetManager manager, boolean sRGB,boolean is2D){
        renderer=new EffekseerRenderer(manager,sRGB);
        renderer.set2D(is2D);
    }



    public EffekseerRenderer getRenderer(){
        return renderer;
    }


    @Override
    protected void initFilter(final AssetManager manager, final RenderManager renderManager, final ViewPort vp, final int w, final int h) {
        assert manager != null;
        this.material=new Material(manager,"Effekseer/Composer.j3md");
        renderer.setRenderManager(renderManager);
        renderer.setViewPort(vp);

    }

    @Override
    public void preFrame(final float tpf) {
        renderer.setTpf(tpf);
    }

 

    // Filter
    @Override
    protected void postFrame(final RenderManager renderManager, final ViewPort viewPort, final FrameBuffer prevFilterBuffer, final FrameBuffer sceneBuffer) {
        renderer.setSceneBuffer(sceneBuffer);
        renderer.render();
    }

    @Override
    protected Material getMaterial() {
        return this.material;
    }

    
}