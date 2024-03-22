varying vec3 v_color;
varying float v_height;
varying float v_darkness;

void main(){
    float dark = min(v_height / ((1.0 - v_darkness) * 32.0), 1.0);
    dark = smoothstep(0.0, 1.0, dark);

    gl_FragColor = vec4(v_color * (v_darkness + dark * (1.0 - v_darkness)), 1.0);
}
