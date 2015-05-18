package com.pipai.wf.gui;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.pipai.wf.WFGame;
import com.pipai.wf.guiobject.GUIObject;
import com.pipai.wf.guiobject.Renderable;

public class PartyInfoGUI extends GUI {

	private OrthographicCamera camera;
	private ArrayList<Renderable> renderables, renderablesCreateBuffer, renderablesDelBuffer; 

	public PartyInfoGUI(WFGame game) {
		super(game);
        camera = new OrthographicCamera();
        camera.setToOrtho(false, this.getScreenWidth(), this.getScreenHeight());
		this.renderables = new ArrayList<Renderable>();
		this.renderablesCreateBuffer = new ArrayList<Renderable>();
		this.renderablesDelBuffer = new ArrayList<Renderable>();
	}
	
	@Override
	public void createInstance(GUIObject o) {
		super.createInstance(o);
		if (o instanceof Renderable) {
			renderablesCreateBuffer.add((Renderable)o);
		}
	}
	
	@Override
	public void deleteInstance(GUIObject o) {
		super.deleteInstance(o);
		if (o instanceof Renderable) {
			renderablesDelBuffer.add((Renderable)o);
		}
	}
	
	@Override
	public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.getSpriteBatch().setProjectionMatrix(camera.combined);
        batch.getShapeRenderer().setProjectionMatrix(camera.combined);
    	for (Renderable r : this.renderables) {
    		r.render(batch);
    	}
		cleanDelBuffers();
		cleanCreateBuffers();
	}

	@Override
	public void onLeftClick(int screenX, int screenY) {
		this.game.setScreen(new BattleGUI(this.game));
		this.dispose();
	}

	@Override
	public void onRightClick(int screenX, int screenY) {
		
	}

	@Override
	public void onKeyDown(int keycode) {
		if (keycode == Keys.ESCAPE) { 
			Gdx.app.exit();
		}
	}

	@Override
	public void onKeyUp(int keycode) {
		
	}
	
	private void cleanCreateBuffers() {
		for (Renderable o : renderablesCreateBuffer) {
			renderables.add(o);
		}
		renderablesCreateBuffer.clear();
	}
	
	private void cleanDelBuffers() {
		for (Renderable o : renderablesDelBuffer) {
			renderables.remove(o);
		}
		renderablesDelBuffer.clear();
	}

}
