#version 300 es

precision highp float;

layout (location = 0) in vec2 inPosition;
layout (location = 1) in vec4 inVertexColor;
layout (location = 2) in float inColumnIndex;

out vec2 outPosition;
out vec4 outVertexColor;
out float outColumnIndex;

out vec4 inFragmentColor;

uniform vec2 size;
uniform float diameter;
uniform float currentColumnIndex;
uniform float maxColumnIndex;

float rand(vec2 co) {
    return fract(sin(dot(co.xy, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
    vec2 normInPosition = inPosition / size * 2.0 - vec2(1.0);

    gl_PointSize = diameter;
    gl_Position = vec4(normInPosition, 0.0, 1.0);

    if (inColumnIndex > currentColumnIndex) {
        outPosition = inPosition;
        outVertexColor = inVertexColor;
        outColumnIndex = inColumnIndex;
        inFragmentColor = outVertexColor;
    } else {
        float dispersionRadius = 1.0;
        float offsetX = (rand(vec2(gl_VertexID)) - 0.5) * dispersionRadius;
        float offsetY = (rand(vec2(gl_VertexID) * 2.0) - 0.5) * dispersionRadius;

        float additionalOffsetY = 0.0;
        float alpha = inVertexColor.w;
        if (currentColumnIndex >= maxColumnIndex && alpha >= 0.0) {
            alpha -= 0.005;
            additionalOffsetY = abs(offsetX) + abs(offsetY);
        }

        vec2 newPosition = inPosition + vec2(offsetX, offsetY + additionalOffsetY);

        outPosition = newPosition;
        outVertexColor = vec4(inVertexColor.xyz, alpha);
        inFragmentColor = outVertexColor;
        outColumnIndex = inColumnIndex;
    }
}
