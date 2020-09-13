#import "Common/ShaderLib/MultiSample.glsllib"

uniform COLORTEXTURE m_Texture;

uniform COLORTEXTURE m_ParticlesColor;


in vec2 texCoord;
out vec4 outFragColor;




void main() {
      vec4 sceneColor = getColor(m_Texture, texCoord);
      vec4 particlesColor = getColor(m_ParticlesColor, texCoord);
      
      outFragColor=sceneColor;

      outFragColor.rgb=mix(outFragColor.rgb,particlesColor.rgb,particlesColor.a);
      outFragColor.a+=particlesColor.a;
      outFragColor.a=clamp(outFragColor.a,0.,1.);
}

