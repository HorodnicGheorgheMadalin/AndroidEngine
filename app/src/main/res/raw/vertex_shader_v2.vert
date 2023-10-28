uniform mat4 u_MVPMatrix;   //  constant Model View Projection Matrix
uniform mat4 u_MVMatrix;    //  constant Model View Matrix
uniform vec3 u_LightPos;    //  Input Light position information

attribute vec4 a_Position;  //  Input position information
attribute vec3 a_Normal;    //  Input normal information
attribute vec4 a_Color;     //  Input color information

varying vec4 v_Color;       //  Color for the vertex shader

void main()
{
    //  Transform the vertex into eye space
    vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);
    //  Transform the normal orientation int eye space
    vec3 modelViewNormal = vec3(u_MVPMatrix * vec4(a_Normal, 0.0));
    // Wil be used for attenuation
    float distance = length(u_LightPos - modelViewVertex);
    // Get the lighting direction vector from the light to the vertex
    vec3 lightVector = normalize(u_LightPos - modelViewVertex);
    //  Calculate the dot product of the light vector and vertex normal. If the normal and the light
    //  point are in the same direction it will get max illumination.
    float diffuse = max(dot(modelViewNormal, lightVector), 0.1);
    //  Attenuate the light based on the distance
    diffuse = diffuse * ( 1.0 / ( 1.0 + ( 0.25 * distance * distance)));
    //  Multiply the color by the illumination level. It will be interpoled accros the triangle
    v_Color = a_Color * diffuse;
    //  gl_Position is a special variable used to store the final position
    //  Multiply the vertex by the matrix to get the final point in normalized screen coordonates.
    gl_Position = u_MVPMatrix * a_Position;
}