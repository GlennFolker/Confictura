attribute vec3 a_position;
attribute vec3 a_normal;
attribute vec4 a_color;

uniform mat4 u_proj;
uniform vec3 u_lightDir;

varying vec4 color;

void main(){
    gl_Position = u_proj * vec4(a_position, 1.0);

    vec3 diffuse = a_color.rgb * (dot(a_normal, -u_lightDir) * 0.5 + 0.5);
    color = vec4(diffuse, 1.0);
}
