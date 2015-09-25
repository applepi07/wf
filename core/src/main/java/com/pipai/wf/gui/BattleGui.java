package com.pipai.wf.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.Ray;
import com.pipai.wf.WFGame;
import com.pipai.wf.battle.BattleController;
import com.pipai.wf.battle.BattleObserver;
import com.pipai.wf.battle.Team;
import com.pipai.wf.battle.action.Action;
import com.pipai.wf.battle.action.MoveAction;
import com.pipai.wf.battle.action.OverwatchAction;
import com.pipai.wf.battle.action.ReadySpellAction;
import com.pipai.wf.battle.action.ReloadAction;
import com.pipai.wf.battle.action.SwitchWeaponAction;
import com.pipai.wf.battle.action.TargetedAction;
import com.pipai.wf.battle.action.TargetedActionable;
import com.pipai.wf.battle.action.TargetedWithAccuracyAction;
import com.pipai.wf.battle.action.WeaponActionFactory;
import com.pipai.wf.battle.agent.Agent;
import com.pipai.wf.battle.ai.AI;
import com.pipai.wf.battle.ai.AIMoveRunnable;
import com.pipai.wf.battle.ai.TopModularAI;
import com.pipai.wf.battle.log.BattleEvent;
import com.pipai.wf.battle.map.BattleMap;
import com.pipai.wf.battle.map.GridPosition;
import com.pipai.wf.battle.map.MapGraph;
import com.pipai.wf.battle.spell.FireballSpell;
import com.pipai.wf.battle.vision.FogOfWar;
import com.pipai.wf.battle.weapon.Rifle;
import com.pipai.wf.battle.weapon.SpellWeapon;
import com.pipai.wf.battle.weapon.Weapon;
import com.pipai.wf.exception.IllegalActionException;
import com.pipai.wf.gui.animation.AnimationController;
import com.pipai.wf.gui.animation.AnimationControllerObserver;
import com.pipai.wf.gui.animation.BulletAttackAnimationHandler;
import com.pipai.wf.gui.animation.CastTargetAnimationHandler;
import com.pipai.wf.gui.animation.MoveAnimationHandler;
import com.pipai.wf.gui.animation.OverwatchAnimationHandler;
import com.pipai.wf.gui.animation.ReadySpellAnimationHandler;
import com.pipai.wf.gui.animation.ReloadAnimationHandler;
import com.pipai.wf.gui.animation.SuppressionAnimationHandler;
import com.pipai.wf.gui.camera.AnchoredCamera;
import com.pipai.wf.guiobject.GuiObject;
import com.pipai.wf.guiobject.GuiRenderable;
import com.pipai.wf.guiobject.LeftClickable;
import com.pipai.wf.guiobject.LeftClickable3D;
import com.pipai.wf.guiobject.RightClickable3D;
import com.pipai.wf.guiobject.battle.AgentGuiObject;
import com.pipai.wf.guiobject.battle.BattleTerrainRenderer;
import com.pipai.wf.guiobject.overlay.ActionToolTip;
import com.pipai.wf.guiobject.overlay.AgentStatusWindow;
import com.pipai.wf.guiobject.overlay.WeaponIndicator;
import com.pipai.wf.unit.ability.Ability;
import com.pipai.wf.unit.ability.ActiveSkillTargetedAbility;
import com.pipai.wf.unit.ability.PrecisionShotAbility;
import com.pipai.wf.unit.ability.SuppressionAbility;
import com.pipai.wf.util.RayMapper;

/*
 * Simple 2D GUI for rendering a BattleMap
 */

public class BattleGui extends Gui implements BattleObserver, AnimationControllerObserver {

	public static enum Mode {
		MOVE(true), TARGET_SELECT(true), PRE_ANIMATION(false), ANIMATION(false), AI(false);

		private boolean allowInput;

		private Mode(boolean allowInput) {
			this.allowInput = allowInput;
		}

		public boolean allowsInput() {
			return allowInput;
		}
	}

	private static final int AI_MOVE_WAIT_TIME = 60;

