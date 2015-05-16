package com.pipai.wf.battle.ai;

import java.util.LinkedList;

import com.pipai.wf.battle.BattleController;
import com.pipai.wf.battle.BattleObserver;
import com.pipai.wf.battle.Team;
import com.pipai.wf.battle.agent.Agent;
import com.pipai.wf.battle.map.BattleMap;

public abstract class AI implements BattleObserver {
	
	protected BattleController battleController;
	protected BattleMap map;
	protected LinkedList<Agent> enemyAgents, toAct;
	
	public AI(BattleController battleController) {
		this.battleController = battleController;
		this.battleController.registerObserver(this);
		this.map = battleController.getBattleMap();
		this.enemyAgents = new LinkedList<Agent>();
		for (Agent a : this.map.getAgents()) {
			if (a.getTeam() == Team.ENEMY) {
				this.enemyAgents.add(a);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void startTurn() {
		this.toAct = (LinkedList<Agent>)this.enemyAgents.clone();
	}
	
	public boolean isDone() {
		return this.toAct.isEmpty();
	}
	
	public abstract void performMove();
	
}
