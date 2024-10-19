#define HIGHP

in vec3 a_position;

out vec3 v_position;

uniform mat4 u_proj;
uniform mat4 u_trans;
uniform float u_radius;

void main(){
    vec4 pos = u_trans * vec4(a_position, 1.0);

    v_position = pos.xyz;
    gl_Position = u_proj * pos;
}