	private AnchoredCamera camera;
	private OrthographicCamera overlayCamera, orthoCamera;
	private RayMapper rayMapper;
	private BattleController battle;
	private HashMap<Agent, AgentGuiObject> agentMap;
	private ArrayList<AgentGuiObject> agentList;
	private LinkedList<AgentGuiObject> selectableAgentOrderedList, targetAgentList;
	private AgentGuiObject selectedAgent, targetAgent;
	private MapGraph selectedMapGraph;
	private ArrayList<GuiRenderable> renderables, foregroundRenderables, renderablesCreateBuffer, renderablesDelBuffer, overlayRenderables;
	private ArrayList<LeftClickable3D> leftClickables, leftClickablesCreateBuffer, leftClickablesDelBuffer;
	private ArrayList<LeftClickable> overlayLeftClickables;
	private ArrayList<RightClickable3D> rightClickables, rightClickablesCreateBuffer, rightClickablesDelBuffer;
	private Mode mode;
	private boolean aiTurn;
	private int aiMoveWait = 0;
	private AI ai;
	private BattleTerrainRenderer terrainRenderer;
	private ActionToolTip tooltip;
	private WeaponIndicator weaponIndicator;
	private AgentStatusWindow agentStatusWindow;
	private TargetedAction targetedAction;
	private AnimationController animationController;
	private FogOfWar fogOfWar;

	public BattleGui(WFGame game, BattleMap map) {
		super(game);
		camera = new AnchoredCamera(this.getScreenWidth(), this.getScreenHeight());
		overlayCamera = new OrthographicCamera();
		orthoCamera = new OrthographicCamera();
		overlayCamera.setToOrtho(false, this.getScreenWidth(), this.getScreenHeight());
		orthoCamera.setToOrtho(false, this.getScreenWidth(), this.getScreenHeight());
		rayMapper = new RayMapper(camera.getCamera());
		this.battle = new BattleController(map);
		this.battle.registerObserver(this);
		this.ai = new TopModularAI(battle);
		this.aiTurn = false;
		this.mode = Mode.MOVE;
		this.renderables = new ArrayList<GuiRenderable>();
		this.foregroundRenderables = new ArrayList<GuiRenderable>();
		this.leftClickables = new ArrayList<LeftClickable3D>();
		this.rightClickables = new ArrayList<RightClickable3D>();
		this.overlayRenderables = new ArrayList<GuiRenderable>();
		this.overlayLeftClickables = new ArrayList<LeftClickable>();
		this.renderablesCreateBuffer = new ArrayList<GuiRenderable>();
		this.leftClickablesCreateBuffer = new ArrayList<LeftClickable3D>();
		this.rightClickablesCreateBuffer = new ArrayList<RightClickable3D>();
		this.renderablesDelBuffer = new ArrayList<GuiRenderable>();
		this.leftClickablesDelBuffer = new ArrayList<LeftClickable3D>();
		this.rightClickablesDelBuffer = new ArrayList<RightClickable3D>();
		this.agentMap = new HashMap<Agent, AgentGuiObject>();
		this.agentList = new ArrayList<AgentGuiObject>();
		this.selectableAgentOrderedList = new LinkedList<AgentGuiObject>();
		for (Agent agent : this.battle.getBattleMap().getAgents()) {
			AgentGuiObject a = new AgentGuiObject(this, agent);
			this.agentMap.put(agent, a);
			this.agentList.add(a);
			if (agent.getTeam() == Team.PLAYER) {
				this.selectableAgentOrderedList.add(a);
			}
			this.createInstance(a);
		}
		this.batch.set3DCamera(this.camera.getCamera());
		this.fogOfWar = new FogOfWar(map, this.selectableAgentOrderedList);
		// this.fogOfWar.fullScan();
		this.terrainRenderer = new BattleTerrainRenderer(this, map, fogOfWar);
		this.createInstance(this.terrainRenderer);
		this.generateOverlays();
		this.animationController = new AnimationController(this);
		this.setSelected(this.selectableAgentOrderedList.getFirst());
	}

