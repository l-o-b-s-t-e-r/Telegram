#version 300 es

precision highp float;

in vec4 inFragmentColor;
out vec4 outFragmentColor;

void main() {
  vec2 circCoord = 2.0 * gl_PointCoord - 1.0;
  if (dot(circCoord, circCoord) > 1.0) {
    discard;
  }
  if (inFragmentColor.w == 0.0) {
    discard;
  }

  outFragmentColor = inFragmentColor;
}