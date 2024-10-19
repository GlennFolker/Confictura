#define HIGHP

in vec3 v_position;

out vec4 fragColor;

uniform vec3 u_relCamPos;
uniform vec3 u_camPos;
uniform vec3 u_center;

uniform float u_radius;
uniform float u_horizon;
uniform sampler2D u_ref;

uniform mat4 u_proj;
uniform mat4 u_invProj;
uniform float u_far;

bool intersectSphere(vec3 origin, vec3 dir, float radius, out vec2 result){
    float b = dot(origin, dir);
    float c = dot(origin, origin) - radius * radius;

    float d = b * b - c;
    if(d < 0.0) return false;

    d = sqrt(d);
    float near = -b - d;
    float far = -b + d;

    result = vec2(near, far);
    return true;
}

vec4 far(vec3 origin, vec3 ray){
    float far = (u_far - origin.z) / ray.z;
    vec3 intersect = origin + far * ray;
    vec4 clip = u_proj * vec4(intersect, 1.0);

    return clip;
}

vec2 coords(vec4 far){
    return (far.xyz / far.w).xy * 0.5 + vec2(0.5);
}

vec2 coords(vec3 origin, vec3 ray){
    return coords(far(origin, ray));
}

void main(){
    vec3 ray = normalize(v_position - u_camPos);
    vec3 normal = normalize(v_position - u_center);

    vec2 bound;
    if(!intersectSphere(u_relCamPos, ray, u_radius, bound)) discard;

    float intensity = smoothstep(0.0, 1.0, pow((bound.y - bound.x) / (u_radius * 2.0), 3.2));
    float dist = length(u_relCamPos + ((bound.x + bound.y) / 2.0) * ray) / u_radius;

    vec4 center = far(u_camPos, normalize(u_center - u_camPos));
    vec4 current = far(u_camPos, ray);
    vec2 centerCoord = coords(center);
    vec2 currentCoord = coords(current);
    vec2 dir = currentCoord - centerCoord;

    vec3 newRay;
    {
        vec4 clip = current;
        clip.xy -= (dir / dist) * intensity * 3.0 * clip.w;

        vec4 world = u_invProj * clip;
        newRay = normalize(world.xyz / world.w - u_camPos);
    }

    vec3 origin = u_camPos + bound.x * ray;
    vec4 shift = texture(u_ref, coords(origin, newRay));

    float inner = 1.0 - smoothstep(0.0, 1.0, pow(1.0 - max(-dist + u_horizon, 0.0) / u_horizon, 16.0));
    fragColor = mix(
        shift,
        vec4(vec3(0.0), 1.0),
        inner
    );
}
