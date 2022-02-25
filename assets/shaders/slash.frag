#define HIGHP

varying lowp vec4 v_color;
varying lowp vec4 v_mix_color;
varying highp vec2 v_texCoords;

uniform highp sampler2D u_screenTexture;
uniform highp sampler2D u_texture;
uniform highp vec2 u_campos;
uniform highp vec2 u_resolution;
uniform vec2 u_viewport;

uniform float u_scale;

const int octaves = 3;
const float persistence = 0.4;

//https://www.shadertoy.com/view/Mls3RS
float noise(int x, int y){
    float fx = float(x);
    float fy = float(y);
    return 2.0 * fract(sin(dot(vec2(fx, fy), vec2(12.9898, 78.233))) * 43758.5453) - 1.0;
}

float smoothNoise(int x, int y){
    return
        noise(x, y) / 4.0 +
        (noise(x + 1, y) + noise(x - 1, y) + noise(x, y + 1) + noise(x, y - 1)) / 8.0 +
        (noise(x + 1, y + 1) + noise(x + 1, y - 1) + noise(x - 1, y + 1) + noise(x - 1, y - 1)) / 16.0;
}

float cosInterpolation(float x, float y, float n){
    float r = n * 3.1415926;
    float f = (1.0 - cos(r)) * 0.5;
    return x * (1.0 - f) + y * f;
}

float interpolationNoise(float x, float y){
    int ix = int(x);
    int iy = int(y);
    float fracx = x - float(int(x));
    float fracy = y - float(int(y));
    
    float v1 = smoothNoise(ix, iy);
    float v2 = smoothNoise(ix + 1, iy);
    float v3 = smoothNoise(ix, iy + 1);
    float v4 = smoothNoise(ix + 1, iy + 1);
    
    float i1 = cosInterpolation(v1, v2, fracx);
    float i2 = cosInterpolation(v3, v4, fracx);
    
    return cosInterpolation(i1, i2, fracy);
}

float perlinNoise2D(float x, float y){
    float sum = 0.0;
    float frequency = 0.0;
    float amplitude = 0.0;

    for(int i = 0; i < octaves; i++){
        frequency = pow(2.0, float(i));
        amplitude = pow(persistence, float(i));

        sum += interpolationNoise(x * frequency, y * frequency) * amplitude;
    }
    
    return sum;
}

void main(){
    vec2 c = gl_FragCoord.xy / u_viewport;
    vec2 coords = c * u_resolution + u_campos;

    float noise = perlinNoise2D(coords.x * u_scale, coords.y * u_scale);
    float center = (1.0 - abs(v_color.g - 0.5) * 2.0);
    center = pow(center - 1.0, 2.0) * -1.0 + 1.0;

    float angle = (v_color.r * 3.1415927 * 2.0), angleCos = cos(angle), angleSin = sin(angle);
    float intensity = (noise * 2.0 - 1.0) * center * v_color.b * 32.0;
    
    vec2 vec = vec2(intensity, 0.0);
    vec.x = vec.x * angleCos - vec.y * angleSin;
    vec.y = vec.x * angleSin + vec.y * angleCos;
    
    coords = (coords + vec - u_campos) / u_resolution;
    
    vec4 tex = texture2D(u_texture, v_texCoords) * v_mix_color;
    tex.a *= center;

    gl_FragColor = vec4(tex.rgb * tex.a, 1.0) + texture2D(u_screenTexture, coords);
}
