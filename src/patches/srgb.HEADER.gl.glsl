

void SRGB2LINEAR(inout vec4 color){
    vec4 txData3=texelFetch2D(_sceneData,vec2(2.0,0.0),0.0);
    if(txData3.r>0.001){
        vec3 linOut = pow(color.xyz,vec3(2.2));
        color=vec4(linOut,color.w);
    }
}