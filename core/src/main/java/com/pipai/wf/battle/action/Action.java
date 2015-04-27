package com.pipai.wf.battle.action;

import com.pipai.wf.battle.agent.Agent;
import com.pipai.wf.exception.IllegalMoveException;

public abstract class Action {
	
	protected Agent performerAgent;
	
	public Action(Agent performerAgent) {
		this.performerAgent = performerAgent;
	}
	
	public void perform() throws IllegalMoveException {
	}
	
	/*
	 * Returns the minimum AP required to perform the action
	 */
	public abstract int getAPRequired();
	
}
