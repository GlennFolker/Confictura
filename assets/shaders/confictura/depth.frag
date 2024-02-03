#define HIGHP

varying float v_depth;

vec4 pack(float value){
    float exp = floor(log2(abs(value)) + 1.0);
    value /= exp2(exp);
    value = (value + 1.0) * (256.0 * 256.0 * 256.0 - 1.0) / (2.0 * 256.0 * 256.0 * 256.0);

    vec4 packed = fract(value * vec4(1.0, 256.0, 256.0 * 256.0, 256.0 * 256.0 * 256.0));
    return vec4(packed.xyz - packed.yzw / 256.0 + 1.0 / 512.0, (exp + 127.5) / 256.0);
}

void main(){
    gl_FragColor = pack(v_depth);
}
