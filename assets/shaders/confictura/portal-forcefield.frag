/*
This shader borrows the 4D Simplex noise implementation from https://github.com/hughsk/glsl-noise/blob/master/simplex/4d.glsl,
therefore here is the required copyright notice.

    Copyright (C) 2011 by Ashima Arts (Simplex noise)

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.
*/

#define HIGHP

varying vec3 v_position;
varying float v_progress;
varying vec4 v_color;

uniform float u_radius;

uniform vec3 u_camPos;
uniform vec3 u_relCamPos;
uniform vec3 u_center;
uniform vec3 u_light;

uniform float u_time;
uniform vec4 u_baseColor;

uniform sampler2D u_topology;
uniform vec2 u_viewport;

vec4 mod289(vec4 x){
    return x - floor(x * (1.0 / 289.0)) * 289.0;
}

float mod289(float x){
    return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 permute(vec4 x){
    return mod289(((x * 34.0) + 1.0) * x);
}

float permute(float x){
    return mod289(((x * 34.0) + 1.0) * x);
}

vec4 taylorInvSqrt(vec4 r){
    return 1.79284291400159 - 0.85373472095314 * r;
}

float taylorInvSqrt(float r){
    return 1.79284291400159 - 0.85373472095314 * r;
}

vec4 grad4(float j, vec4 ip){
    const vec4 ones = vec4(1.0, 1.0, 1.0, -1.0);
    vec4 p, s;

    p.xyz = floor(fract (vec3(j) * ip.xyz) * 7.0) * ip.z - 1.0;
    p.w = 1.5 - dot(abs(p.xyz), ones.xyz);
    s = vec4(lessThan(p, vec4(0.0)));
    p.xyz = p.xyz + (s.xyz * 2.0 - 1.0) * s.www;

    return p;
}

const float F4 = 0.309016994374947451;

float simplex_noise(vec4 v){
    const vec4 C = vec4(
        0.138196601125011,
        0.276393202250021,
        0.414589803375032,
        -0.447213595499958
    );

    vec4 i = floor(v + dot(v, vec4(F4)));
    vec4 x0 = v - i + dot(i, C.xxxx);

    vec4 i0;
    vec3 isX = step(x0.yzw, x0.xxx);
    vec3 isYZ = step(x0.zww, x0.yyz);

    i0.x = isX.x + isX.y + isX.z;
    i0.yzw = 1.0 - isX;
    i0.y += isYZ.x + isYZ.y;
    i0.zw += 1.0 - isYZ.xy;
    i0.z += isYZ.z;
    i0.w += 1.0 - isYZ.z;

    vec4 i3 = clamp(i0, 0.0, 1.0);
    vec4 i2 = clamp(i0 - 1.0, 0.0, 1.0);
    vec4 i1 = clamp(i0 - 2.0, 0.0, 1.0);

    vec4 x1 = x0 - i1 + C.xxxx;
    vec4 x2 = x0 - i2 + C.yyyy;
    vec4 x3 = x0 - i3 + C.zzzz;
    vec4 x4 = x0 + C.wwww;

    i = mod289(i);
    float j0 = permute(permute(permute(permute(i.w) + i.z) + i.y) + i.x);
    vec4 j1 = permute(permute(permute(permute(
    i.w + vec4(i1.w, i2.w, i3.w, 1.0))
    + i.z + vec4(i1.z, i2.z, i3.z, 1.0))
    + i.y + vec4(i1.y, i2.y, i3.y, 1.0))
    + i.x + vec4(i1.x, i2.x, i3.x, 1.0));

    vec4 ip = vec4(1.0 / 294.0, 1.0 / 49.0, 1.0 / 7.0, 0.0);

    vec4 p0 = grad4(j0, ip);
    vec4 p1 = grad4(j1.x, ip);
    vec4 p2 = grad4(j1.y, ip);
    vec4 p3 = grad4(j1.z, ip);
    vec4 p4 = grad4(j1.w, ip);

    vec4 norm = taylorInvSqrt(vec4(dot(p0, p0), dot(p1, p1), dot(p2, p2), dot(p3, p3)));
    p0 *= norm.x;
    p1 *= norm.y;
    p2 *= norm.z;
    p3 *= norm.w;
    p4 *= taylorInvSqrt(dot(p4, p4));

    vec3 m0 = max(0.6 - vec3(dot(x0, x0), dot(x1, x1), dot(x2, x2)), 0.0);
    vec2 m1 = max(0.6 - vec2(dot(x3, x3), dot(x4, x4)), 0.0);
    m0 = m0 * m0;
    m1 = m1 * m1;

    float value = 49.0 * (dot(m0 * m0, vec3(dot(p0, x0), dot(p1, x1), dot(p2, x2))) + dot(m1 * m1, vec2(dot(p3, x3), dot(p4, x4))));
    return clamp((value + 1.0) / 2.0, 0.0, 1.0);
}

float ridged_noise(vec4 v){
    return abs(simplex_noise(v) - 0.5) * 2.0;
}

float octave_noise(vec3 v, int octaves, float scale, float lacunarity, float persistence){
    float total = 0.0;

    float frequency = scale;
    float magnitude = 1.0;
    float highest = 0.0;

    for(int i = 0; i < octaves; i++){
        highest += magnitude;
        total += ridged_noise(vec4(v * frequency, u_time)) * magnitude;

        frequency *= lacunarity;
        magnitude *= persistence;
    }

    return total / highest;
}

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

float unpack(vec4 pack){
    return dot(pack, 1.0 / vec4(1.0, 255.0, 65025.0, 16581375.0)) * 150.0;
}

void main(){
    vec3 eye = u_relCamPos;
    vec3 ray = normalize(v_position - u_camPos);
    vec3 normal = normalize(v_position - u_center);

    vec2 intersect = intersect(eye, ray, u_radius - 0.01);
    float topo = unpack(texture2D(u_topology, gl_FragCoord.xy / u_viewport));

    float dst = (intersect.y - intersect.x) / ((u_radius - 0.01) * 2.0);
    float noise = octave_noise(vec3(eye + ray * intersect.x), 4, 1.8, 1.8, 0.67);
    noise = pow(noise - 1.0, 3.0) + 1.0;
    float light = (dot(normal, -u_light) + 1.0) / 2.0;

    float outer = 0.4;
    float inner = 0.83;
    float bound = dst + noise * 0.33;

    float medium = pow(bound, 2.4);

    float base = medium * 0.33;
    if(bound < outer){
        base += pow(smoothstep(0.0, 1.0, bound / outer), 3.0);
    }else if(bound < inner){
        base += pow(smoothstep(0.0, 1.0, 1.0 - (bound - outer) / (inner - outer)), 1.6);
    }

    base *= 0.2 + light * 0.4;
    if(topo >= intersect.x && topo <= intersect.y){
        float hit = 1.0 - clamp(min(topo - intersect.x, intersect.y - topo) / 0.16, 0.0, 1.0);
        base += (max(base, 1.0) - base) * pow(hit, 3.2);
    }

    vec3 baseColor = u_baseColor.xyz * u_baseColor.a * base;

    float outline = 0.1 + pow(sin(u_time * 2.0 + v_progress * 16.0) / 2.0 + 0.5, 7.0) * 0.9;
    outline *= medium * light;

    vec3 outlineColor = v_color.xyz * pow(max(1.0 - v_color.a - 0.5, 0.0) * 2.0, 4.0) * outline;
    gl_FragColor = vec4(baseColor + outlineColor, 1.0);
}
