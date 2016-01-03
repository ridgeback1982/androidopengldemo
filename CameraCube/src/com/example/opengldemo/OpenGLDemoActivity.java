package com.example.opengldemo;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import com.example.opengldemo.R;

public class OpenGLDemoActivity extends Activity {
    
	private OpenGLRenderer glRender = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Go fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        GLSurfaceView view = new GLSurfaceView(this);
        //view.setEGLContextClientVersion(2);	//OpenGL ES 1.x is not compatible with OpenGL ES 2.0
        glRender = new OpenGLRenderer(this, view);
        view.setRenderer(glRender);
        view.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        setContentView(view);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.open_gldemo, menu);
		return true;
	}
	
	@Override
	public void onDestroy()
	{
		glRender.release();
		super.onDestroy();
	}

}
