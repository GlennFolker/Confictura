varying vec3 v_near;
varying vec3 v_far;

vec4 grid(vec3 pos, float scale) {
    vec2 coord = pos.xz * scale;
    vec2 derivative = fwidth(coord);
    vec2 grid = abs(fract(coord - 0.5) - 0.5) / derivative;

    float line = min(grid.x, grid.y);
    return vec4(0.2, 0.2, 0.2, 1.0 - min(line, 1.0));
}

void main() {
    float t = -v_near.y / (v_far.y - v_near.y);
    if(t <= 0.0) discard;

    vec3 pos = v_near + t * (v_far - v_near);
    gl_FragColor = grid(pos, 1.0) * vec4(1.0, 1.0, 1.0, 1.0 - t * 2.0);
}