	private void generateOverlays() {
		this.tooltip = new ActionToolTip(this, 0, 120, 320, 120);
		this.weaponIndicator = new WeaponIndicator(this, this.getScreenWidth() - 120, 80, 120, 80);
		this.agentStatusWindow = new AgentStatusWindow(this);
		this.overlayRenderables.add(this.tooltip);
		this.overlayRenderables.add(this.weaponIndicator);
		this.overlayRenderables.add(this.agentStatusWindow);
	}

	private void beginAnimation() {
		this.mode = Mode.ANIMATION;
	}

	public void endAnimation() {
		performVictoryCheck();
		if (aiTurn) {
			this.mode = Mode.AI;
			this.aiMoveWait = 0;
		} else {
			this.fogOfWar.fullScan();
			this.populateSelectableAgentList();
			this.mode = Mode.MOVE;
			// Check if we need to change selected Agent
			if (this.selectedAgent.getAgent().getAP() == 0 || this.selectedAgent.getAgent().isKO()) {
				this.selectableAgentOrderedList.remove(this.selectedAgent);
				if (this.selectableAgentOrderedList.size() > 0) {
					this.setSelected(this.selectableAgentOrderedList.getFirst());
				}
			}
			for (AgentGuiObject a : this.agentList) {
				if (a.getAgent().getTeam() == Team.PLAYER && (a.getAgent().getAP() > 0 && !a.getAgent().isKO())) {
					// Found a movable Agent, so we return and do not start AI turn
					this.updatePaths();
					this.terrainRenderer.setMovingTiles(this.selectedMapGraph);
					return;
				}
			}
			// All moves finished - start the AI
			startAiTurn();
		}
	}

	public Mode getMode() {
		return this.mode;
	}

	public RayMapper getRayMapper() {
		return this.rayMapper;
	}

	public AnchoredCamera getCamera() {
		return this.camera;
	}

	public void setSelected(AgentGuiObject agent) {
		if (agent.getAgent().getAP() > 0) {
			this.selectedAgent = agent;
			this.weaponIndicator.updateToAgent(agent);
			this.moveCameraToPos(this.selectedAgent.x, this.selectedAgent.y);
			this.updatePaths();
			this.terrainRenderer.setMovingTiles(this.selectedMapGraph);
		}
	}

	@Override
	public void createInstance(GuiObject o) {
		super.createInstance(o);
		if (o instanceof GuiRenderable) {
			renderablesCreateBuffer.add((GuiRenderable) o);
		}
		if (o instanceof LeftClickable3D) {
			leftClickablesCreateBuffer.add((LeftClickable3D) o);
		}
		if (o instanceof RightClickable3D) {
			rightClickablesCreateBuffer.add((RightClickable3D) o);
		}
	}

	@Override
	public void deleteInstance(GuiObject o) {
		super.deleteInstance(o);
		if (o instanceof GuiRenderable) {
			renderablesDelBuffer.add((GuiRenderable) o);
		}
		if (o instanceof LeftClickable3D) {
			leftClickablesDelBuffer.add((LeftClickable3D) o);
		}
		if (o instanceof RightClickable3D) {
			rightClickablesDelBuffer.add((RightClickable3D) o);
		}
	}

	private void cleanCreateBuffers() {
		for (GuiRenderable o : renderablesCreateBuffer) {
			if (o.renderPriority() == -1) {
				foregroundRenderables.add(o);
			} else {
				renderables.add(o);
			}
		}
		for (LeftClickable3D o : leftClickablesCreateBuffer) {
			leftClickables.add(o);
		}
		for (RightClickable3D o : rightClickablesCreateBuffer) {
			rightClickables.add(o);
		}
		renderablesCreateBuffer.clear();
		leftClickablesCreateBuffer.clear();
		rightClickablesCreateBuffer.clear();
	}

