#define HIGHP

in vec2 v_texCoords;

out vec4 fragColor;

uniform mat4 u_invProj;
uniform mat4 u_invProjView;
uniform vec3 u_camPos;
uniform vec3 u_relCamPos;
uniform vec2 u_viewport;

uniform float u_radius;

uniform sampler2D u_src;
uniform sampler2D u_srcDepth;

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

void main(){
    vec2 uv = gl_FragCoord.xy / u_viewport;
    vec2 ndc = uv * 2.0 - 1.0;
    vec4 view = u_invProjView * vec4(ndc, -1.0, 1.0);
    vec3 ray = normalize(view.xyz / view.w - u_camPos);

    float srcDepth = texture(u_srcDepth, uv).r;
    vec4 viewDepth = u_invProj * vec4(uv.x * 2.0 - 1.0, uv.y * 2.0 - 1.0, srcDepth * 2.0 - 1.0, 1.0);
    float depth = length(viewDepth.xyz / viewDepth.w);

    vec2 bound;
    if(intersectSphere(u_relCamPos, ray, u_radius, bound) && depth > bound.x){
        fragColor = texture(u_src, v_texCoords);
        gl_FragDepth = srcDepth;
    }else{
        discard;
    }
}
