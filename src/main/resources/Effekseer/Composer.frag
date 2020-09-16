#import "Common/ShaderLib/GLSLCompat.glsllib"
#import "Common/ShaderLib/MultiSample.glsllib"


uniform COLORTEXTURE m_Texture;

uniform COLORTEXTURE m_ParticlesColor;


varying vec2 texCoord;



void main() {
      vec4 sceneColor = getColor(m_Texture, texCoord);
      vec4 particlesColor = getColor(m_ParticlesColor, texCoord);
      
      gl_FragColor=sceneColor;

      gl_FragColor.rgb=mix(gl_FragColor.rgb,particlesColor.rgb,particlesColor.a);
      gl_FragColor.a+=particlesColor.a;
      gl_FragColor.a=clamp(gl_FragColor.a,0.,1.);
}