	private void cleanDelBuffers() {
		for (GuiRenderable o : renderablesDelBuffer) {
			if (!renderables.remove(o)) {
				foregroundRenderables.remove(o);
			}
		}
		for (LeftClickable3D o : leftClickablesDelBuffer) {
			leftClickables.remove(o);
		}
		for (RightClickable3D o : rightClickablesDelBuffer) {
			rightClickables.remove(o);
		}
		renderablesDelBuffer.clear();
		leftClickablesDelBuffer.clear();
		rightClickablesDelBuffer.clear();
	}

	public void updatePaths() {
		if (selectedAgent != null) {
			Agent a = selectedAgent.getAgent();
			MapGraph graph = new MapGraph(this.battle.getBattleMap(), a.getPosition(), a.getEffectiveMobility(), a.getAP(), a.getMaxAP());
			this.selectedMapGraph = graph;
		}
	}

	private Vector2 screenPosToGraphicPos(int screenX, int screenY) {
		float x = camera.position().x - this.getScreenWidth() / 2 + screenX;
		float y = camera.position().y - this.getScreenHeight() / 2 + screenY;
		return new Vector2(x, y);
	}

	@Override
	public void notifyBattleEvent(BattleEvent ev) {
		this.animateEvent(ev);
	}

	private void performVictoryCheck() {
		if (this.battle.battleResult() != BattleController.Result.NONE) {
			this.game.setScreen(new BattleResultsGui(this.game));
			this.dispose();
			return;
		}
	}

	private void startAiTurn() {
		this.terrainRenderer.clearShadedTiles();
		this.mode = Mode.AI;
		this.aiTurn = true;
		this.aiMoveWait = 0;
		this.battle.endTurn();
		this.ai.startTurn();
	}

	private void runAiTurn() {
		this.aiMoveWait += 1;
		if (this.aiMoveWait == BattleGui.AI_MOVE_WAIT_TIME) {
			AIMoveRunnable t = new AIMoveRunnable(this.ai);
			t.run();
		}
	}

	private void populateSelectableAgentList() {
		this.selectableAgentOrderedList.clear();
		for (Agent agent : this.battle.getBattleMap().getAgents()) {
			if (agent.getTeam() == Team.PLAYER && agent.getAP() > 0 && !agent.isKO()) {
				this.selectableAgentOrderedList.add(this.agentMap.get(agent));
			}
		}
	}

	public void performAttackAction(AgentGuiObject target) {
		if (selectedAgent != null) {
			Action atk = null;
			Weapon weapon = selectedAgent.getAgent().getCurrentWeapon();
			if (weapon instanceof SpellWeapon) {
				SpellWeapon sw = (SpellWeapon) weapon;
				atk = sw.getSpell().getAction(selectedAgent.getAgent(), target.getAgent());
			} else {
				if (weapon.currentAmmo() > 0) {
					if (this.targetedAction == null) {
						atk = ((TargetedActionable) weapon).getAction(selectedAgent.getAgent(), target.getAgent());
					} else {
						atk = this.targetedAction;
					}
				} else {
					return;
				}
			}
			try {
				this.battle.performAction(atk);
			} catch (IllegalActionException e) {
				System.out.println("Illegal move: " + e.getMessage());
			}
		}
	}

	public void performMoveWithSelectedAgent(GridPosition destination) {
		if (this.selectedAgent != null) {
			if (this.selectedMapGraph.canMoveTo(destination)) {
				LinkedList<GridPosition> path = this.selectedMapGraph.getPath(destination);
				int useAP = this.selectedMapGraph.apRequiredToMoveTo(destination);
				MoveAction move = new MoveAction(selectedAgent.getAgent(), path, useAP);
				this.terrainRenderer.clearShadedTiles();
				try {
					this.battle.performAction(move);
				} catch (IllegalActionException e) {
					System.out.println("IllegalMoveException detected: " + e.getMessage());
				}
			}
		}
	}

