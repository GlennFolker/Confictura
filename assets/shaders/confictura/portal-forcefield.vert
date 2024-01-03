#define HIGHP

attribute vec3 a_position;
attribute vec4 a_color;

varying vec3 v_position;
varying vec4 v_color;

uniform mat4 u_projection;
uniform mat4 u_model;
uniform float u_radius;

void main(){
    vec4 pos = u_model * vec4(a_position * u_radius, 1.0);

    v_position = pos.xyz;
    v_color = a_color;

    gl_Position = u_projection * pos;
}
