in vec4 v_color;
in vec2 v_texCoord;

out vec4 fragColor;

uniform sampler2D u_sampler0;

void main(){
    vec4 emit = texture(u_sampler0, v_texCoord);
    fragColor = vec4(emit.rgb * emit.a + v_color.rgb, v_color.a);
}
