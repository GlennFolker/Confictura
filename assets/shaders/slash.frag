#define HIGHP

#define PI 3.1415927
#define PI2 2 * PI

varying lowp vec4 v_color;
varying lowp vec4 v_mix_color;
varying highp vec2 v_texCoords;
varying highp vec3 v_slashData;

uniform highp sampler2D u_texture;

uniform highp sampler2D u_screenTexture;
uniform highp vec2 u_campos;
uniform highp vec2 u_resolution;
uniform highp vec2 u_viewport;
uniform lowp float u_blend;

uniform int u_noiseOct;
uniform float u_noiseScl;
uniform float u_noiseLac;
uniform float u_noisePer;
uniform float u_noiseMag;

uniform highp vec2 u_slashVerts[DATA_COUNT];
uniform int u_slashVertsLen;

float hash(vec2 p){
    vec3 p3 = fract(vec3(p.xyx) * 0.13);
    p3 += dot(p3, p3.yzx + 3.333);
    
    return fract((p3.x + p3.y) * p3.z);
}

float noise(vec2 pos){
    vec2 i = floor(pos);
    vec2 f = fract(pos);
    
    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));
    
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}

float octaveNoise(vec2 pos){
    pos /= u_noiseScl;
    pos += vec2(1000.0, 1000.0);
    
    float frequency = 1.0;
    float magnitude = 1.0;
    float sum = 0.0;
    float total = 0.0;
    for(int i = 0; i < u_noiseOct; i++){
        sum += noise(pos / frequency) * magnitude;
        total += magnitude;
        
        frequency *= u_noiseLac;
        magnitude *= u_noisePer;
    }
    
    return sum / total;
}

float interp(float val){
    return val * val * (3.0 - 2.0 * val);
}

vec2 trns(float angle, float amount){
    return vec2(amount * cos(angle), amount * sin(angle));
}

void main(){
    vec2 coords = (gl_FragCoord.xy / u_viewport) * u_resolution + u_campos;
    float magRaw = interp(octaveNoise(coords));
    float mag = pow((magRaw * u_noiseMag - u_noiseMag / 2.0) * v_slashData.y, 2.0);

    float center = interp(1.0 - abs(v_slashData.x - 0.5) * 2.0);
    float intensity = (v_slashData.y + mag) * center;

    vec2 deviation = vec2(0.0, 0.0);

    int startIndex = int(v_slashData.z);
    float fractIndex = fract(v_slashData.z);
    for(int slashVertsIndex = min(startIndex, u_slashVertsLen - 1); slashVertsIndex >= 0; slashVertsIndex--){
        if(intensity <= 0.0) break;
        
        vec2
            slashVert = u_slashVerts[slashVertsIndex],
            slashVertNext = slashVertsIndex == 0 ? slashVert : u_slashVerts[slashVertsIndex - 1];
        float
            angle = slashVert.x,
            len = mix(slashVert.y, slashVertNext.y, fractIndex);
    
        deviation += trns(angle, min(intensity, len));
        intensity -= len;
    }
    
    coords = (coords + deviation - u_campos) / u_resolution;
    
    vec4 screen = texture2D(u_screenTexture, coords);
    
    vec4 tex = texture2D(u_texture, v_texCoords);
    tex.a *= center * (0.5 + magRaw * 0.5);
    tex = v_color * mix(tex, vec4(v_mix_color.rgb, tex.a), v_mix_color.a);
    
    gl_FragColor = vec4((tex * tex.a + screen * screen.a * (1.0 - tex.a * (1.0 - u_blend))).rgb, 1.0);
}
