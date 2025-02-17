#if __VERSION__ >= 130
    #define varying in
    out vec4 mgl_FragColor;
#else
    #define mgl_FragColor gl_FragColor
#endif

uniform mat4 MVP;
uniform vec2 viewSize;
uniform mat4 iV;
uniform mat4 iP;

uniform sampler3D tex;
uniform sampler3D normals;
uniform sampler2D colorMap;
uniform int depthSampleCount;
uniform vec3 aabbMin;
uniform vec3 aabbMax;

uniform bool orthographic;

uniform float brightness;

#include volumeBase.glsl

void main(){
    vec2 vUV = 2.0 * (gl_FragCoord.xy + vec2(0.5, 0.5)) / viewSize - 1.0;
    Ray ray;
    if (orthographic) {
        ray = createRayOrthographic(vUV);
    } else {
        ray = createRayPerspective(vUV);
    }
    vec3 aabb[2];
    aabb[0] = aabbMin;
    aabb[1] = aabbMax;
    float tmin = 0.0;
    float tmax = 0.0;
    intersect(ray, aabb, tmin, tmax);
    if (tmax < tmin){
        discard;
        return;
    }
    vec3 start = (ray.origin.xyz + tmin*ray.direction.xyz - aabb[0])/(aabb[1]-aabb[0]);
    vec3 end = (ray.origin.xyz + tmax*ray.direction.xyz - aabb[0])/(aabb[1]-aabb[0]);

    float len = distance(end, start);
    int sampleCount = int(float(depthSampleCount)*len);

    float px = 0.0;
    vec4 pxColor = vec4(0.0, 0.0, 0.0, 0.0);
    vec3 texCo = vec3(0.0, 0.0, 0.0);
    float tt = tmin;
    float px0 = 0.0;
    for(int count = 0; count < sampleCount; count++){

        texCo = mix(end, start, float(count)/float(sampleCount));// - originOffset;

        px = max(px, texture3D(tex, texCo).r);

        if (px > px0) {
            tt = tmax - float(count)/float(sampleCount)*(tmax - tmin);
        }

        px0 = px;

        if(px >= 0.99){
            break;
        }
    }
    pxColor = texture2D(colorMap, vec2(px, 0.0));

    mgl_FragColor = pxColor * brightness;

    gl_FragDepth = calculateDepth(ray.origin + ray.direction * tt);
    if (pxColor.a < 0.01) {
        gl_FragDepth = 1.0;
    }
}