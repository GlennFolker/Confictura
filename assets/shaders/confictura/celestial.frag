uniform vec3 u_ambientColor;
uniform vec3 u_camPos;

varying vec3 v_pos;
varying vec4 v_color;
varying vec3 v_normal;
varying vec3 v_light;

void main(){
    vec3 specular = vec3(0.0);
    vec3 ref = reflect(-v_light, v_normal);
    vec3 eye = normalize(u_camPos - v_pos);
    float factor = dot(eye, ref);
    if(factor > 0.0){
        specular = vec3(pow(factor, 40.0)) * (1.0 - v_color.a);
    }

    vec3 diffuse = (u_ambientColor + specular) * vec3(0.01 + clamp((dot(v_normal, v_light) + 1.0) / 2.0, 0.0, 1.0));
    gl_FragColor = vec4(v_color.rgb, 1.0) * vec4(diffuse, 1.0);
}
