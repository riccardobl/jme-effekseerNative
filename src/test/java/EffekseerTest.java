
import com.jme.effekseer.EffekseerEmitterControl;
import com.jme.effekseer.driver.EffekseerEmissionDriverGeneric;
import com.jme.effekseer.driver.fun.impl.EffekseerGenericDynamicInputSupplier;
import com.jme.effekseer.driver.fun.impl.EffekseerGenericSpawner;
import com.jme.effekseer.driver.fun.impl.EffekseerPointFollowingSpatialShape;
import com.jme.effekseer.jme3.EffekseerRenderer;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.post.filters.BloomFilter.GlowMode;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.scene.Node;
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

        EffekseerRenderer effekseerRenderer=EffekseerRenderer.addToViewPort(stateManager, viewPort, assetManager,settings.isGammaCorrection());
        effekseerRenderer.setAsync(1);


        BloomFilter bloom=new BloomFilter(GlowMode.Scene);
        bloom.setBloomIntensity(2f);
        fpp.addFilter(bloom);

        // SSAOFilter ssao=new SSAOFilter();
        // fpp.addFilter(ssao);

        if(settings.getSamples()>0)fpp.setNumSamples(settings.getSamples());


        flyCam.setMoveSpeed(10);
        viewPort.setBackgroundColor(ColorRGBA.Black);
        
        EffekseerEmitterControl effekt=new EffekseerEmitterControl(assetManager,"effekts/Basic/OnlyDistortion.efkefc");
        effekt.setDriver(
            new EffekseerEmissionDriverGeneric()
                .shape(new EffekseerPointFollowingSpatialShape())
                .spawner(new EffekseerGenericSpawner().loop(true).maxInstances(1))
                .dynamicInputSupplier(new EffekseerGenericDynamicInputSupplier().set(0,10f).set(1,11f))
        );
        sp.addControl(effekt);
        effekt.setScale(1f);

        // cam.setLocation(new Vector3f(0,0,10));


        EffekseerEmitterControl effekt2=new EffekseerEmitterControl(assetManager,"effekts/Basic/OnlySoft.efkefc");
        Node n=new Node();
        n.addControl(effekt2);
        n.setLocalTranslation(0,1,10);
        rootNode.attachChild(n);
        effekt2.setScale(1f);
        effekt.setDriver(
            new EffekseerEmissionDriverGeneric()
                .shape(new EffekseerPointFollowingSpatialShape())
                .spawner(new EffekseerGenericSpawner().loop(true).maxInstances(1))
                .dynamicInputSupplier(new EffekseerGenericDynamicInputSupplier().set(0,10f).set(1,11f))
        );

        cam.lookAt(Vector3f.ZERO,Vector3f.UNIT_Y);

    }
    

     public static void main(String[] args) {
         AppSettings settings=new AppSettings(true);
         settings.setRenderer(AppSettings.LWJGL_OPENGL3);
         settings.setDepthBits(24);
         settings.        putBoolean("GraphicsDebug", true);
         EffekseerTest app=new EffekseerTest();
        app.setSettings(settings);;
         app.start();
     }
}