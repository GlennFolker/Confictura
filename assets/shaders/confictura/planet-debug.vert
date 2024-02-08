attribute vec2 a_position;

varying vec3 v_near;
varying vec3 v_far;

uniform mat4 u_invProj;

vec3 unproj(float x, float y, float z){
    vec4 point = u_invProj * vec4(x, y, z, 1.0);
    return point.xyz / point.w;
}

void main(){
    vec2 pos = a_position;
    v_near = unproj(pos.x, pos.y, 0.0);
    v_far = unproj(pos.x, pos.y, 1.0);
    gl_Position = vec4(pos, 0.0, 1.0);
}
