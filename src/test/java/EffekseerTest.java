
import com.jme.effekseer.EffekseerEmitterControl;
import com.jme.effekseer.EffekseerEmitterEmitFunctions;
import com.jme.effekseer.EffekseerEmitterShapeFunctions;
import com.jme.effekseer.EffekseerPostRenderer;
import com.jme.effekseer.EffekseerEmissionDriverGeneric;
import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.post.filters.DepthOfFieldFilter;
import com.jme3.post.filters.BloomFilter.GlowMode;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.system.AppSettings;

import org.jesse.Jesse;
import org.jesse.JesseSpatial;
import org.jesse.JesseSpatial.JesseAnimations;

public class EffekseerTest extends SimpleApplication{

    @Override
    public void simpleInitApp() {
        JesseSpatial sp=Jesse.buildAndAttachScene(assetManager,rootNode);
        sp.playAnim(0,JesseAnimations.Run,true);
        
        FilterPostProcessor fpp=new FilterPostProcessor(assetManager);
        viewPort.addProcessor(fpp);
        fpp.addFilter(new EffekseerPostRenderer(assetManager));
        fpp.addFilter(new BloomFilter(GlowMode.Scene));
        fpp.addFilter(new SSAOFilter(2.9299974f,32.920483f,5.8100376f,0.091000035f));
        

        flyCam.setMoveSpeed(10);
        viewPort.setBackgroundColor(ColorRGBA.Black);
        


        EffekseerEmitterControl effekt=(EffekseerEmitterControl)assetManager.loadAsset("effekts/Pierre/Flame.efkefc");
        effekt.setDriver(new EffekseerEmissionDriverGeneric()
            .emitFunction(EffekseerEmitterEmitFunctions.emitLoop(1,0,0f,0f))
            .shapeFunction(EffekseerEmitterShapeFunctions.pointFollowingSpatial())
        );
        sp.addControl(effekt);


        cam.setLocation(new Vector3f(0,0,10));
        cam.lookAt(Vector3f.ZERO,Vector3f.UNIT_Y);

    }
    

     public static void main(String[] args) {
         AppSettings settings=new AppSettings(true);
         settings.setRenderer(AppSettings.LWJGL_OPENGL3);
         EffekseerTest app=new EffekseerTest();
        app.setSettings(settings);;
         app.start();
     }
}