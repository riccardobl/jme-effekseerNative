# Upgrade guide



## From 0.3 to 0.4

### Generic
- OpenGL 3+ is now a requirement

### Managed Rendering
- EffekseerRenderer, EffekseerProcessor and EffekseerFilter were moved from `com.jme.effekseer` to `com.jme.effekseer.jme3`

---

### Manual Rendering
- Effekseer.render() signature was changed from
```java
Effekseer.render(
	Renderer, /* The opengl renderer */
    Camera, /* The scene camera */
    FrameBuffer, /* The render target */
    Texture2D, /* The depth texture of the current scene (for soft particles, null to disable soft particles) */
    Float, /* Particles hardness (for soft particles) */
    Float, /* Particles contrast (for soft particles) */
    Boolean /* True if rendering on 2d Target */
);
``` 
to
```java
Effekseer.render(
	Renderer gl, /* The opengl renderer */
	Camera cam, /* The scene camera */
	FrameBuffer target, /* The render target */
	Texture2D color, /* The current scene, used for distortions  (null to disable distortions) */
	Texture2D depth, /* The depth of the current scene, used for soft particles (null to disable soft particles) */
	boolean isOrthographic /* true if rendering in orthographic mode */
);
```

- EffekseerUtils.copyFrameBuffer() signature was changed from 
```java
Texture copiedDepth=EffekseerUtils.copyDepthFromFrameBuffer(
	RenderManager, /* the render manager */
	FrameBuffer, /* source */
	int, /* width */
	int /* height */
);

```
to
```java
FrameBufferCopy copiedFb=EffekseerUtils.copyFrameBuffer(
	AssetManager am, /* the asset manager */
	RenderManager rm, /* the render manager */
	FrameBuffer source, /* source */
	int width, /* width */
	int height, /* height */
	boolean copyColor, /* true to copy the color buffer */
	boolean colorTarget, /* which target to copy (0=first) */
	boolean copyDepth /* true to copy the depth buffer*/
);

Texture color=copiedFb.colorTx;
Texture depth=copiedFb.depthTx;


```