package com.example.opengldemo;
   
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;

import javax.microedition.khronos.opengles.GL10;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.os.Environment;
import android.util.Log;
/*
 * A cube with texture. 
 * Define the vertices for only one representative face.
 * Render the cube by translating and rotating the face.
 */
@SuppressLint("NewApi")
public class CameraCube implements OnFrameAvailableListener {
   private FloatBuffer vertexBuffer; // Buffer for vertex-array
   private FloatBuffer texBuffer;    // Buffer for texture-coords-array (NEW)
   private Camera camera;
   private SurfaceTexture surfaceTexture;
   private GLSurfaceView view;
   private int previewWidth = 1280;
   private int previewHeight = 720;
  
   private int render_mode = 1;

   private float[] vertices = {  // Vertices of the 6 faces
      // FRONT
      -1.0f, -1.0f,  1.0f,  // 0. left-bottom-front
       1.0f, -1.0f,  1.0f,  // 1. right-bottom-front
      -1.0f,  1.0f,  1.0f,  // 2. left-top-front
       1.0f,  1.0f,  1.0f,  // 3. right-top-front
      // BACK
       1.0f, -1.0f, -1.0f,  // 6. right-bottom-back
      -1.0f, -1.0f, -1.0f,  // 4. left-bottom-back
       1.0f,  1.0f, -1.0f,  // 7. right-top-back
      -1.0f,  1.0f, -1.0f,  // 5. left-top-back
      // LEFT
      -1.0f, -1.0f, -1.0f,  // 4. left-bottom-back
      -1.0f, -1.0f,  1.0f,  // 0. left-bottom-front 
      -1.0f,  1.0f, -1.0f,  // 5. left-top-back
      -1.0f,  1.0f,  1.0f,  // 2. left-top-front
      // RIGHT
       1.0f, -1.0f,  1.0f,  // 1. right-bottom-front
       1.0f, -1.0f, -1.0f,  // 6. right-bottom-back
       1.0f,  1.0f,  1.0f,  // 3. right-top-front
       1.0f,  1.0f, -1.0f,  // 7. right-top-back
      // TOP
      -1.0f,  1.0f,  1.0f,  // 2. left-top-front
       1.0f,  1.0f,  1.0f,  // 3. right-top-front
      -1.0f,  1.0f, -1.0f,  // 5. left-top-back
       1.0f,  1.0f, -1.0f,  // 7. right-top-back
      // BOTTOM
      -1.0f, -1.0f, -1.0f,  // 4. left-bottom-back
       1.0f, -1.0f, -1.0f,  // 6. right-bottom-back
      -1.0f, -1.0f,  1.0f,  // 0. left-bottom-front
       1.0f, -1.0f,  1.0f   // 1. right-bottom-front
   };
   
   float[] texCoords = { // Texture coords for the above face (NEW)
      0.0f, 1.0f,  // A. left-bottom (NEW)
      1.0f, 1.0f,  // B. right-bottom (NEW)
      0.0f, 0.0f,  // C. left-top (NEW)
      1.0f, 0.0f,   // D. right-top (NEW)
      //the same
      0.0f, 1.0f,  // A. left-bottom (NEW)
      1.0f, 1.0f,  // B. right-bottom (NEW)
      0.0f, 0.0f,  // C. left-top (NEW)
      1.0f, 0.0f,   // D. right-top (NEW)
      //the same
      0.0f, 1.0f,  // A. left-bottom (NEW)
      1.0f, 1.0f,  // B. right-bottom (NEW)
      0.0f, 0.0f,  // C. left-top (NEW)
      1.0f, 0.0f,   // D. right-top (NEW)
      //the same
      0.0f, 1.0f,  // A. left-bottom (NEW)
      1.0f, 1.0f,  // B. right-bottom (NEW)
      0.0f, 0.0f,  // C. left-top (NEW)
      1.0f, 0.0f,   // D. right-top (NEW)
      //the same
      0.0f, 1.0f,  // A. left-bottom (NEW)
      1.0f, 1.0f,  // B. right-bottom (NEW)
      0.0f, 0.0f,  // C. left-top (NEW)
      1.0f, 0.0f,   // D. right-top (NEW)
      //the same
      0.0f, 1.0f,  // A. left-bottom (NEW)
      1.0f, 1.0f,  // B. right-bottom (NEW)
      0.0f, 0.0f,  // C. left-top (NEW)
      1.0f, 0.0f   // D. right-top (NEW)
   };
   int[] textureIDs = new int[1];   // Array for 1 texture-ID (NEW)
     
