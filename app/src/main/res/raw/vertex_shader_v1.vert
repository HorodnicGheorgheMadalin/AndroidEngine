uniform mat4 u_ProductMatrix;   //  constant Move View Projection Matrix
attribute vec4 a_Position;  //  Input position information
attribute vec4 a_Color;     //  Input color information

varying vec4 v_Color;       //  Color for the vertex shader

void main()
{
    v_Color = a_Color;

    gl_Position = u_ProductMatrix * a_Position;
}