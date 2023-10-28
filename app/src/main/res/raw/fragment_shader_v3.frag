precision mediump float;            //  Set the default precision to medium we do not need high precision for a fragment shader

uniform vec3 u_LightPos;            //  The position of the light in eye space

varying vec3 v_Position;            //  Interpolated positionfor this fragment
varying vec4 v_Color;               //  This is the color from the verted interpolated across the triangle per fragment
varying vec3 v_Normal;

//  The entry point of our fragemnt shader
void main()
{
    //  Will be used for attenuation
    float distance = length(u_LightPos - v_Position);

    //  Get a lighting direction vector from the light to the vertex
    vec3 lightVector = normalize(u_LightPos -v_Position);

    //  Calculate the dot product of the light vector and vertex normal. If the normal and the light vector are
    //  pointing in the samme direction then it will get max Illumination
    float diffuse = max(dot(v_Normal, lightVector), 0.1);

    //  Add attenuation
    diffuse = diffuse * ( 1.0 / (1.0 + (0.25 * distance * distance )));

    //  Multiply the color by the diffuse illumination level to get teh final color output
    gl_FragColor.rgb = v_Color.rgb * diffuse;
}