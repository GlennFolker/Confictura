uniform sampler2D u_sampler0;

varying vec4 v_color;
varying vec2 v_texCoord;

void main(){
    vec4 emit = texture2D(u_sampler0, v_texCoord);
    gl_FragColor = vec4(emit.rgb * emit.a + v_color.rgb, v_color.a);
}
