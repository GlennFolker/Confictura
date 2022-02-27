#define HIGHP

attribute vec2 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;
attribute vec4 a_mix_color;
attribute vec3 a_slashInput;

uniform mat4 u_projTrans;

varying lowp vec4 v_color;
varying lowp vec4 v_mix_color;
varying highp vec2 v_texCoords;
varying highp vec3 v_slashData;

void main(){
    v_color = a_color;
    v_color.a = v_color.a * (255.0 / 254.0);
    
    v_mix_color = a_mix_color;
    v_mix_color.a *= (255.0 / 254.0);
    
    v_texCoords = a_texCoord0;
    v_slashData = a_slashInput;
    
    gl_Position = u_projTrans * vec4(a_position, 0.0, 1.0);
}
