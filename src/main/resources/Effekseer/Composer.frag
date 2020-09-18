#import "Common/ShaderLib/GLSLCompat.glsllib"
#import "Common/ShaderLib/MultiSample.glsllib"



varying vec2 texCoord;

uniform COLORTEXTURE m_Texture;
#ifdef BLEND
uniform COLORTEXTURE m_BlendTexture;
#endif
void main() {
      vec4 sceneColor = getColor( m_Texture,texCoord);
      gl_FragColor=sceneColor;
      #ifdef BLEND
         vec4 b = getColor( m_BlendTexture,texCoord);
         float a=clamp(b.a,0.,1.);
         gl_FragColor.rgb=mix(gl_FragColor.rgb,b.rgb,a);
         gl_FragColor.a+=a;
         gl_FragColor.a=clamp(gl_FragColor.a,0.,1.);         
      #endif
}

