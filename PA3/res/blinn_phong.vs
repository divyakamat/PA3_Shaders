#version 120

varying vec3 varyingColour;

void main() {

    varyingColour = gl_Color.xyz;
	vec3 vertexPosition = (gl_ModelViewMatrix * gl_Vertex).xyz;
	vec3 lightDirection = normalize(gl_LightSource[0].position.xyz - vertexPosition);
	vec3 surfaceNormal  = (gl_NormalMatrix * gl_Normal).xyz;

	float diffuseLightIntensity = max(0, dot(surfaceNormal, lightDirection));
	varyingColour += diffuseLightIntensity * gl_FrontMaterial.diffuse.rgb;
	varyingColour += gl_LightModel.ambient.rgb;
	vec3 reflectionDirection = normalize(reflect(-lightDirection, surfaceNormal));

	float specular = max(dot(surfaceNormal, gl_LightSource[0].halfVector.xyz),0.0);

	if (diffuseLightIntensity != 0) {
	    float fspecular = pow(specular, gl_FrontMaterial.shininess);
        fspecular = clamp(fspecular, 0.0, 1.0);
		varyingColour.rgb += gl_FrontMaterial.specular.rgb * gl_LightSource[0].specular.rgb * vec3(fspecular, fspecular, fspecular);
	}

    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}