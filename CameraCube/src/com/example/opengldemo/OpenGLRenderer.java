package com.example.opengldemo;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;

public class OpenGLRenderer implements Renderer {

		private Context context = null;   // Application context needed to read image (NEW)
		private GLSurfaceView view  = null;
        private CameraCube mCube = null;
        private TextureWall mWall = null;
        private float mCubeRotation = 0;
        private float mWallScaleFactor = (float) 12.0;
        private int bounce = -1;

        // Constructor
        public OpenGLRenderer(Context context, GLSurfaceView view) {
           this.context = context;   // Get the application context (NEW)
           this.view = view;
        }
        
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); 
                
            gl.glClearDepthf(1.0f);
            gl.glEnable(GL10.GL_DEPTH_TEST);
            gl.glDepthFunc(GL10.GL_LEQUAL);

            gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
                      GL10.GL_NICEST);
         // Setup Texture, each time the surface is created (NEW)
            mCube = new CameraCube();
            mCube.loadTexture(gl, view);    // Load image into Texture (NEW)
            mWall = new TextureWall();
            mWall.loadTexture(gl, context);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT); 
            
            //draw camera cube
            gl.glLoadIdentity();
            gl.glTranslatef(0.0f, 0.0f, -10.0f);
            gl.glRotatef(mCubeRotation, 1.0f, 1.0f, 1.0f);
            checkGLError(gl,"glRotatef");
            mCube.draw(gl);
            
            //draw background
            gl.glLoadIdentity();         
            gl.glScalef(mWallScaleFactor, mWallScaleFactor, 1);
            mWall.draw(gl);
                
            mCubeRotation -= 0.4f; 
            
            mWallScaleFactor += bounce*(0.015f);
            if (mWallScaleFactor < 8 || mWallScaleFactor > 13)
            	bounce *= -1;
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            gl.glViewport(0, 0, width, height);
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();
            GLU.gluPerspective(gl, 45.0f, (float)width / (float)height, 0.1f, 100.0f);
            gl.glViewport(0, 0, width, height);

            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();
        }
        
        public void release()
        {
        	//release camera
        	mCube.release();
        }
        
        public static void checkGLError(GL10 gl, String msg) {
            int e = gl.glGetError();
            if( gl.GL_NO_ERROR != e ){
                msg += ": Error 0x" + Integer.toHexString(e);
                throw new RuntimeException(msg);
            }
        }
}