	@Override
	public void onLeftClick(int screenX, int screenY) {
		if (!this.mode.allowsInput()) {
			return;
		}
		Ray ray = this.rayMapper.screenToRay(screenX, screenY);
		Vector2 gamePos = screenPosToGraphicPos(screenX, screenY);
		int gameX = (int) gamePos.x;
		int gameY = (int) gamePos.y;
		if (this.mode != Mode.ANIMATION) {
			for (LeftClickable o : overlayLeftClickables) {
				o.onLeftClick(screenX, screenY, gameX, gameY);
			}
			for (LeftClickable3D o : leftClickables) {
				o.onLeftClick(ray);
			}
		}
	}

	@Override
	public void onRightClick(int screenX, int screenY) {
		if (!this.mode.allowsInput()) {
			return;
		}
		Ray ray = this.rayMapper.screenToRay(screenX, screenY);
		if (this.mode != Mode.ANIMATION) {
			for (RightClickable3D o : rightClickables) {
				o.onRightClick(ray);
			}
		}
	}

	@Override
	public void mouseScrolled(int amount) {
		switch (amount) {
		case 1:
			this.camera.decreaseHeight();
			break;
		case -1:
			this.camera.increaseHeight();
			break;
		}
	}

	public void animateEvent(BattleEvent event) {
		if (event.getPerformer() != null && !agentMap.get(event.getPerformer()).visible()) {
			endAnimation();
			return;
		}
		boolean animating = true;
		switch (event.getType()) {
		case MOVE:
			animationController.startAnimation(new MoveAnimationHandler(this, animationController, event, event.getPerformer().getTeam() == Team.PLAYER));
			break;
		case ATTACK:
		case RANGED_WEAPON_ATTACK:
			animationController.startAnimation(new BulletAttackAnimationHandler(this, event));
			break;
		case CAST_TARGET:
			animationController.startAnimation(new CastTargetAnimationHandler(this, event));
			break;
		case OVERWATCH:
			animationController.startAnimation(new OverwatchAnimationHandler(this, event, event.getPerformer().getTeam() == Team.PLAYER));
			break;
		case RELOAD:
			animationController.startAnimation(new ReloadAnimationHandler(this, event, event.getPerformer().getTeam() == Team.PLAYER));
			break;
		case READY:
			animationController.startAnimation(new ReadySpellAnimationHandler(this, event, event.getPerformer().getTeam() == Team.PLAYER));
			break;
		case TARGETED_ACTION:
			animationController.startAnimation(new SuppressionAnimationHandler(this, event));
			break;
		case START_TURN:
			if (event.getTeam() == Team.PLAYER) {
				this.mode = Mode.MOVE;
				this.aiTurn = false;
				this.populateSelectableAgentList();
				this.setSelected(this.selectableAgentOrderedList.getFirst());
			}
			animating = false;
			break;
		default:
			animating = false;
			break;
		}
		if (animating) {
			beginAnimation();
		} else {
			// No animation yet, just end now
			endAnimation();
		}
	}

	@Override
	public void notifyControlReleased() {
		endAnimation();
	}

	public void switchToMoveMode() {
		this.mode = Mode.MOVE;
		this.terrainRenderer.setMovingTiles(this.selectedMapGraph);
		this.moveCameraToPos(this.selectedAgent.x, this.selectedAgent.y);
	}

	public void switchToTargetMode() {
		switchToTargetMode(null);
	}

	public void switchToTargetMode(ActiveSkillTargetedAbility ability) {
		this.terrainRenderer.clearShadedTiles();
		this.targetAgentList = new LinkedList<AgentGuiObject>();
		for (Agent a : this.selectedAgent.getAgent().enemiesInRange()) {
			this.targetAgentList.add(this.agentMap.get(a));
		}
		this.mode = Mode.TARGET_SELECT;
		if (this.targetAgentList.size() == 0) {
			this.targetedAction = null;
			if (ability == null) {
				this.tooltip.setToGeneralDescription(WeaponActionFactory.defaultWeaponActionName(this.selectedAgent.getAgent()), "No enemies in range");
			} else {
				this.tooltip.setToGeneralDescription(ability.name(), "No enemies in range");
			}
			return;
		}
		this.switchTarget(this.targetAgentList.getFirst(), ability);
	}

