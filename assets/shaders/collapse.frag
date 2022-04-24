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
};

uniform Data u_datas[DATA_COUNT];
uniform highp sampler2D u_texture;
uniform highp vec4 u_view;
uniform mediump float u_fallScale;

varying highp vec2 v_texCoords;

vec4 getTexture(int index, vec2 pos){
    vec4 bound = u_datas[index].bound;
    pos -= bound.xy;
    
    return texture2D(u_datas[index].texture, pos / bound.yz);
}

void main(){
    vec4 base = texture2D(u_texture, v_texCoords);
    if(base.a < 0.99) discard;
    
    int index = int(base.r * float(DATA_COUNT));
    if(index >= DATA_COUNT) discard;

    vec2 worldPos = v_texCoords * u_view.zw + u_view.xy;
    vec2 dir = (v_texCoords - vec2(0.5)) * vec2(1.0, u_view.w / u_view.z);
    dir *= u_fallScale * u_datas[index].progress;
    worldPos += dir;
    
    if(texture2D(u_texture, (worldPos - u_view.xy) / u_view.zw) != base) discard;
    gl_FragColor = getTexture(index, worldPos);
}
