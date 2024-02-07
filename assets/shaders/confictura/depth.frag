#define HIGHP

varying float v_depth;

vec4 pack(float value){
    value = value / 150.0;

    vec4 enc = vec4(1.0, 255.0, 65025.0, 16581375.0) * value;
    enc = fract(enc);
    enc -= enc.yzww * vec2(1.0 / 255.0, 0.0).xxxy;
    return enc;
}

void main(){
    gl_FragColor = pack(v_depth);
}
