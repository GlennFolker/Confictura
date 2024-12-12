#define HIGHP

in vec2 v_texCoords;

out vec4 fragColor;

uniform sampler2D u_texture;

void main(){
    fragColor = texture2D(u_texture, v_texCoords);
}
