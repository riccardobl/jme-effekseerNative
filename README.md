# Effekseer Native for JME

This is a library that uses [EffekseerForMultiLanguages](https://github.com/effekseer/EffekseerForMultiLanguages) to load and render effects made with Effekseer in jme.

It extends Effekseer by providing basic soft particles and support for offscreen rendering.

Supported platforms:
- Windows 64bit
- Linux 64bit

TODO:

- Distortions

- Sounds

## Gradle 
```gradle
repositories {
	maven { url 'https://jitpack.io' }
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
    new EmissionDriverGeneric()
    .emitFunction(EffekseerEmitterEmitFunctions.emitLoop(100,0,1f,2f))
    .shapeFunction(EffekseerEmitterShapeFunctions.pointFollowingSpatial())
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

// Update the logic
Effekseer.update( tpf);

// Render
// sceneDepth is the depth of the current scene - used for soft particles
Effekseer.render(Renderer renderer,Camera cam,FrameBuffer renderTarget,Texture sceneDepth);



```