   // Constructor - Set up the buffers
   public CameraCube() {
      // Setup vertex-array buffer. Vertices in float. An float has 4 bytes
      ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
      vbb.order(ByteOrder.nativeOrder()); // Use native byte order
      vertexBuffer = vbb.asFloatBuffer(); // Convert from byte to float
      vertexBuffer.put(vertices);         // Copy data into buffer
      vertexBuffer.position(0);           // Rewind
  
      // Setup texture-coords-array buffer, in float. An float has 4 bytes (NEW)
      ByteBuffer tbb = ByteBuffer.allocateDirect(texCoords.length * 4);
      tbb.order(ByteOrder.nativeOrder());
      texBuffer = tbb.asFloatBuffer();
      texBuffer.put(texCoords);
      texBuffer.position(0);
   }
   
   // Draw the shape
   public void draw(GL10 gl) {
	   //Log.d("CameraCube", "draw called");
	   
	   surfaceTexture.updateTexImage();
	   
	   gl.glEnable(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);  // Enable texture (NEW), specific for OpenGL1.x
	   gl.glActiveTexture(gl.GL_TEXTURE0);
	   
	   gl.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureIDs[0]);   // Bind to texture ID
	   OpenGLRenderer.checkGLError(gl, "glBindTexture");
	   
      gl.glFrontFace(GL10.GL_CCW);    // Front face in counter-clockwise orientation
      gl.glEnable(GL10.GL_CULL_FACE); // Enable cull face
      gl.glCullFace(GL10.GL_BACK);    // Cull the back face (don't display) 
      OpenGLRenderer.checkGLError(gl, "glCullFace");
   
      gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
      gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
      gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);  // Enable texture-coords-array (NEW)
      gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texBuffer); // Define texture-coords buffer (NEW)
      OpenGLRenderer.checkGLError(gl, "glTexCoordPointer");
      
      if (render_mode == 0) {
	      // Front
	      gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
	      OpenGLRenderer.checkGLError(gl, "glDrawArrays");
	      
	      // Right - Rotate 90 degree about y-axis
	      gl.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);
	      gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
	      OpenGLRenderer.checkGLError(gl, "glDrawArrays");
	
	      // Back - Rotate another 90 degree about y-axis
	      gl.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);
	      gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
	      OpenGLRenderer.checkGLError(gl, "glDrawArrays");
	
	      // Left - Rotate another 90 degree about y-axis
	      gl.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);
	      gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
	      OpenGLRenderer.checkGLError(gl, "glDrawArrays");
	
	      // Bottom - Rotate 90 degree about x-axis
	      gl.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
	      gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
	      OpenGLRenderer.checkGLError(gl, "glDrawArrays");
	      
	      // Top - Rotate another 180 degree about x-axis
	      gl.glRotatef(180.0f, 1.0f, 0.0f, 0.0f);
	      gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
	      OpenGLRenderer.checkGLError(gl, "glDrawArrays");
      }
      else if (render_mode == 1) {
	      for(int i=0;i<6;i++ ){
	    	  gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, i*4, 4);
	      }
      }
  
      gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);  // Disable texture-coords-array (NEW)
      gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
      gl.glDisable(GL10.GL_CULL_FACE);
      OpenGLRenderer.checkGLError(gl, "glDisable");
      
      gl.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
      OpenGLRenderer.checkGLError(gl, "glBindTexture");
      gl.glDisable(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);  // Enable texture (NEW)
   }
  
   // Load an image into GL texture
   public void loadTexture(GL10 gl, GLSurfaceView view) {
      gl.glGenTextures(1, textureIDs, 0); // Generate texture-ID array
      OpenGLRenderer.checkGLError(gl, "glGenTextures");

      gl.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureIDs[0]);   // Bind to texture ID
      OpenGLRenderer.checkGLError(gl, "glBindTexture");
      // Set up texture filters
      gl.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
      gl.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
      //gl.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
      //gl.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
      OpenGLRenderer.checkGLError(gl, "glTexParameterf");
      
      //now, it is in thread of GLSurfaceView
      surfaceTexture = new SurfaceTexture(textureIDs[0]);
      surfaceTexture.setOnFrameAvailableListener(this);
      
      this.view = view;
      camera = Camera.open(1);
      Log.d("CameraCube", "open camera OK");
      Camera.Parameters param = camera.getParameters();
      param.setPreviewSize(previewWidth, previewHeight);
      camera.setParameters(param);
      
      try {
		camera.setPreviewTexture(surfaceTexture);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      camera.startPreview();
      Log.d("CameraCube", "start preview OK");
  
   }
   
   public void release()
   {
	   camera.stopPreview();
	   surfaceTexture.setOnFrameAvailableListener(null);
	   camera.release();
	   Log.d("CameraCube", "camera release");
   }

	@Override
	public void onFrameAvailable(SurfaceTexture surfaceTexture) {
		// TODO Auto-generated method stub
		//Log.d("CameraCube", "onFrameAvailable called");
		view.requestRender();
	}
}