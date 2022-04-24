// Preprocessor(s):
// DATA_COUNT: The amount of collapse datas.

#define HIGHP

struct Data{
    // Boundary of the collapse data.
    highp vec4 bound;
    // Progress of the data, [0..1].
    highp float progress;
    // The texture of the area before collapsing.
    highp sampler2D texture;
    // The collapsing floor tiles, acts as if it was a stencil.
    highp sampler2D stencil;
};

uniform Data u_datas[DATA_COUNT];
uniform highp sampler2D u_texture;
uniform highp vec4 u_view;
uniform mediump float u_fallScale;

varying highp vec2 v_texCoords;

void main(){
    vec4 base = texture2D(u_texture, v_texCoords);
    if(base.a < 0.99) discard;

    int index = int(base.r * float(DATA_COUNT));
    if(index >= DATA_COUNT) discard;

    float prog = u_datas[index].progress;

    vec2 worldPos = v_texCoords * u_view.zw + u_view.xy;
    vec2 dir = (v_texCoords - vec2(0.5)) * vec2(1.0, u_view.w / u_view.z);
    worldPos += dir * u_fallScale * (pow(prog - 1.0, 3.0) + 1.0);

    vec4 bound = u_datas[index].bound;
    vec2 tpos = (worldPos - bound.xy) / bound.zw;

    if(
        texture2D(u_datas[index].stencil, tpos) != base ||
        tpos.x < 0.0 || tpos.x > 1.0 ||
        tpos.y < 0.0 || tpos.y > 1.0
    ){
        gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
        return;
    }

    gl_FragColor = texture2D(u_datas[index].texture, tpos) * vec4((1.0 - prog), (1.0 - prog), (1.0 - prog), 1.0);
}
