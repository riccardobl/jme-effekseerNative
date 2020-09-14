# Effekseer Native for JME

This is a library that uses [EffekseerForMultiLanguages](https://github.com/effekseer/EffekseerForMultiLanguages) to load and render effects made with Effekseer in jme.

It extends Effekseer by providing basic soft particles and support for offscreen rendering.

Supported platforms:
- Windows 64bit
- Linux 64bit

Extra features:
- Soft particles
- Multiscene rendering
- Offscreen rendering

Missing features:
- Distortions
- Sounds

## Gradle 
```gradle
repositories {
    maven { url 'https://jitpack.io' }    
    maven { url  "https://dl.bintray.com/riccardo/effekseer" }
}
dependencies {
	implementation 'com.github.riccardobl:jme-effekseerNative:-SNAPSHOT'
}

```


## Usage
```java
// Add a filter post processor to your viewPort
FilterPostProcessor fpp=new FilterPostProcessor(assetManager);
 viewPort.addProcessor(fpp);

// Add Effekseer Renderer
fpp.addFilter(new EffekseerPostRenderer(assetManager));
        
// Load an effect
EffekseerEmitterControl effekt=(EffekseerEmitterControl)assetManager.loadAsset("effekts/Pierre/Lightning.efkefc");

// Set a driver (optional)
effekt.setDriver(
    new EffekseerEmissionDriverGeneric()
        .shape(new EffekseerPointFollowingSpatialShape())
        .spawner(new EffekseerGenericSpawner().loop(true).delay(1f,2f, 1f).maxInstances(1000))
        .dynamicInputSupplier(new EffekseerGenericDynamicInputSupplier().set(0,10f).set(1,11f))
);

// Attach to a spatial
Node n=new Node();
n.addControl(effekt);


rootNode.attachChild(n);

```

### Advanced Usage - Manual Rendering
This is intended to be used on custom render pipelines or offscreen rendering
```java
// Init Effekseer
Effekseer.init(assetManager);

// Select the scene to render (only child of root will be renderer). root=null to render all.
Effekseer.beginScene(root);

// Update the logic
Effekseer.update( tpf);

// Render
// sceneDepth is the depth of the current scene - used for soft particles
Effekseer.render(Renderer renderer,Camera cam,FrameBuffer renderTarget,Texture sceneDepth);

// End the scene, reset states
Effekseer.endScene();

```



