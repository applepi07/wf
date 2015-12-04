package com.pipai.wf.battle.ai;

import java.util.ArrayList;

import com.pipai.wf.battle.BattleController;
import com.pipai.wf.battle.Team;
import com.pipai.wf.battle.action.Action;
import com.pipai.wf.battle.action.MoveAction;
import com.pipai.wf.battle.action.OverwatchAction;
import com.pipai.wf.battle.action.ReadySpellAction;
import com.pipai.wf.battle.action.ReloadAction;
import com.pipai.wf.battle.action.TargetedWithAccuracyAction;
import com.pipai.wf.battle.action.WeaponActionFactory;
import com.pipai.wf.battle.agent.Agent;
import com.pipai.wf.battle.map.BattleMap;
import com.pipai.wf.battle.map.AgentCoverCalculator;
import com.pipai.wf.battle.map.DirectionalCoverSystem;
import com.pipai.wf.battle.map.GridPosition;
import com.pipai.wf.battle.map.MapGraph;
import com.pipai.wf.battle.spell.FireballSpell;
import com.pipai.wf.battle.vision.AgentVisionCalculator;
import com.pipai.wf.battle.weapon.SpellWeapon;
import com.pipai.wf.battle.weapon.Weapon;

public class GeneralModularAI extends ModularAI {

	private AgentCoverCalculator coverCalc;
	private AgentVisionCalculator agentVisionCalc;
	private ArrayList<Agent> playerAgents;
	private MapGraph mapgraph;

	public GeneralModularAI(BattleController controller, Agent a) {
		super(controller, a);
		coverCalc = new AgentCoverCalculator(getBattleMap(), getBattleConfiguration());
		agentVisionCalc = new AgentVisionCalculator(getBattleMap(), getBattleConfiguration());
		mapgraph = new MapGraph(getBattleMap(), getAgent().getPosition(), getAgent().getEffectiveMobility(), 1, 2);
		playerAgents = getAgentsInTeam(Team.PLAYER);
	}

	@Override
	public ActionScore getBestMove() {
		Agent a = getAgent();
		ActionScore best;
		if (coverCalc.isFlanked(a)) {
			best = getBestMoveAction();
		} else {
			best = getBestAttackAction();
		}
		if (best.action == null) {
			return new ActionScore(reloadWeaponAction(), 20);
		}
		return best;
	}

	private Action reloadWeaponAction() {
		if (getAgent().getCurrentWeapon() instanceof SpellWeapon) {
			return new ReadySpellAction(getBattleController(), getAgent(), new FireballSpell());
		} else {
			return new ReloadAction(getBattleController(), getAgent());
		}
	}

	private ActionScore getBestAttackAction() {
		Agent a = getAgent();
		Weapon w = a.getCurrentWeapon();
		if (agentVisionCalc.enemiesInRangeOf(a).size() == 0) {
			if (w instanceof SpellWeapon) {
				SpellWeapon sw = (SpellWeapon) w;
				if (sw.getSpell() == null) {
					return new ActionScore(new ReadySpellAction(getBattleController(), a, new FireballSpell()), 20);
				} else {
					return new ActionScore(new OverwatchAction(getBattleController(), a), 30);
				}
			} else {
				if (!w.needsAmmunition() || (w.needsAmmunition() && w.currentAmmo() > 0)) {
					return new ActionScore(new OverwatchAction(getBattleController(), a), 30);
				} else {
					return new ActionScore(new ReloadAction(getBattleController(), a), 20);
				}
			}
		} else {
			if (w.needsAmmunition() && w.currentAmmo() == 0) {
				return new ActionScore(new ReloadAction(getBattleController(), a), 20);
			} else {
				if (w instanceof SpellWeapon) {
					SpellWeapon sw = (SpellWeapon) w;
					if (sw.getSpell() == null) {
						return new ActionScore(new ReadySpellAction(getBattleController(), a, new FireballSpell()), 20);
					}
				}
				ActionScore best = new ActionScore(null, Float.MIN_NORMAL);
				for (Agent player : playerAgents) {
					if (player.isKO()) {
						continue;
					}
					WeaponActionFactory wFactory = new WeaponActionFactory(getBattleController());
					TargetedWithAccuracyAction action = wFactory.defaultWeaponAction(a, player);
					best = best.compareAndReturnBetter(new ActionScore(action, action.toHit()));
				}
				return best;
			}
		}
	}

	private ActionScore getBestMoveAction() {
		ArrayList<GridPosition> potentialTiles = mapgraph.getMovableCellPositions(1);
		ActionScore best = new ActionScore(null, Float.MIN_NORMAL);
		for (GridPosition pos : potentialTiles) {
			float score = scorePosition(pos);
			best = best.compareAndReturnBetter(new ActionScore(new MoveAction(getBattleController(), getAgent(), mapgraph.getPath(pos), 1), score));
		}
		return best;
	}

	private float scorePosition(GridPosition pos) {
		BattleMap map = getBattleMap();
		float min = Float.MAX_VALUE;
		for (Agent a : playerAgents) {
			if (a.isKO()) {
				continue;
			}
			DirectionalCoverSystem coverSystem = new DirectionalCoverSystem(map);
			float current = coverSystem.getBestCoverAgainstAttack(pos, a.getPosition()).getDefense();
			min = (current < min) ? current : min;
		}
		return min;
	}

}
