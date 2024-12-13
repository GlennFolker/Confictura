#define HIGHP

in vec2 v_texCoords;

out vec4 fragColor;

uniform sampler2D u_src;
uniform sampler2D u_srcDepth;
uniform sampler2D u_ref;

void main(){
    float srcDepth = texture(u_srcDepth, v_texCoords).r;
    float dstDepth = texture(u_ref, v_texCoords).r;

    if(srcDepth < dstDepth) discard;
    fragColor = texture(u_src, v_texCoords);
    gl_FragDepth = srcDepth;
}
