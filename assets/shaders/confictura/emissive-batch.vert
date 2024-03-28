attribute vec3 a_position;
attribute vec3 a_normal;
attribute vec4 a_color;
attribute vec2 a_texCoord0;

uniform mat4 u_proj;
uniform mat4 u_trans;
uniform mat3 u_normal;

uniform vec3 u_light;
uniform vec3 u_ambientColor;

varying vec4 v_color;
varying vec2 v_texCoord;

void main(){
    vec4 pos = u_trans * vec4(a_position, 1.0);
    vec3 normal = normalize(u_normal * a_normal);
    vec3 lightDir = normalize(u_light - pos.xyz);

    vec3 diffuse = u_ambientColor * vec3(0.01 + clamp((dot(normal, lightDir) + 1.0) / 2.0, 0.0, 1.0));
    v_color = a_color * vec4(diffuse, 1.0);
    v_texCoord = a_texCoord0;

    gl_Position = u_proj * pos;
}
