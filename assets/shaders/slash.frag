#define HIGHP

varying lowp vec4 v_color;
varying lowp vec4 v_mix_color;
varying highp vec2 v_texCoords;
varying highp vec2 v_slashData;
flat varying int v_slashIndex;

uniform highp sampler2D u_texture;

uniform highp sampler2D u_screenTexture;
uniform highp vec2 u_campos;
uniform highp vec2 u_resolution;
uniform highp vec2 u_viewport;
uniform lowp float u_blend;

uniform lowp float u_noiseScl;
uniform lowp float u_noiseLac;
uniform lowp float u_noisePer;
uniform int u_noiseOct;

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
    float res = val * val * (3.0 - 2.0 * val);
    return res * res;
}

vec2 trns(float angle, float amount){
    return vec2(amount * cos(angle), amount * sin(angle));
}

void main(){
    float center = interp(v_slashData.x);
    float intensity = v_slashData.y * center;
    
    vec2 deviation = vec2(0.0, 0.0);
    for(int slashVertsIndex = v_slashIndex; slashVertsIndex < u_slashVertsLen; slashVertsIndex++){
        if(intensity <= 0.0) break;
        
        vec2 slashVert = u_slashVerts[slashVertsIndex];
        float angle = slashVert.x, len = slashVert.y;
    
        deviation += trns(angle, min(intensity, len));
        intensity -= len;
    }
    
    vec2 coords = (gl_FragCoord.xy / u_viewport) * u_resolution + u_campos;
    coords = (coords + deviation - u_campos) / u_resolution;
    
    vec4 screen = texture2D(u_screenTexture, coords);
    
    vec4 tex = texture2D(u_texture, v_texCoords);
    tex = v_color * mix(tex, vec4(v_mix_color.rgb, tex.a), v_mix_color.a);
    
    gl_FragColor = screen * tex.a * u_blend + tex * tex.a;
}
