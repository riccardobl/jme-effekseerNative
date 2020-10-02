#import "Common/ShaderLib/GLSLCompat.glsllib"
#import "Common/ShaderLib/MultiSample.glsllib"



varying vec2 texCoord;

uniform COLORTEXTURE m_Texture;
#ifdef TX2
uniform COLORTEXTURE m_Texture2;
#endif
void main() {
      #ifdef TX2
            gl_FragColor = getColor( m_Texture2,texCoord);
      #else
            gl_FragColor = getColor( m_Texture,texCoord);
      #endif
      gl_FragColor.a=clamp(gl_FragColor.a,0.,1.);         
}

