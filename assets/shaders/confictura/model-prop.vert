attribute vec3 a_position;
attribute vec3 a_normal;
attribute vec4 a_color;

uniform mat4 u_proj;
uniform vec3 u_camPos;
uniform vec3 u_lightDir;
uniform vec4 u_reflectColor;

varying vec4 color;

void main(){
    gl_Position = u_proj * vec4(a_position, 1.0);

    vec3 ref = reflect(normalize(u_lightDir), normalize(a_normal));
    vec3 eye = normalize(u_camPos - a_position);

    float factor = (dot(eye, ref) + 1.0) * 0.5;
    float specular = pow(factor, 4.0) * (1.0 - a_color.a);

    vec3 diffuse = u_reflectColor.rgb * u_reflectColor.a * specular + a_color.rgb * (0.15 + smoothstep(-1.0, 1.0, dot(a_normal, -u_lightDir)) * 0.85);
    color = vec4(diffuse, 1.0);
}
