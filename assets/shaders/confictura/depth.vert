#define HIGHP

attribute vec3 a_position;

varying vec3 v_pos;

uniform mat4 u_proj;
uniform mat4 u_trans;

void main(){
    vec4 pos = u_trans * vec4(a_position, 1.0);
    v_pos = pos.xyz;

    gl_Position = u_proj * pos;
}
