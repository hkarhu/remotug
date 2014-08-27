package fi.uef.remotug;

import org.lwjgl.opengl.GL11;

import fi.conf.ae.gl.GLGraphicRoutines;
import fi.conf.ae.gl.texture.GLTextureManager;

public class PowerMeter {

	protected final float METER_SIZE = 1f;
	
	protected volatile float force = 0;
	
	public void glDraw(){
		
		if(force >= 0.9f){
			float f = ((force-0.9f)/0.2f);
			GL11.glTranslatef((float)Math.random()*(0.2f*f), (float)Math.random()*(0.3f*f), 0);
			GL11.glColor4f(1, 1-f, 1-f, 1);
		} else {
			GL11.glColor4f(1, 1, 1, 1);
		}
		GL11.glPushMatrix();
			GLTextureManager.getInstance().bindTexture("meter_face");
			GLGraphicRoutines.draw2DRect(-METER_SIZE, -METER_SIZE, METER_SIZE, METER_SIZE, 0);
			GL11.glRotatef(105 + 330*force, 0, 0, 1);
			GLTextureManager.getInstance().bindTexture("meter_indicator");
			GLGraphicRoutines.draw2DRect(-0.12f*METER_SIZE, -0.12f*METER_SIZE, 0.8f*METER_SIZE, 0.12f*METER_SIZE, 0);
		GL11.glPopMatrix();
		
		//force = (float)(Math.sin((System.currentTimeMillis()-1409129465301l)*0.0002f)*0.5f+0.5f);
		
	}
	
	public void setForce(float force){
		this.force = force;
	}
	
}
