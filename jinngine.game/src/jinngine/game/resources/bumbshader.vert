varying vec3 point;
varying vec3 facenormal;

void main(void) {
	facenormal = /*gl_NormalMatrix**/gl_Normal;
	point = vec3(gl_ModelViewMatrix * gl_Vertex); 
    gl_TexCoord[0] = gl_MultiTexCoord0;
    gl_Position = ftransform();
}

