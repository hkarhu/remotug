package ropepull;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import ropepull.gl.ModelManager;
import ae.gl.GLGraphicRoutines;
import ae.gl.GLValues;
import ae.gl.core.DisplayModePack;
import ae.gl.core.GLCore;
import ae.gl.core.GLKeyboardListener;
import ae.gl.texture.GLTextureManager;
import ae.routines.S;

public class RopeGUI extends GLCore implements GLKeyboardListener, ServerConnectionListener {

	private long startTime = 0;
	private volatile float balance = 0;
	private float lt = 0;

	@Override
	public boolean glInit() {

		GL11.glClearColor( 0.0f, 0.0f, 0.0f, 1.0f);

		//Some nice values
		GL11.glEnable( GL11.GL_ALPHA_TEST );
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable( GL11.GL_DEPTH_TEST );
		GL11.glDepthFunc( GL11.GL_LEQUAL );
		GL11.glEnable( GL11.GL_BLEND );
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glEnable(GL11.GL_NORMALIZE);
		GL11.glEnable( GL11.GL_CULL_FACE );
		GL11.glCullFace( GL11.GL_BACK );
		GL11.glEnable( GL11.GL_LIGHTING );
		GL11.glEnable( GL11.GL_DITHER );
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);

		//Sharp bitmap scaling
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		
		//Clear buffers
		GL11.glClear(
				GL11.GL_COLOR_BUFFER_BIT |
				GL11.GL_DEPTH_BUFFER_BIT |
				GL11.GL_ACCUM_BUFFER_BIT |
				GL11.GL_STENCIL_BUFFER_BIT
				);
		
		//Add this as a listener to keyboard and mouse data providers
		keyboardListeners.add(this);
		
		//Create new instance for texture manager
		new GLTextureManager(getExecutorService()).initialize();
		
		//Load all graphics from ./gfx
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("gfx"), "*.{jpg,png}")) {
			for (Path path : stream) {
				String identifier = path.getFileName().toString();
				identifier = identifier.substring(0,identifier.length()-4);
				S.debug("Loading texture '%s' to identifier '%s'", path.toString(), identifier);
				GLTextureManager.getInstance().blockingLoad(path, identifier);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Create new model manager instance
		new ModelManager().initialize();

		//Load all models from ./gfx
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("gfx"), "*.{rawgl}")) {
			for (Path path : stream) {
				String identifier = path.getFileName().toString();
				identifier = identifier.substring(0,identifier.length()-6);
				S.debug("Loading model '%s' to identifier '%s'", path.toString(), identifier);
				ModelManager.getInstance().loadModel(path, identifier);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		startTime = System.currentTimeMillis();

		return true;

	}

	@Override
	public DisplayModePack glPickDisplayMode() throws Exception {return null;}

	@Override
	public void glLoop() {
		GL11.glClear(
				GL11.GL_COLOR_BUFFER_BIT |
				GL11.GL_DEPTH_BUFFER_BIT |
				GL11.GL_ACCUM_BUFFER_BIT |
				GL11.GL_STENCIL_BUFFER_BIT
				);

		lt = (System.currentTimeMillis() - startTime)%Float.MAX_VALUE;

		//Init perspective projection and draw the map
		GL11.glLoadIdentity();
		GL11.glTranslatef(0, 0, 0);
		GL11.glEnable(GL11.GL_LIGHTING);
		GLGraphicRoutines.initPerspective(60);
		GLGraphicRoutines.initCamera();

		//Light
        FloatBuffer fb = BufferUtils.createFloatBuffer(4);
        fb.put(new float[] {0.0f, 0.0f, 0.0f, 1.0f}); fb.rewind(); GL11.glLight(GL11.GL_LIGHT0, GL11.GL_AMBIENT, fb);
        fb.put(new float[] {1.0f, 1.0f, 1.0f, 0.5f}); fb.rewind(); GL11.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, fb);
        fb.put(new float[] {1.0f, 1.0f, 1.0f, 0.25f}); fb.rewind(); GL11.glLight(GL11.GL_LIGHT0, GL11.GL_SPECULAR, fb);
        fb.put(new float[] {0.0f, 0.0f, 10.0f}); fb.rewind(); GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, fb);
        fb.clear();
        
        GL11.glEnable(GL11.GL_LIGHT0);
		
		GLValues.cameraPositionX = (float) (5*Math.sin(0.25f*Math.sin(lt * 0.0001f)));
		GLValues.cameraPositionY = (float) (5*Math.cos(0.25f*Math.sin(lt * 0.0001f)));
		GLValues.cameraPositionZ = -10;
		GLValues.cameraRotationX = 0;
		GLValues.cameraRotationY = 0;
		GLValues.cameraRotationZ = -1;
		
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glPushMatrix();
			GLTextureManager.getInstance().bindTexture("interrope");
			ModelManager.getInstance().getModel("rope").glDraw();
			GLTextureManager.getInstance().bindTexture("map");
			ModelManager.getInstance().getModel("map").glDraw();
		GL11.glPopMatrix();

		//Swap to orthogonal projection
		GL11.glLoadIdentity();
		GL11.glTranslatef(0, 0, 0);
		GLGraphicRoutines.initOrtho();
		GL11.glDisable(GL11.GL_LIGHTING);


	}

	@Override
	public void glFocusChanged(boolean isFocused) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glTerminate() {
		//Shut down the texture processor treads
		GLTextureManager.getInstance().requestShutdown();
	}

	@Override
	public void glKeyDown(int eventKey) {
		// TODO Auto-generated method stub

	}

	@Override
	public void glKeyUp(int eventKey) {
		// TODO Auto-generated method stub

	}


	@Override //This will most likely to be called from another thread, so ensure thread safety
	public void gameBalanceChanged(float balance) {
		this.balance = balance;
	}

}
