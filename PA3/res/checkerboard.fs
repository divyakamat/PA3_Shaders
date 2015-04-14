#version 120

varying vec3 varyingColour;
varying vec3 position;

void main() {
    gl_FragColor = vec4(varyingColour,1);

    float angle = atan(-position.z,position.x)*180/3.1415;

    if ( mod(ceil(position.y/0.3) + floor(angle/10),2) == 1 ) {
        gl_FragColor += vec4(0.3, 0.3, 0.3, 0.0);
    } else {
        gl_FragColor -= vec4(0.3, 0.3, 0.3, 0.0);
    }
}