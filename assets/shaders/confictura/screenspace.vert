#define HIGHP

in vec4 a_position;
in vec2 a_texCoord0;

out vec2 v_texCoords;

uniform vec4 u_offset;

void main(){
    v_texCoords = u_offset.xy + (a_texCoord0 - vec2(0.5)) * u_offset.zw;
    gl_Position = a_position;
}
