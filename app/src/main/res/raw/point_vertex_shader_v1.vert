uniform mat4 u_MVPMatrix;   //  constant Model View Projection Matrix
attribute vec4 a_Position;  //  Input position information

void main()
{
    gl_Position = u_MVPMatrix * a_Position;
    gl_PointSize = 5.0;
}