	public void switchTarget(AgentGuiObject target) {
		switchTarget(target, null);
	}

	public void switchTarget(AgentGuiObject target, ActiveSkillTargetedAbility ability) {
		if (this.mode == Mode.TARGET_SELECT) {
			if (this.targetAgentList.contains(target)) {
				ArrayList<GridPosition> targetTiles = new ArrayList<>();
				ArrayList<GridPosition> targetableTiles = new ArrayList<>();
				targetTiles.add(target.getAgent().getPosition());
				for (AgentGuiObject a : this.targetAgentList) {
					targetableTiles.add(a.getAgent().getPosition());
				}
				this.terrainRenderer.setTargetableTiles(targetableTiles);
				this.terrainRenderer.setTargetTiles(targetTiles);
				this.targetAgent = target;
				if (ability == null) {
					this.targetedAction = WeaponActionFactory.defaultWeaponAction(this.selectedAgent.getAgent(), target.getAgent());
				} else {
					this.targetedAction = ability.getAction(this.selectedAgent.getAgent(), target.getAgent());
				}
				this.tooltip.setToActionDescription(this.targetedAction);
				this.moveCameraToPos((this.selectedAgent.x + target.x) / 2, (this.selectedAgent.y + target.y) / 2);
			}
		}
	}

	public AgentGuiObject getTarget() {
		return this.targetAgent;
	}

	public AgentGuiObject getAgentGUIObject(Agent a) {
		return this.agentMap.get(a);
	}

	private void globalUpdate() {
		updateCamera();
	}

	private void moveCameraToPos(float x, float y) {
		this.camera.moveTo(x, y);
	}

	private void updateCamera() {
		if (this.mode.allowsInput()) {
			if (this.checkKey(Keys.A)) {
				this.camera.moveLeft();
			}
			if (this.checkKey(Keys.D)) {
				this.camera.moveRight();
			}
			if (this.checkKey(Keys.W)) {
				this.camera.moveUp();
			}
			if (this.checkKey(Keys.S)) {
				this.camera.moveDown();
			}
		}
		this.camera.update();
		this.orthoCamera.update();
	}

