#define HIGHP

#define SCATTER_OUT 3
#define SCATTER_IN 3

const int numOutScatter = SCATTER_OUT;
const float fNumOutScatter = float(SCATTER_OUT);
const int numInScatter = SCATTER_IN;
const float fNumInScatter = float(SCATTER_IN);

const float pi = 3.14159265359;
const float peak = 0.1;
const float flare = 0.0025;
const float intensity = 14.3;
const float gm = -0.85;

varying vec3 v_position;

uniform vec3 u_camPos;
uniform vec3 u_relCamPos;
uniform vec3 u_center;
uniform vec3 u_light;
uniform vec3 u_color;

uniform float u_innerRadius;
uniform float u_outerRadius;
uniform sampler2D u_topology;
uniform vec2 u_viewport;

vec2 intersect(vec3 ray_origin, vec3 ray_dir, float radius){
    float b = dot(ray_origin, ray_dir);
    float c = dot(ray_origin, ray_origin) - radius * radius;

    float d = b * b - c;
    if(d < 0.0) discard;

    d = sqrt(d);
    float near = -b - d;
    float far = -b + d;

    return vec2(near, far);
}

float miePhase(float g, float c, float cc){
    float gg = g * g;

    float a = (1.0 - gg) * (1.0 + cc);

    float b = 1.0 + gg - 2.0 * g * c;
    b *= sqrt(b);
    b *= 2.0 + gg;

    return 1.5 * a / b;
}

float rayleighPhase(float cc){
    return 0.75 * (1.0 + cc);
}

float density(vec3 p){
    return exp(-(length(p) - u_innerRadius) * (4.0 / (u_outerRadius - u_innerRadius)));
}

float optic(vec3 p, vec3 q){
    vec3 step = (q - p) / fNumOutScatter;
    vec3 v = p + step * 0.5;

    float sum = 0.0;
    for(int i = 0; i < numOutScatter; i++){
        sum += density(v);
        v += step;
    }
    sum *= length(step) * (1.0 / (u_outerRadius - u_innerRadius));
    return sum;
}

vec3 inScatter(vec3 eye, vec3 ray, vec2 bound, vec3 light){
    float len = (bound.y - bound.x) / fNumInScatter;
    vec3 step = ray * len;
    vec3 start = eye + ray * bound.x;
    vec3 march = start + ray * (len * 0.5);

    vec3 sum = vec3(0.0);
    for(int i = 0; i < numInScatter; i++){
        vec2 f = intersect(march, light, u_outerRadius);
        vec3 u = march + light * f.y;
        float n = (optic(start, march) + optic(march, u)) * (pi * 4.0);

        sum += density(march) * exp(-n * (peak * u_color + flare));
        march += step;
    }
    sum *= len * (1.0 / (u_outerRadius - u_innerRadius));
    float c = dot(ray, -light);
    float cc = c * c;
    return sum * (peak * u_color * rayleighPhase(cc) + flare * miePhase(gm, c, cc)) * intensity;
}

float unpack(vec4 pack){
    return dot(pack, 1./vec4(1.,255.,65025.,16581375.)) * 150.0;
    /*float exp = floor(pack.w * 256.0 - 127.0);
    float value = dot(pack.xyz, 1.0 / vec3(1.0, 256.0, 256.0 * 256.0));

    value = value * (2.0 * 256.0 * 256.0 * 256.0) / (256.0 * 256.0 * 256.0 - 1.0) - 1.0;
    return value * exp2(exp);*/
}

void main(){
    vec3 eye = u_relCamPos;
    vec3 ray = normalize(v_position - u_camPos);
    vec3 normal = normalize(v_position - u_center);

    vec2 bound = intersect(eye, ray, u_outerRadius);
    bound.y = min(bound.y, unpack(texture2D(u_topology, gl_FragCoord.xy / u_viewport)));

    gl_FragColor = vec4(inScatter(eye, ray, bound, u_light), 1.0);
}
