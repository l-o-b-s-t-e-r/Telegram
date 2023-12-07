package org.telegram.ui.Components.spoilers;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLExt;
import android.opengl.GLES20;
import android.opengl.GLES31;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RLottieDrawable;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;


public class DisintegrationEffect {

    public final int MAX_FPS;
    private final double MIN_DELTA;
    private final double MAX_DELTA;

    public void pause(boolean pause) {
        thread.pause(pause);
    }

    private Bitmap bitmap;
    private final ViewGroup textureViewContainer;
    private final TextureView textureView;
    private DisintegrationThread thread;
    private int width, height;
    private float top;
    private int left;
    public boolean destroyed;

    public void destroy() {
        if (!destroyed) {
            destroyed = true;
            if (thread != null) {
                thread.halt();
                thread = null;
            }
            textureViewContainer.removeView(textureView);
        }
    }

    public DisintegrationEffect(ViewGroup container, Bitmap bitmap, int width, int height, float top, int left) {
        MAX_FPS = (int) AndroidUtilities.screenRefreshRate;
        MIN_DELTA = 1.0 / (2 * MAX_FPS);
        MAX_DELTA = MIN_DELTA * 4;

        this.width = width;
        this.height = height;
        this.top = top;
        this.left = left;

        this.bitmap = bitmap;
        textureViewContainer = container;
        textureView = new TextureView(textureViewContainer.getContext()) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                setMeasuredDimension(DisintegrationEffect.this.width, DisintegrationEffect.this.height);
            }
        };
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                if (thread == null) {
                    thread = new DisintegrationThread(surface, width, height, DisintegrationEffect.this::destroy);
                    thread.start();
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
                if (thread != null) {
                    thread.updateSize(width, height);
                }
            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                if (thread != null) {
                    thread.halt();
                    thread = null;
                }
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

            }
        });

        FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT);
        frameLayoutParams.topMargin = (int) top;

        textureView.setOpaque(false);
        textureViewContainer.addView(textureView, frameLayoutParams);
    }

    private class DisintegrationThread extends Thread {
        private volatile boolean running = true;
        private volatile boolean paused = false;

        private final Runnable destroy;
        private final SurfaceTexture surfaceTexture;
        private final Object resizeLock = new Object();
        private boolean resize;
        private int width, height;
        private int particlesX, particlesY;
        private int particlesCount;
        private float diameter = AndroidUtilities.dpf2(1f);

        public DisintegrationThread(SurfaceTexture surfaceTexture, int width, int height, Runnable destroy) {
            this.destroy = destroy;
            this.surfaceTexture = surfaceTexture;
            this.width = width;
            this.height = height;
            this.particlesCount = particlesCount();
        }

        private int particlesCount() {
            particlesX = (int) (bitmap.getWidth() / diameter);
            particlesY = (int) (bitmap.getHeight() / diameter);

            return particlesX * particlesY;
        }

        public void updateSize(int width, int height) {
            synchronized (resizeLock) {
                resize = true;
                this.width = width;
                this.height = height;
            }
        }

        public void halt() {
            running = false;
        }

        public void pause(boolean paused) {
            this.paused = paused;
        }

        @Override
        public void run() {
            init();
            long lastTime = System.nanoTime();
            while (running) {
                final long now = System.nanoTime();
                double Δt = (now - lastTime) / 1_000_000_000.;
                lastTime = now;

                if (Δt < MIN_DELTA) {
                    double wait = MIN_DELTA - Δt;
                    try {
                        long milli = (long) (wait * 1000L);
                        int nano = (int) ((wait - milli / 1000.) * 1_000_000_000);
                        sleep(milli, nano);
                    } catch (Exception ignore) {
                    }
                    Δt = MIN_DELTA;
                } else if (Δt > MAX_DELTA) {
                    Δt = MAX_DELTA;
                }

                while (paused) {
                    try {
                        sleep(1000);
                    } catch (Exception ignore) {
                    }
                }

                checkResize();
                drawFrame((float) Δt);
            }
            die();
            AndroidUtilities.cancelRunOnUIThread(this.destroy);
            AndroidUtilities.runOnUIThread(this.destroy);
        }

        private EGL10 egl;
        private EGLDisplay eglDisplay;
        private EGLConfig eglConfig;
        private EGLSurface eglSurface;
        private EGLContext eglContext;

        private int drawProgram;
        private int sizeHandle;
        private int diameterHandle;
        private int currentColumnIndexHandle;
        private int maxColumnIndexHandle;

        private int currentBuffer = 0;
        private int[] particlesData;

        private void init() {
            egl = (EGL10) EGLContext.getEGL();

            eglDisplay = egl.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            if (eglDisplay == egl.EGL_NO_DISPLAY) {
                running = false;
                return;
            }
            int[] version = new int[2];
            if (!egl.eglInitialize(eglDisplay, version)) {
                running = false;
                return;
            }

            int[] configAttributes = {
                    EGL14.EGL_RED_SIZE, 8,
                    EGL14.EGL_GREEN_SIZE, 8,
                    EGL14.EGL_BLUE_SIZE, 8,
                    EGL14.EGL_ALPHA_SIZE, 8,
                    EGL14.EGL_RENDERABLE_TYPE, EGLExt.EGL_OPENGL_ES3_BIT_KHR,
                    EGL14.EGL_NONE
            };
            EGLConfig[] eglConfigs = new EGLConfig[1];
            int[] numConfigs = new int[1];
            if (!egl.eglChooseConfig(eglDisplay, configAttributes, eglConfigs, 1, numConfigs)) {
                running = false;
                return;
            }
            eglConfig = eglConfigs[0];

            int[] contextAttributes = {
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
                    EGL14.EGL_NONE
            };
            eglContext = egl.eglCreateContext(eglDisplay, eglConfig, egl.EGL_NO_CONTEXT, contextAttributes);
            if (eglContext == null) {
                running = false;
                return;
            }

            eglSurface = egl.eglCreateWindowSurface(eglDisplay, eglConfig, surfaceTexture, null);
            if (eglSurface == null) {
                running = false;
                return;
            }

            if (!egl.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
                running = false;
                return;
            }

            genParticlesData();

            // draw program (vertex and fragment shaders)
            int vertexShader = GLES31.glCreateShader(GLES31.GL_VERTEX_SHADER);
            int fragmentShader = GLES31.glCreateShader(GLES31.GL_FRAGMENT_SHADER);
            if (vertexShader == 0 || fragmentShader == 0) {
                running = false;
                return;
            }
            GLES31.glShaderSource(vertexShader, RLottieDrawable.readRes(null, R.raw.disintegration_vertex) + "\n// " + Math.random());
            GLES31.glCompileShader(vertexShader);
            int[] status = new int[1];
            GLES31.glGetShaderiv(vertexShader, GLES31.GL_COMPILE_STATUS, status, 0);
            if (status[0] == 0) {
                FileLog.e("DisintegrationEffect, compile vertex shader error: " + GLES31.glGetShaderInfoLog(vertexShader));
                GLES31.glDeleteShader(vertexShader);
                running = false;
                return;
            }
            GLES31.glShaderSource(fragmentShader, RLottieDrawable.readRes(null, R.raw.disintegration_fragment) + "\n// " + Math.random());
            GLES31.glCompileShader(fragmentShader);
            GLES31.glGetShaderiv(fragmentShader, GLES31.GL_COMPILE_STATUS, status, 0);
            if (status[0] == 0) {
                FileLog.e("DisintegrationEffect, compile fragment shader error: " + GLES31.glGetShaderInfoLog(fragmentShader));
                GLES31.glDeleteShader(fragmentShader);
                running = false;
                return;
            }
            drawProgram = GLES31.glCreateProgram();
            if (drawProgram == 0) {
                running = false;
                return;
            }
            GLES31.glAttachShader(drawProgram, vertexShader);
            GLES31.glAttachShader(drawProgram, fragmentShader);
            String[] feedbackVaryings = {"outPosition", "outVertexColor", "outColumnIndex"};
            GLES31.glTransformFeedbackVaryings(drawProgram, feedbackVaryings, GLES31.GL_INTERLEAVED_ATTRIBS);

            GLES31.glLinkProgram(drawProgram);
            GLES31.glGetProgramiv(drawProgram, GLES31.GL_LINK_STATUS, status, 0);
            if (status[0] == 0) {
                FileLog.e("DisintegrationEffect, link draw program error: " + GLES31.glGetProgramInfoLog(drawProgram));
                running = false;
                return;
            }

            sizeHandle = GLES31.glGetUniformLocation(drawProgram, "size");
            diameterHandle = GLES31.glGetUniformLocation(drawProgram, "diameter");
            currentColumnIndexHandle = GLES31.glGetUniformLocation(drawProgram, "currentColumnIndex");
            maxColumnIndexHandle = GLES31.glGetUniformLocation(drawProgram, "maxColumnIndex");

            GLES31.glViewport(0, 0, width, height);
            GLES31.glEnable(GLES31.GL_BLEND);
            GLES31.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            GLES31.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

            GLES31.glUseProgram(drawProgram);
            GLES31.glUniform2f(sizeHandle, width, height);
            GLES31.glUniform1f(diameterHandle, diameter);
            GLES31.glUniform1f(currentColumnIndexHandle, 0f);
            GLES31.glUniform1f(maxColumnIndexHandle, particlesX - 1);
        }

        private float t;
        private float currentColumnIndex = 0f;

        private void drawFrame(float Δt) {
            if (!egl.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext) || t >= 3.5) {
                running = false;
                return;
            }

            t += Δt;

            GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT);
            GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, particlesData[currentBuffer]);

            GLES31.glVertexAttribPointer(0, 2, GLES31.GL_FLOAT, false, 28, 0);
            GLES31.glEnableVertexAttribArray(0);

            GLES31.glVertexAttribPointer(1, 4, GLES31.GL_FLOAT, false, 28, 8);
            GLES31.glEnableVertexAttribArray(1);

            GLES31.glVertexAttribPointer(2, 1, GLES31.GL_FLOAT, false, 28, 24);
            GLES31.glEnableVertexAttribArray(2);

            GLES31.glBindBufferBase(GLES31.GL_TRANSFORM_FEEDBACK_BUFFER, 0, particlesData[1 - currentBuffer]);

            GLES31.glVertexAttribPointer(0, 2, GLES31.GL_FLOAT, false, 28, 0);
            GLES31.glEnableVertexAttribArray(0);

            GLES31.glVertexAttribPointer(1, 4, GLES31.GL_FLOAT, false, 28, 8);
            GLES31.glEnableVertexAttribArray(1);

            GLES31.glVertexAttribPointer(2, 1, GLES31.GL_FLOAT, false, 28, 24);
            GLES31.glEnableVertexAttribArray(2);

            GLES31.glUniform1f(currentColumnIndexHandle, currentColumnIndex);
            GLES31.glBeginTransformFeedback(GLES31.GL_POINTS);
            GLES31.glDrawArrays(GLES31.GL_POINTS, 0, particlesCount);
            GLES31.glEndTransformFeedback();

            currentBuffer = 1 - currentBuffer;
            currentColumnIndex += 1f;

            egl.eglSwapBuffers(eglDisplay, eglSurface);

            checkGlErrors();
        }

        private void die() {
            if (particlesData != null) {
                try {
                    GLES31.glDeleteBuffers(2, particlesData, 0);
                } catch (Exception e) {
                    FileLog.e(e);
                }
                ;
                particlesData = null;
            }
            if (drawProgram != 0) {
                try {
                    GLES31.glDeleteProgram(drawProgram);
                } catch (Exception e) {
                    FileLog.e(e);
                }
                ;
                drawProgram = 0;
            }
            if (egl != null) {
                try {
                    egl.eglMakeCurrent(eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
                } catch (Exception e) {
                    FileLog.e(e);
                }
                ;
                try {
                    egl.eglDestroySurface(eglDisplay, eglSurface);
                } catch (Exception e) {
                    FileLog.e(e);
                }
                ;
                try {
                    egl.eglDestroyContext(eglDisplay, eglContext);
                } catch (Exception e) {
                    FileLog.e(e);
                }
                ;
            }
            try {
                surfaceTexture.release();
            } catch (Exception e) {
                FileLog.e(e);
            }
            ;

            checkGlErrors();
        }

        private void checkResize() {
            synchronized (resizeLock) {
                if (resize) {
                    GLES31.glUniform2f(sizeHandle, width, height);
                    GLES31.glViewport(0, 0, width, height);
                    int newParticlesCount = particlesCount();
                    if (newParticlesCount > this.particlesCount) {
                        genParticlesData();
                    }
                    this.particlesCount = newParticlesCount;
                    resize = false;
                }
            }
        }
        int vertexDataSize = 7;
        private void genParticlesData() {
            if (particlesData != null) {
                GLES31.glDeleteBuffers(2, particlesData, 0);
            }

            particlesData = new int[2];
            GLES31.glGenBuffers(2, particlesData, 0);

            float currentX = left;
            float currentY = 0;
            float currentPixelX = 0;
            float currentPixelY = bitmap.getHeight() - 1;

            float deltaX = (float) bitmap.getWidth() / particlesX;
            float deltaY = (float) bitmap.getHeight() / particlesY;

            float[] vertexData = new float[particlesCount * vertexDataSize];

            for (int i = 0; i < particlesX; i++) {
                for (int j = 0; j < particlesY * vertexDataSize; j += vertexDataSize) {
                    vertexData[(i * particlesY * vertexDataSize) + j] = currentX;
                    vertexData[(i * particlesY * vertexDataSize) + j + 1] = currentY;

                    int intColor = bitmap.getPixel((int) currentPixelX, (int) currentPixelY);
                    int red = Color.red(intColor);
                    int green = Color.green(intColor);
                    int blue = Color.blue(intColor);
                    int alpha = Color.alpha(intColor);
                    vertexData[(i * particlesY * vertexDataSize) + j + 2] = red / 255f;
                    vertexData[(i * particlesY * vertexDataSize) + j + 3] = green / 255f;
                    vertexData[(i * particlesY * vertexDataSize) + j + 4] = blue / 255f;
                    vertexData[(i * particlesY * vertexDataSize) + j + 5] = alpha / 255f;

                    vertexData[(i * particlesY * vertexDataSize) + j + 6] = (float) i;

                    currentY += deltaY;
                    currentPixelY -= deltaY;

                    if (currentPixelY < 0) {
                        currentPixelY = 0;
                    }
                }
                currentY = 0;
                currentPixelY = bitmap.getHeight() - 1;
                currentX += deltaX;
                currentPixelX += deltaX;

                if (currentPixelX > bitmap.getWidth() - 1) {
                    currentPixelX = bitmap.getWidth() - 1;
                }
            }

            FloatBuffer vertexBuffer = FloatBuffer.wrap(vertexData);
            for (int i = 0; i < 2; ++i) {
                GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, particlesData[i]);
                GLES31.glBufferData(GLES31.GL_ARRAY_BUFFER, vertexBuffer.capacity() * 4, vertexBuffer, GLES31.GL_DYNAMIC_DRAW);
            }

            checkGlErrors();
        }

        private void checkGlErrors() {
            int err;
            while ((err = GLES31.glGetError()) != GLES31.GL_NO_ERROR) {
                FileLog.e("disintegration gles error " + err);
            }
        }
    }
}
