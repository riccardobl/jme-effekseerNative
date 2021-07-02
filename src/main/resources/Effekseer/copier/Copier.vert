out vec2 TexCoord;

in vec4 inPosition;
in vec2 inTexCoord;


void main(){
    TexCoord=inTexCoord;
    vec2 pos = inPosition.xy * 2.0 - 1.0;      
    gl_Position = vec4(pos, 0.0, 1.0);
}
