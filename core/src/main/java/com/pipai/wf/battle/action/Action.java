package com.pipai.wf.battle.action;

import com.pipai.wf.battle.BattleConfiguration;
import com.pipai.wf.battle.agent.Agent;
import com.pipai.wf.battle.log.BattleEvent;
import com.pipai.wf.battle.log.BattleEventLoggable;
import com.pipai.wf.battle.log.BattleLog;
import com.pipai.wf.battle.map.BattleMap;
import com.pipai.wf.exception.IllegalActionException;

public abstract class Action implements BattleEventLoggable {

	private BattleLog log;
	private Agent performerAgent;
	private BattleMap map;

	public Action(Agent performerAgent) {
		this.performerAgent = performerAgent;
		this.map = performerAgent.getBattleMap();
	}

	public final Agent getPerformer() {
		return performerAgent;
	}

	public final BattleMap getBattleMap() {
		return map;
	}

	public final void perform(BattleConfiguration config) throws IllegalActionException {
		if (this.performerAgent.getAP() < this.getAPRequired()) {
			throw new IllegalActionException("Not enough AP for action");
		}
		if (this.performerAgent.isKO()) {
			throw new IllegalActionException("KOed unit cannot act");
		}
		performImpl(config);
		this.performerAgent.onAction(this);
	}

	protected abstract void performImpl(BattleConfiguration config) throws IllegalActionException;

	/*
	 * Returns the minimum AP required to perform the action
	 */
	public abstract int getAPRequired();

	public abstract String name();

	public abstract String description();

	@Override
	public final void register(BattleLog log) {
		this.log = log;
	}

	protected final void log(BattleEvent ev) {
		if (log != null) {
			log.logEvent(ev);
		}
	}

}
