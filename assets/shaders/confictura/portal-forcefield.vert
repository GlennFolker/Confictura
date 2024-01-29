#define HIGHP

attribute vec3 a_position;
attribute vec3 a_normal;
attribute vec4 a_color;

varying vec3 v_position;
varying float v_progress;
varying vec4 v_color;

uniform mat4 u_proj;
uniform mat4 u_trans;
uniform float u_radius;

void main(){
    vec4 pos = u_trans * vec4(a_position * u_radius, 1.0);

    v_position = pos.xyz;
    v_progress = a_normal.x;
    v_color = a_color;

    gl_Position = u_proj * pos;
}
