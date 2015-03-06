package com.pipai.wf.screen;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.pipai.wf.WFGame;
import com.pipai.wf.battle.Agent;
import com.pipai.wf.battle.BattleController;
import com.pipai.wf.battle.map.BattleMap;
import com.pipai.wf.battle.map.GridPosition;
import com.pipai.wf.renderable.BatchHelper;
import com.pipai.wf.renderable.gui.BattleTestGUI;

public class BattleTestbedScreen implements Screen, InputProcessor {
	
	private WFGame game;
	private OrthographicCamera camera;
	private BattleTestGUI gui;
	private int width, height;
	private BatchHelper batch;
	
	private HashMap<Integer, Boolean> heldKeys;
	
	public BattleTestbedScreen(WFGame game) {
		this.game = game;
        this.camera = new OrthographicCamera();
        this.width = 800;
        this.height = 600;
        this.camera.setToOrtho(false, width, height);
        BattleMap map = new BattleMap(12, 10);
        map.addAgentAtPos(new GridPosition(1, 1), Agent.Team.PLAYER);
        map.addAgentAtPos(new GridPosition(4, 1), Agent.Team.PLAYER);
        map.addAgentAtPos(new GridPosition(5, 8), Agent.Team.ENEMY);
        map.addAgentAtPos(new GridPosition(9, 10), Agent.Team.ENEMY);
        map.getCell(new GridPosition(5, 5)).setSolid(true);
        this.gui = new BattleTestGUI(new BattleController(map));
        this.heldKeys = new HashMap<Integer, Boolean>();
        Gdx.input.setInputProcessor(this);
        this.batch = new BatchHelper(game.sprBatch, game.shapeBatch, game.font);
	}
	
	private boolean checkKey(int key) {
		if (!this.heldKeys.containsKey(key)) {
			return false;
		} else {
			return this.heldKeys.get(key);
		}
	}
	
	private void updateCamera() {
		if (this.checkKey(Keys.A)) {
			this.camera.translate(-3, 0);
		}
		if (this.checkKey(Keys.D)) {
			this.camera.translate(3, 0);
		}
		if (this.checkKey(Keys.W)) {
			this.camera.translate(0, 3);
		}
		if (this.checkKey(Keys.S)) {
			this.camera.translate(0, -3);
		}

        this.camera.update();
	}
	
	private GridPosition resolveScreenPosition(int screenX, int screenY) {
		int x = (int)this.camera.position.x - this.width/2 + screenX;
		int y = (int)this.camera.position.y + this.height/2 - screenY;
		return new GridPosition(x, y);
	}

	@Override
	public void show() {
		
	}

	@Override
	public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        this.updateCamera();
        this.game.shapeBatch.setProjectionMatrix(camera.combined);
        this.game.sprBatch.setProjectionMatrix(camera.combined);
        this.gui.render(this.batch, this.width, this.height);
	}

	@Override
	public void resize(int width, int height) {
		this.width = width;
		this.height = height;
		this.camera.setToOrtho(false, width, height);
	}

	@Override
	public void pause() {
		
	}

	@Override
	public void resume() {
		
	}

	@Override
	public void hide() {
		
	}

	@Override
	public void dispose() {
		
	}

	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Keys.ESCAPE) {
			Gdx.app.exit();
		}
		this.heldKeys.put(keycode, true);
        return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		this.heldKeys.put(keycode, false);
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		GridPosition graphicPos = this.resolveScreenPosition(screenX, screenY);
		if (button == Buttons.LEFT) {
			this.gui.onLeftClick(screenX, screenY, graphicPos.x, graphicPos.y);
		} else if (button == Buttons.RIGHT) {
			this.gui.onRightClick(screenX, screenY, graphicPos.x, graphicPos.y);
		}
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}
	
	
	
}