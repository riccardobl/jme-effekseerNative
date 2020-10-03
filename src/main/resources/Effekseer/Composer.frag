#import "Common/ShaderLib/GLSLCompat.glsllib"
#import "Common/ShaderLib/MultiSample.glsllib"

varying vec2 texCoord;
uniform COLORTEXTURE m_Texture;

void main() {
      gl_FragColor = getColor( m_Texture,texCoord);
}