	@Override
	public void onKeyDown(int keycode) {
		if (keycode == Keys.ESCAPE) {
			if (this.agentStatusWindow.getVisible()) {
				this.agentStatusWindow.setVisible(false);
				return;
			}
			if (this.mode == Mode.TARGET_SELECT) {
				this.switchToMoveMode();
				return;
			} else {
				Gdx.app.exit();
			}
		}
		if (selectedAgent == null || !this.mode.allowsInput()) {
			return;
		}
		Action action = null;
		switch (keycode) {
		case Keys.NUM_1:
			if (this.mode == Mode.MOVE) {
				switchToTargetMode();
			}
			break;
		case Keys.ENTER:
			if (this.mode == Mode.TARGET_SELECT) {
				this.performAttackAction(this.targetAgent);
			}
			break;
		case Keys.X:
			if (this.mode == Mode.MOVE) {
				action = new SwitchWeaponAction(selectedAgent.getAgent());
			}
			break;
		case Keys.R:
			// Reload
			if (selectedAgent.getAgent().getCurrentWeapon() instanceof SpellWeapon) {
				action = new ReadySpellAction(selectedAgent.getAgent(), new FireballSpell());
			} else {
				action = new ReloadAction(selectedAgent.getAgent());
			}
			break;
		case Keys.Y:
			// Overwatch
			action = new OverwatchAction(selectedAgent.getAgent());
			break;
		case Keys.H:
			// Hunker
			break;
		case Keys.K:
			// Skill
			if (this.mode == Mode.MOVE) {
				for (Ability a : selectedAgent.getAgent().getAbilities()) {
					if (a instanceof PrecisionShotAbility && !a.isOnCooldown() && selectedAgent.getAgent().getCurrentWeapon() instanceof Rifle) {
						this.switchToTargetMode((PrecisionShotAbility) a);
						break;
					} else if (a instanceof SuppressionAbility) {
						this.switchToTargetMode((SuppressionAbility) a);
						break;
					}
				}
			}
			break;
		case Keys.M:
			// Recharge mana
			break;
		case Keys.SHIFT_LEFT:
			// Select next unit
			if (this.mode == Mode.MOVE) {
				this.selectableAgentOrderedList.remove(this.selectedAgent);
				this.selectableAgentOrderedList.addLast(this.selectedAgent);
				this.setSelected(this.selectableAgentOrderedList.peek());
			} else if (this.mode == Mode.TARGET_SELECT) {
				this.targetAgentList.remove(this.targetAgent);
				this.targetAgentList.addLast(this.targetAgent);
				this.switchTarget(this.targetAgentList.peek());
			}
			break;
		case Keys.Q:
			this.camera.arcballRotationCW();
			break;
		case Keys.E:
			this.camera.arcballRotationCCW();
			break;
		case Keys.F1:
			if (this.mode == Mode.MOVE || this.targetedAction == null) {
				this.agentStatusWindow.setAgentStatus(this.selectedAgent.getAgent());
			} else if (this.mode == Mode.TARGET_SELECT) {
				if (this.targetedAction instanceof TargetedWithAccuracyAction) {
					this.agentStatusWindow.setTargetedWithAccuracyAction((TargetedWithAccuracyAction) this.targetedAction);
				} else {
					this.agentStatusWindow.setAgentStatus(this.targetedAction.getTarget());
				}
			}
			this.agentStatusWindow.setVisible(true);
			break;
		case Keys.BACKSLASH:
			terrainRenderer.setIsRenderingTextures(!terrainRenderer.getIsRenderingTextures());
			break;
		default:
			break;
		}
		if (action != null) {
			try {
				this.mode = Mode.PRE_ANIMATION;
				this.battle.performAction(action);
			} catch (IllegalActionException e) {
				System.out.println("Illegal move: " + e.getMessage());
				this.mode = Mode.MOVE;
			}
		}
	}

	@Override
	public void onKeyUp(int keycode) {

	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		// this.camera.setToOrtho(false, width, height);
		this.camera.setViewport(width, height);
		this.orthoCamera.setToOrtho(false, width, height);
		this.overlayCamera.setToOrtho(false, width, height);
	}

	@Override
	public void render(float delta) {
		super.render(delta);
		animationController.update();
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		globalUpdate();
		batch.getSpriteBatch().setProjectionMatrix(camera.getProjectionMatrix());
		batch.getShapeRenderer().setProjectionMatrix(camera.getProjectionMatrix());
		this.terrainRenderer.render(batch);
		// renderShape(batch.getShapeRenderer());
		for (GuiRenderable r : this.renderables) {
			r.render(batch);
		}
		batch.getDecalBatch().flush();
		batch.getSpriteBatch().setProjectionMatrix(orthoCamera.combined);
		batch.getShapeRenderer().setProjectionMatrix(orthoCamera.combined);
		for (GuiRenderable r : this.foregroundRenderables) {
			r.render(batch);
		}
		batch.getSpriteBatch().setProjectionMatrix(overlayCamera.combined);
		batch.getShapeRenderer().setProjectionMatrix(overlayCamera.combined);
		for (GuiRenderable r : this.overlayRenderables) {
			r.render(batch);
		}
		batch.getSpriteBatch().begin();
		batch.getSpriteBatch().draw(fogOfWar.getFogOfWarTexture(), 0, 0);
		batch.getSpriteBatch().end();
		drawFPS();
		if (aiTurn) {
			runAiTurn();
		}
		cleanDelBuffers();
		cleanCreateBuffers();
	}

	private void drawFPS() {
		BitmapFont font = batch.getFont();
		batch.getSpriteBatch().begin();
		font.setColor(Color.WHITE);
		font.draw(batch.getSpriteBatch(), String.valueOf(Gdx.graphics.getFramesPerSecond()), this.getScreenWidth() - 24, this.getScreenHeight() - font.getLineHeight() / 2);
		batch.getSpriteBatch().end();
	}

}