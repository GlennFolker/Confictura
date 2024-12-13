#define HIGHP

in vec3 a_position;
in vec3 a_normal;
in vec4 a_color;

out vec3 v_position;
out float v_progress;
out vec4 v_color;

uniform mat4 u_projView;
uniform mat4 u_trans;
uniform float u_radius;

void main(){
    vec4 pos = u_trans * vec4(a_position * u_radius, 1.0);

    v_position = pos.xyz;
    v_progress = a_normal.x;
    v_color = a_color;

    gl_Position = u_projView * pos;
}
