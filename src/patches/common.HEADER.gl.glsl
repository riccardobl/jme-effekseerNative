#if __VERSION__ >= 130
    #define texture2D texture
    #define texelFetch2D(tx,coord,lod) texelFetch(tx,ivec2(coord.x,coord.y),int(lod))
    #define _SP_OUTPUT _entryPointOutput
#else
    #define texelFetch2D(tx,coord,lod) texture2D(tx,(coord+vec2(0.5))/vec3(3.0,1.0))
    #define _SP_OUTPUT gl_FragData[0] 
#endif

layout(binding=13) uniform sampler2D _sceneData;

