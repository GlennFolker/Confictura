#define HIGHP

varying float v_depth;

vec4 pack(float value){
    value = value / 150.0;

    vec4 enc = vec4(1.,255.,65025.,16581375.) * value;
    enc = fract(enc);
    enc -= enc.yzww * vec2(1./255., 0.).xxxy;
    return enc;
    /*float exp = floor(log2(abs(value)) + 1.0);
    value = value / exp2(exp);
    value = (value + 1.0) * (256.0 * 256.0 * 256.0 - 1.0) / (2.0 * 256.0 * 256.0 * 256.0);

    vec4 pack = fract(value * vec4(1.0, 256.0, 256.0 * 256.0, 256.0 * 256.0 * 256.0));
    return vec4(pack.xyz - pack.yzw / 256.0 + 1.0 / 512.0, (exp + 127.5) / 256.0);*/
}

void main(){
    gl_FragColor = pack(v_depth);
}
