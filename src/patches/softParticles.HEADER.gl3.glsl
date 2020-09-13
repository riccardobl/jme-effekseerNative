
layout(binding=12) uniform sampler2D _sceneDepth;
layout(binding=13) uniform sampler2D _sceneData;


float linearizeDepth(in float depth,in vec2 frustumNearFar){
    float f=frustumNearFar.y;
    float n = frustumNearFar.x;
    float d=depth*2.-1.;
    return (2. * n * f) / (f + n - d * (f - n));
}

float linearize01Depth(in float depth,in vec2 frustumNearFar){
    float d=linearizeDepth(depth,frustumNearFar);
    float f=frustumNearFar.y;
    float n = frustumNearFar.x;
    return (d-n)/(f-n);
}

float _df(float d){
    float hardness=0.1;
    float contrast=2.0;
    d= d*hardness;
    d=pow(d,contrast);
    d=clamp(d,0.0,1.0);
    return d;
}

void _softParticles(inout vec4 color){
   vec2 resolution=vec2(textureSize(_sceneDepth,0));
   vec2 screenUv=gl_FragCoord.xy/resolution;
   vec2 frustumNearFar=vec2(texelFetch(_sceneData,ivec2(0,0),0).rg);

   float sceneDepthV=linearizeDepth(texture(_sceneDepth,screenUv).r,frustumNearFar);
   float particleDepthV=linearizeDepth(gl_FragCoord.z,frustumNearFar);
   float d=sceneDepthV-particleDepthV;
   if(d<=0)discard;
   color.a=color.a*_df(d);   
}