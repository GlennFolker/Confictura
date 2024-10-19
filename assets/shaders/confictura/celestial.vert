in vec3 a_position;
in vec3 a_normal;
in vec4 a_color;

out vec3 v_color;

uniform mat4 u_proj;
uniform mat4 u_trans;
uniform mat3 u_normal;

uniform vec3 u_light;
uniform vec3 u_ambientColor;
uniform vec3 u_camPos;

void main(){
    vec4 pos = u_trans * vec4(a_position, 1.0);
    vec3 normal = normalize(u_normal * a_normal);
    vec3 lightDir = normalize(u_light - pos.xyz);

    vec3 specular = vec3(0.0);
    vec3 ref = reflect(-lightDir, normal);
    vec3 eye = normalize(u_camPos - pos.xyz);

    float factor = dot(eye, ref);
    if(factor > 0.0) specular = vec3(pow(factor, 40.0)) * (1.0 - a_color.a);

    vec3 diffuse = (u_ambientColor + specular) * vec3(0.01 + clamp((dot(normal, lightDir) + 1.0) / 2.0, 0.0, 1.0));
    v_color = a_color.rgb * diffuse;
    gl_Position = u_proj * pos;
}
