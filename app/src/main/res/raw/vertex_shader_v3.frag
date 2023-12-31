uniform mat4 u_MVPMatrix;       //  A constant representing the combined Model/View/Projection matrix
uniform mat4 u_MVMatrix;       //  A constant representing the combined Model/View Matrix

attribute vec4 a_Position;      //  Per-vertex position information we pass in
attribute vec4 a_Color;         //  Per-vertex color information we pass in
attribute vec3 a_Normal;        //  Per-vertex normal information we pass in

varying vec3 v_Position;        //  This will be passed into the fragment shader
varying vec4 v_Color;           //  This will be passed into the fragment shader
varying vec3 v_Normal;          //  This will be passed into the fragment shader

//  The main entry point of our vertex shader
void main()
{
    //  Transform the vertex into eye space.
    v_Position = vec3(u_MVMatrix * a_Position);

    //  Pass through the color
    v_Color = a_Color;

    //  Transform the normal's orientation into eye space
    v_Normal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));

    //  gl_Position is a special variabl used to store the final position
    //  Multiply the vertex by the matrix to get the final point in normalized screen coordonates
    gl_Position = u_MVPMatrix * a_Position;
}