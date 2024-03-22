attribute vec3 a_position;
attribute vec3 a_normal;
attribute vec4 a_color;
attribute float a_darkness;

uniform mat4 u_proj;
uniform vec3 u_camPos;
uniform vec3 u_lightDir;
uniform vec4 u_reflectColor;

varying vec3 v_color;
varying float v_height;
varying float v_darkness;

void main(){
    gl_Position = u_proj * vec4(a_position, 1.0);

    vec3 ref = reflect(normalize(u_lightDir), normalize(a_normal));
    vec3 eye = normalize(u_camPos - a_position);
    float factor = (dot(eye, ref) + 1.0) * 0.5;

    vec3 specular = u_reflectColor.rgb * u_reflectColor.a * pow(factor, 4.0) * (1.0 - a_color.a);
    vec3 diffuse = a_color.rgb * (0.15 + smoothstep(-1.0, 1.0, dot(a_normal, -u_lightDir)) * 0.85);

    v_color = specular + diffuse;
    v_height = a_position.y;
    v_darkness = a_darkness;
}
