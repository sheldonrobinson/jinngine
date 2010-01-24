varying vec3 point;
varying vec3 facenormal;
uniform sampler2D texture0;
uniform sampler2D normalmap;

void main (void) {
	vec4 color = texture2D( texture0, gl_TexCoord[0].xy);
	vec3 normal = vec3( texture2D( normalmap, gl_TexCoord[0].xy)).xyz;
	//normal = vec3(0.0, 0.0, 1.0);
	normal = ((2.0*normal-1.0).xyz);
	
	//change space
	normal = vec3(-normal.x, normal.y, -normal.z);
	
	//transform to eye space
	normal = gl_NormalMatrix*normal;
	normal = normalize(normal); //normalize is required because the normal matrix can be scaled
	//phong shading using the normal map
	vec3 L = normalize(gl_LightSource[0].position.xyz - point);
	vec3 E = normalize(-point);
	vec3 R = normalize(-reflect(L,normal));

	float diff = /*gl_FrontLightProduct[0].diffuse*/ 0.5 * max(dot(normal,L), 0.0);

	vec3 spec = /*gl_FrontLightProduct[0].specular*/ 
               2.0 * pow(max(dot(R,E),0.0), 15.0);//gl_FrontMaterial.shininess);


	gl_FragColor = vec4((color*( 0.1 + diff + spec )).xyz, 1.0);  
	//gl_FragColor = color;
}