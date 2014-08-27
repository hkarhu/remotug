package fi.uef.remotug;

import java.nio.FloatBuffer;
import java.util.HashMap;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import fi.conf.ae.gl.GLGraphicRoutines;
import fi.conf.ae.gl.GLValues;
import fi.conf.ae.gl.core.DisplayModePack;
import fi.conf.ae.gl.core.GLCore;
import fi.conf.ae.gl.core.GLKeyboardListener;
import fi.conf.ae.gl.text.GLBitmapFontBlitter;
import fi.conf.ae.gl.text.GLBitmapFontBlitter.Alignment;
import fi.conf.ae.gl.texture.GLTextureManager;
import fi.uef.remotug.gl.ModelManager;
import fi.uef.remotug.net.client.ConnectionListener;
import fi.uef.remotug.sensor.SensorListener;

public class RopeGUI extends GLCore implements GLKeyboardListener, ConnectionListener, SensorListener {

	private static final int ROUND_TIME = 30;
	
	private long startTime = 0;
	private long resetTime = 0;
	private volatile float balance = 0;
	private volatile float sensor = 0;
	private float lt = 0;
	private int winner = -1;

	private LocalPowerMeter localPowerMeter = new LocalPowerMeter();
	private PowerMeter remotePowerMeter = new PowerMeter();
	
	@Override
	public boolean glInit() {

		GL11.glClearColor( 0.0f, 0.0f, 0.0f, 1.0f);

		//Some nice values
		GL11.glEnable( GL11.GL_ALPHA_TEST );
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
		
		//Load all graphics
		GLTextureManager.getInstance().blockingLoad(this.getClass().getResourceAsStream("/font_default.png"), "font");
		GLTextureManager.getInstance().blockingLoad(this.getClass().getResourceAsStream("/map.png"), "map");
		GLTextureManager.getInstance().blockingLoad(this.getClass().getResourceAsStream("/rope.png"), "rope");
		
		//Create new model manager instance
		new ModelManager().initialize();

		//Load all models
		ModelManager.getInstance().loadModel(this.getClass().getResourceAsStream("/map.rawgl"), "map");
		ModelManager.getInstance().loadModel(this.getClass().getResourceAsStream("/rope.rawgl"), "rope");
		
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
        
		GLValues.cameraPositionX = (float) (6*Math.sin(0.25f*balance));
		GLValues.cameraPositionY = (float) (6*Math.cos(0.25f*balance));
		GLValues.cameraPositionZ = -3;
		GLValues.cameraRotationX = 0;
		GLValues.cameraRotationY = 0;
		GLValues.cameraRotationZ = -1;
		
		GL11.glEnable( GL11.GL_BLEND );
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glPushMatrix();
//			GLTextureManager.getInstance().bindTexture("map");
//			ModelManager.getInstance().getModel("map").glDraw();
//			GLTextureManager.getInstance().bindTexture("rope");
//			ModelManager.getInstance().getModel("rope").glShiftDraw(balance*5);
		GL11.glPopMatrix();

		//Swap to orthogonal projection
		GL11.glLoadIdentity();
		GL11.glTranslatef(0, 0, 0);
		GLGraphicRoutines.initOrtho();
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glColor4f(1, 1, 1,1);
		
		GL11.glPushMatrix();
		
			localPowerMeter.glDraw();
			remotePowerMeter.glDraw();
		
			GL11.glTranslatef(GLValues.glWidth/2, 0.5f, 5);
			GLBitmapFontBlitter.drawString(String.format("%+.03f", balance), "font", 0.3f, 0.35f, Alignment.CENTERED);
			
			GL11.glTranslatef(0, GLValues.glHeight*0.8f, 0);
			
//			if(winner < 0){
//				GLBitmapFontBlitter.drawString(String.format("%.02f s", ROUND_TIME - (System.currentTimeMillis()-resetTime)*0.001f), "font", 0.3f, 0.35f, Alignment.CENTERED);
//			} else {
//				GLBitmapFontBlitter.drawString("LOL WIN", "font", 0.3f, 0.35f, Alignment.CENTERED);
//			}
		GL11.glPopMatrix();
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
	public void glKeyDown(int eventKey, char keyChar) {}

	@Override
	public void glKeyUp(int eventKey, char keyChar) {
		//Reset game
		if(eventKey == Keyboard.KEY_SPACE){
			resetTime = System.currentTimeMillis();
			winner = -1;
		}
	}
	
	@Override
	public void newSensorDataArrived(float kg) {
		
	}

	@Override
	public void gameValuesChanged(float balance, HashMap<Integer, Float> balances) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void readyAnnounced(int playerID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void winnerAnnounced(int winnerID) {
		// TODO Auto-generated method stub
		
	}
	
	public void startAnnounced(long serverTime) {
		// TODO: much code
		
	}
	
	

}
