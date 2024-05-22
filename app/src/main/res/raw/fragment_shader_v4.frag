precision mediump float;            //  Set the default precision to medium we do not need high precision for a fragment shader

uniform vec3 u_LightPos;            //  The position of the light in eye space
uniform sampler2D u_Texture;        //  The input texture

varying vec3 v_Position;            //  Interpolated position for this fragment
varying vec4 v_Color;               //  This is the color from the verted interpolated across the triangle per fragment
varying vec3 v_Normal;
varying vec2 v_TexCoordinate;       //  Itherpolated texture coordonate per fragment

//  The entry point of our fragemnt shader
void main()
{
    //  Will be used for attenuation
    float distance = length(u_LightPos - v_Position);

    //  Get a lighting direction vector from the light to the vertex
    vec3 lightVector = normalize(u_LightPos -v_Position);

    //  Calculate the dot product of the light vector and vertex normal. If the normal and the light vector are
    //  pointing in the samme direction then it will get max Illumination
    float diffuse = max(dot(v_Normal, lightVector), 0.2);

    //  Add attenuation
    diffuse = diffuse * ( 1.0 / (1.0 + (0.10 * distance )));

    diffuse = diffuse + 0.3;

    //  Multiply the color by the diffuse illumination level to get teh final color output
    gl_FragColor = (v_Color * diffuse * texture2D(u_Texture, v_TexCoordinate ));
}