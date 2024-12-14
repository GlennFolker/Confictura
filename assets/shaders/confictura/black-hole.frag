#define HIGHP

in vec2 v_texCoords;

out vec4 fragColor;

uniform vec3 u_camPos;
uniform vec2 u_viewport;
uniform vec3 u_relCamPos;
uniform vec3 u_center;

uniform float u_radius;
uniform float u_horizon;

uniform mat4 u_cubeView[6];
uniform mat4 u_cubeInvView[6];
uniform mat4 u_projView;
uniform mat4 u_invProj;
uniform vec2 u_depthRange;

uniform samplerCube u_ref;
uniform samplerCube u_depth;

bool intersectSphere(vec3 origin, vec3 dir, float radius, out vec2 result){
    float b = dot(origin, dir);
    float c = dot(origin, origin) - radius * radius;

    float d = b * b - c;
    if(d < 0.0) return false;

    d = sqrt(d);
    float near = -b - d;
    float far = -b + d;

    result = vec2(near, far);
    return true;
}

int cubeIndex(vec3 dir){
    vec3 absDir = abs(dir);
    if(absDir.x >= absDir.y && absDir.x >= absDir.z) {
        return (dir.x > 0.0) ? 0 : 1;
    }else if(absDir.y >= absDir.x && absDir.y >= absDir.z) {
        return (dir.y > 0.0) ? 2 : 3;
    }else {
        return (dir.z > 0.0) ? 4 : 5;
    }
}

vec3 rotate(vec3 v, vec3 axis, float angle){
    float cosTheta = cos(angle);
    float sinTheta = sin(angle);

    return
        v * cosTheta +
        cross(axis, v) * sinTheta +
        axis * dot(axis, v) * (1.0 - cosTheta);
}

void main(){
    vec2 ndc = (gl_FragCoord.xy / u_viewport) * 2.0 - 1.0;
    vec4 view = u_invProj * vec4(ndc, -1.0, 1.0);
    vec3 ray = normalize(view.xyz / view.w - u_camPos);

    int faceIndex = cubeIndex(ray);
    float near = u_depthRange.x, far = u_depthRange.y;

    float zCamera = (near * far) / (far + (near - far) * texture(u_depth, ray).r);
    vec3 rayDirCameraSpace = (u_cubeView[faceIndex] * vec4(ray, 0.0)).xyz;
    vec3 worldPos = (u_cubeInvView[faceIndex] * vec4(zCamera * rayDirCameraSpace, 1.0)).xyz;

    vec2 bound;
    if(intersectSphere(u_relCamPos, ray, u_radius, bound) && bound.x < distance(u_camPos, worldPos)){
        far = gl_DepthRange.far, near = gl_DepthRange.near;
        vec4 clip = u_projView * vec4(u_camPos + ray * bound.x, 1.0);
        gl_FragDepth = (((far - near) * (clip.z / clip.w)) + near + far) / 2.0;

        vec3 hit = u_camPos + ray * (bound.x + bound.y) / 2.0 - u_center;
        vec3 axis = normalize(cross(hit, u_center - u_camPos));
        float dist = length(hit) / u_radius;
        float inner = 1.0 - smoothstep(0.0, 1.0, pow(1.0 - max(-dist + u_horizon, 0.0) / u_horizon, 30.0));

        fragColor = mix(
            texture(u_ref, rotate(ray, axis, 2.0944 * pow(1.0 - dist, 3.0))),
            vec4(vec3(0.0), 1.0),
            inner
        );
    }else{
        discard;
    }
}
