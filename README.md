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


## Usage - Managed Rendering
```java
// Add a filter post processor to your viewPort
// optional:
//   if the FilterPostProcessor is created, EffekseerRenderer will attach itself as a filter
//   otherwise it will be attached as a scene processor.
//   Note: if a FilterPostProcessor is used in the viewport, EffekseerRenderer must be called before 
//         attaching any other filter, becoming defacto the first filter to be attached to the FilterPostProcessor
// FilterPostProcessor fpp=new FilterPostProcessor(assetManager);
// viewPort.addProcessor(fpp);

// Add Effekseer Renderer
EffekseerRenderer effekseerRenderer=EffekseerRenderer.addToViewPort(stateManager, viewPort, assetManager, settings.isGammaCorrection());
        
// Load an effect
EffekseerEmitterControl effekt=new EffekseerEmitterControl(assetManager,"effekts/Pierre/Lightning.efkefc");

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
boolean isSRGB=true;
Effekseer.init(assetManager,isSRGB);

// Update logic: This needs to be called in your update loop (once per frame)
Effekseer.update( tpf);

// --- Render scene 1
// Select the scene to render
Effekseer.beginRender(root1);

// Render
Effekseer.render(
	Renderer, /* The opengl renderer */
	Camera, /* The scene camera */
	FrameBuffer, /* The render target */
	Texture2D, /* The depth texture of the current scene (for soft particles, null to disable soft particles) */
	Float, /* Particles hardness (for soft particles) */
	Float, /* Particles contrast (for soft particles) */
	Boolean /* True if rendering on 2d Target */
);

// End the render
Effekseer.endRender();

// --- Render scene 2
// Select the scene to render
Effekseer.beginRender(root2);

// Render
Effekseer.render(
	Renderer, /* The opengl renderer */
	Camera, /* The scene camera */
	FrameBuffer, /* The render target */
	Texture2D, /* The depth texture of the current scene (for soft particles, null to disable soft particles) */
	Float, /* Particles hardness (for soft particles) */
	Float, /* Particles contrast (for soft particles) */
	Boolean /* True if rendering on 2d Target */
);

// End the render
Effekseer.endRender();

```

Note: opengl cannot read and write on the same target, this means that depth that comes from the same target framebuffer has to be copied.
The following helper utility can be used to do it in a reasonably fast way (it used glBlitFramebuffer and a pool of cached framebuffers internally):
```java
Texture copiedDepth=EffekseerUtils.copyDepthFromFrameBuffer(
	RenderManager, /* the render manager */
	FrameBuffer, /* source */
	int, /* width */
	int /* height */
);
```

### Limitations
- There is an issue with depth sorting when using *Managed Rendering* to render inside the SimpleApplication's guiViewPort: the particles will always be rendered on top. Finding a generic workaround for this issue is pretty complex due to the way the engine handles the guiViewPort. A possible solution is to use *Advanced Usage - Manual Rendering* to render a special root node containing only "gui particles" on top of the main framebuffer using an appropriate Camera.

