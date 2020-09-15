import com.jme.effekseer.EffekseerEmitterControl;
import com.jme.effekseer.EffekseerPostRenderer;
import com.jme3.app.SimpleApplication;
import com.jme3.post.FilterPostProcessor;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;

public class EffekseerTestGUI extends SimpleApplication{

    public static void main(String[] args) {
        EffekseerTestGUI app=new EffekseerTestGUI();
        app.setShowSettings(false);
        AppSettings settings=new AppSettings(true);
        settings.setRenderer(AppSettings.LWJGL_OPENGL3);
        app.setSettings(settings);
        app.start(); // start the game
    }

    @Override
    public void simpleInitApp() {

        // Add a filter post processor to your viewPort
        FilterPostProcessor fpp=new FilterPostProcessor(getAssetManager());
        getGuiViewPort().addProcessor(fpp);

        // Add Effekseer Renderer
        fpp.addFilter(new EffekseerPostRenderer(getAssetManager(), false /*Disable depth test from the scene*/));

        // Load an effect
        EffekseerEmitterControl effekt=(EffekseerEmitterControl)assetManager.loadAsset("effekts/Pierre/Flame.efkefc");

        // Set a driver (optional)
        //        effekt.setDriver(
        //                new EmissionDriverGeneric()
        //                        .emitFunction(EffekseerEmitterEmitFunctions.emitLoop(100,0,1f,2f))
        //                        .shapeFunction(EffekseerEmitterShapeFunctions.pointFollowingSpatial())
        //        );

        // Attach to a spatial
        Node n=new Node();
        n.addControl(effekt);
        n.setLocalTranslation(cam.getWidth() / 2,cam.getHeight() / 2,0);
        n.setLocalScale(50f);

        guiNode.attachChild(n);
    }
}