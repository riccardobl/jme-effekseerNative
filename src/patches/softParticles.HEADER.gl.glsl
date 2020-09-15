#if __VERSION__ >= 130
    #define texture2D texture
    #define texelFetch2D(tx,coord,lod,res) texelFetch(tx,ivec2(coord.x,coord.y),int(lod))
    #define _SP_OUTPUT _entryPointOutput
#else
    #define texelFetch2D(tx,coord,lod,res) texture(tx,(coord+vec2(0.5))/res))
    #define _SP_OUTPUT gl_FragData[0] 
#endif
layout(binding=12) uniform sampler2D _sceneDepth;
layout(binding=13) uniform sampler2D _sceneData;


float linearizeDepth(in float depth,in vec2 frustumNearFar){
    float f=frustumNearFar.y;
    float n = frustumNearFar.x;
    float d=depth*2.-1.;
    return (2. * n * f) / (f + n - d * (f - n));
}

float _df(in float d,in float hardness,in float contrast){
    // float hardness=0.1;
    // float contrast=2.0;
    d= d*hardness;
    d=pow(d,contrast);
    d=clamp(d,0.0,1.0);
    return d;
}

void _softParticles(inout vec4 color){
    vec4 txData1=texelFetch2D(_sceneData,vec2(0.0,0.0),0.0,vec2(1.0,1.0));
    vec2 resolution=txData1.rg;
    float hardness=txData1.b;

    vec4 txData2=texelFetch2D(_sceneData,vec2(1.0,0.0),0.0,resolution);
    vec2 frustumNearFar=txData2.rg;
    float hardnessContrast=txData2.b;

    if(frustumNearFar.x<=0&&frustumNearFar.y<=0)return;

    vec2 screenUv=gl_FragCoord.xy/resolution;
    float sceneDepthV=linearizeDepth(texture2D(_sceneDepth,screenUv).r,frustumNearFar);
    float particleDepthV=linearizeDepth(gl_FragCoord.z,frustumNearFar);
    float d=sceneDepthV-particleDepthV;
    if(d<=0.0)discard;

    color.a=color.a*_df(d,hardness,hardnessContrast);   
}