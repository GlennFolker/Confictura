in vec3 v_color;
in float v_height;
in float v_darkness;

out vec4 fragColor;

void main(){
    float dark = min(v_height / ((1.0 - v_darkness) * 32.0), 1.0);
    dark = smoothstep(0.0, 1.0, dark);

    fragColor = vec4(v_color * (v_darkness + dark * (1.0 - v_darkness)), 1.0);
}
