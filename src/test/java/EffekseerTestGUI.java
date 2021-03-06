import com.jme.effekseer.EffekseerEmitterControl;
import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.post.filters.BloomFilter.GlowMode;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.jme3.ui.Picture;

import org.jesse.Jesse;
import org.jesse.JesseSpatial;
import org.jesse.JesseSpatial.JesseAnimations;

import com.jme.effekseer.driver.EffekseerEmissionDriverGeneric;
import com.jme.effekseer.driver.fun.impl.*;
import com.jme.effekseer.jme3.EffekseerRenderer;

public class EffekseerTestGUI extends SimpleApplication{

    public static void main(String[] args) {
        EffekseerTestGUI app=new EffekseerTestGUI();
        AppSettings settings=new AppSettings(true);
        settings.setRenderer(AppSettings.LWJGL_OPENGL3);
        app.setSettings(settings);
        app.start(); // start the game
    }

    
    @Override
    public void simpleInitApp() {

   
        EffekseerRenderer effekseerRenderer=EffekseerRenderer.addToViewPort(stateManager, guiViewPort,assetManager, settings.isGammaCorrection());
 
        // Load an effect
        EffekseerEmitterControl effekt=(EffekseerEmitterControl)assetManager.loadAsset("effekts/Basic/OnlySoft.efkefc");

        effekt.setDriver(
            new EffekseerEmissionDriverGeneric()
            .shape(new EffekseerPointFollowingSpatialShape())
            .spawner(new EffekseerGenericSpawner().loop(true).maxInstances(1))
        );

        // Attach to a spatial
        Node n=new Node();
        n.addControl(effekt);
        n.setLocalTranslation(cam.getWidth() / 2,cam.getHeight() / 2,0);
        n.setLocalScale((float)cam.getWidth()/12f);

        guiNode.attachChild(n);
    }
}