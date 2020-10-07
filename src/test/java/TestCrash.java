import com.jme.effekseer.EffekseerEmitterControl;
import com.jme.effekseer.EffekseerRenderer;
import com.jme.effekseer.driver.EffekseerEmissionDriverGeneric;
import com.jme.effekseer.driver.fun.impl.EffekseerGenericSpawner;
import com.jme.effekseer.driver.fun.impl.EffekseerPointFollowingSpatialShape;
import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;

import java.util.List;

public class TestCrash extends SimpleApplication {

    public static void main(String[] args) {
        TestCrash app = new TestCrash();
        app.setShowSettings(false);
        AppSettings settings = new AppSettings(true);
        settings.setRenderer(AppSettings.LWJGL_OPENGL3);
        app.setSettings(settings);
        app.start(); // start the game
    }

    @Override
    public void simpleInitApp() {

        EffekseerRenderer.addToViewPort(getStateManager(), guiViewPort, assetManager, settings.isGammaCorrection());
        EffekseerEmitterControl effekt = (EffekseerEmitterControl) assetManager.loadAsset("Effekts/prova.efkefc");
        effekt.setDriver(
                new EffekseerEmissionDriverGeneric()
                        .shape(new EffekseerPointFollowingSpatialShape())
                        .spawner(new EffekseerGenericSpawner().loop(true).maxInstances(1))
        );

        for (int i = 0; i < 10; i++) {
            // Attach to a spatial
            Node n = new Node();
            makeEffect(n);

            guiNode.attachChild(n);
        }

    }

    float TOT=0;
    public void simpleUpdate(float tpf){
        TOT+=tpf;
        if (TOT>5){
            guiNode.depthFirstTraversal(sx->{
                EffekseerEmitterControl c=sx.getControl(EffekseerEmitterControl.class);
                if(c!=null){
                    System.out.println("Stop "+c);
                    c.stop();
                }
            });
            List<Spatial> children=guiNode.getChildren();
            for (Spatial s:children){
                s.removeFromParent();
            }
        }
    }
    void makeEffect(Node node) {

        // Load an effect
        EffekseerEmitterControl effekt = new EffekseerEmitterControl(getAssetManager(), "Effekts/prova.efkefc");
        effekt.setDriver(
                new EffekseerEmissionDriverGeneric()
                        .shape(new EffekseerPointFollowingSpatialShape())
                        .spawner(new EffekseerGenericSpawner().loop(true).maxInstances(1))
        );

        // Attach to a spatial
        node.addControl(effekt);
        node.setLocalScale(50f);
        node.addControl(effekt);
        node.setLocalTranslation(cam.getWidth() / 2, cam.getHeight() / 2, 0);
        node.setLocalScale(50f);

    }
}