#extension GL_ARB_explicit_attrib_location : enable


#if defined(NUM_SAMPLES) && NUM_SAMPLES > 1
    #define SAMPLER_COLOR sampler2DMS
#else
    #define SAMPLER_COLOR sampler2D
#endif

#if defined(NUM_SAMPLES) && NUM_SAMPLES > 1
    #define SAMPLER_DEPTH sampler2DMS
#else
    #define SAMPLER_DEPTH sampler2D
#endif





#ifdef COLOR
    uniform SAMPLER_COLOR m_Color;
#endif

#ifdef DEPTH
    uniform SAMPLER_DEPTH m_Depth;
#endif


in vec2 TexCoord;



#if defined(COLOR)&&defined(DEPTH)
    layout (location = 0) out vec4 outColor;
    layout (location = 1) out vec4 outDepth;
#elif defined(DEPTH)
    layout (location = 0) out vec4 outDepth;
#else
    layout (location = 0) out vec4 outColor;
#endif



vec4 sampleColor(in SAMPLER_COLOR tex,in vec2 tx){
    #if defined(NUM_SAMPLES) && NUM_SAMPLES > 1
        vec4 c=vec4(0);
        ivec2 iTexC = ivec2(tx * vec2(textureSize(tex)));
        for (int i = 0; i < NUM_SAMPLES; i++)c += texelFetch(tex, iTexC, i);    
        c /= float(NUM_SAMPLES);
    #else
        vec4 c=texture(tex,tx);
    #endif
    return c;
}

vec4 sampleDepth(in SAMPLER_DEPTH tex,in vec2 tx){
    #if defined(NUM_SAMPLES) && NUM_SAMPLES > 1
        vec4 c=vec4(0);
        ivec2 iTexC = ivec2(tx * vec2(textureSize(tex)));
        for (int i = 0; i < NUM_SAMPLES; i++)c += texelFetch(tex, iTexC, i);    
        c /= float(NUM_SAMPLES);
    #else
        vec4 c=texture(tex,tx);
    #endif
    return c;
}


void main(){
    #ifdef COLOR
        vec4 color=sampleColor(m_Color,TexCoord);
        outColor=color;
    #endif

    #ifdef DEPTH
        float d=sampleDepth(m_Depth,TexCoord).r;
        d=d*2.-1.;   
        outDepth=vec4(d);
    #endif
}

