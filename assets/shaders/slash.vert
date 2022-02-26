#define HIGHP

attribute highp vec2 a_position;
attribute lowp vec4 a_color;
attribute highp vec2 a_texCoord0;
attribute lowp vec4 a_mix_color;
attribute highp vec3 a_slashInput;

uniform mat4 u_projTrans;

varying lowp vec4 v_color;
varying lowp vec4 v_mix_color;
varying highp vec2 v_texCoords;
varying highp vec2 v_slashData;
flat varying int v_slashIndex;

void main(){
    v_color = a_color;
    v_color.a = v_color.a * (255.0 / 254.0);
    
    v_mix_color = a_mix_color;
    v_mix_color.a *= (255.0 / 254.0);
    
    v_texCoords = a_texCoord0;
    v_slashData = vec2(1.0 - abs(a_slashInput.x - 0.5) * 2.0, a_slashInput.y);
    v_slashIndex = int(a_slashInput.z);
    
    gl_Position = u_projTrans * vec4(a_position, 0.0, 1.0);
}
