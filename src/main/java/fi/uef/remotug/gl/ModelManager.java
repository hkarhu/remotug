package ropepull.gl;

import java.nio.file.Path;
import java.util.HashMap;

import ae.AE;
import ae.routines.S;

public class ModelManager {
	
	private static ModelManager INSTANCE;
	private final HashMap<String, Model> models;

	public ModelManager() {
		INSTANCE = this;
		models = new HashMap<>();
	}
	
	public static ModelManager getInstance() {
		return INSTANCE;
	}
	
	public void initialize() {
	}

	public void loadModel(Path path, String identifier) {
		if (models.containsKey(identifier)) return;
		models.put(identifier, ModelLoader.loadModel(path));
	}
	
	public Model getModel(String identifier) {
		Model model = models.get(identifier);
		if (AE.isDebug() && model == null) {
			S.debugFunc("Model for identifier '%s' not loaded", identifier);
		}
		return model; 
	}
}
