// Preprocessor(s):
// DATA_COUNT: The amount of collapse datas.

struct Data{
    // Boundary of the collapse data.
    vec4 bound;
    // Progress of the data, 0-1.
    float progress;
    // Resolution of the screen textures.
    //vec2 resolution;
    // Screen textures covering the entire collapse area.
    //sampler2D textures[TEX_WIDTH * TEX_HEIGHT];
    sampler2D texture;
};

uniform Data u_datas[DATA_COUNT];
uniform sampler2D u_texture;
uniform vec4 u_view;
uniform float u_fallScale;

varying vec2 v_texCoords;

vec4 getTexture(int index, vec2 pos){
    /*pos -= u_datas[index].bound.xy;
    if(
        pos.x < 0.0 || pos.x > u_datas[index].bound.z ||
        pos.y < 0.0 || pos.y > u_datas[index].bound.w
    ) return vec4(0.0);

    vec2 res = u_datas[index].resolution;
    ivec2 start = ivec2(int(pos.x * 4.0 / res.x), int(pos.y * 4.0 / res.y));
    vec2 startf = vec2(float(start.x) * res.x, float(start.y) * res.y);
    vec2 endf = startf + res;
    
    return texture2D(u_datas[index].textures[start.y * TEX_WIDTH + start.x], (pos - startf) / endf);*/
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
