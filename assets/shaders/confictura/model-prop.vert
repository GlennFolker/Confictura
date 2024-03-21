attribute vec3 a_position;
attribute vec3 a_normal;

uniform mat4 u_proj;
uniform vec3 u_lightDir;
uniform vec4 u_ambientColor;

void main(){
    gl_Position = u_proj * vec4(a_position, 1.0);
}
