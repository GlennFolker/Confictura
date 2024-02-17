attribute vec3 a_position;
attribute vec3 a_normal;
attribute vec4 a_color;

uniform mat4 u_proj;
uniform mat4 u_trans;
uniform mat4 u_normal;

uniform vec3 u_light;

varying vec3 v_pos;
varying vec4 v_color;
varying vec3 v_normal;
varying vec3 v_light;

void main(){
    vec4 pos = u_trans * vec4(a_position, 1.0);
    vec3 normal = (u_normal * vec4(a_normal, 1.0)).xyz;
    vec3 lightDir = normalize(u_light - pos.xyz);

    v_pos = pos.xyz;
    v_color = a_color;
    v_normal = normal;
    v_light = lightDir;
    gl_Position = u_proj * pos;
}
