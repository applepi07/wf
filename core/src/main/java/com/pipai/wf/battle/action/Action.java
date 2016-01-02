package com.pipai.wf.battle.action;

import com.pipai.wf.battle.BattleConfiguration;
import com.pipai.wf.battle.BattleController;
import com.pipai.wf.battle.action.component.ApRequiredComponent;
import com.pipai.wf.battle.action.component.PerformerComponent;
import com.pipai.wf.battle.damage.DamageCalculator;
import com.pipai.wf.battle.damage.DamageDealer;
import com.pipai.wf.battle.damage.TargetedActionCalculator;
import com.pipai.wf.battle.log.BattleEvent;
import com.pipai.wf.battle.log.BattleLog;
import com.pipai.wf.battle.map.BattleMap;
import com.pipai.wf.exception.IllegalActionException;
import com.pipai.wf.misc.HasDescription;
import com.pipai.wf.misc.HasName;

public abstract class Action implements HasName, HasDescription {

	private BattleController battleController;
	private BattleLog log;
	private BattleMap battleMap;
	private DamageDealer damageDealer;

	public Action(BattleController controller) {
		battleController = controller;
		if (controller != null) {
			// TODO: Fix this hack for NullPointerException calling this constructor during OverwatchHelper.getName()
			log = controller.getLog();
			battleMap = controller.getBattleMap();
			damageDealer = new DamageDealer(battleMap);
		}
	}

	public final BattleController getBattleController() {
		return battleController;
	}

	public final BattleMap getBattleMap() {
		return battleMap;
	}

	public final BattleConfiguration getBattleConfiguration() {
		return battleController.getBattleConfiguration();
	}

	public final TargetedActionCalculator getTargetedActionCalculator() {
		return getBattleConfiguration().getTargetedActionCalculator();
	}

	public final DamageCalculator getDamageCalculator() {
		return getBattleConfiguration().getDamageCalculator();
	}

	public final DamageDealer getDamageDealer() {
		return damageDealer;
	}

	public final void perform() throws IllegalActionException {
		if (this instanceof ApRequiredComponent && this instanceof PerformerComponent) {
			if (((PerformerComponent) this).getPerformer().getAP() < ((ApRequiredComponent) this).getAPRequired()) {
				throw new IllegalActionException("Not enough AP to perform this action");
			}
		}
		performImpl();
		battleController.performPostActionNotifications();
		if (this instanceof PerformerComponent) {
			((PerformerComponent) this).getPerformer().onAction(this);
		}
		postPerform();
	}

	protected abstract void performImpl() throws IllegalActionException;

	/**
	 * Override if anything needs to be done after the action is complete
	 */
	protected void postPerform() {
		// Nothing
	}

	protected void logBattleEvent(BattleEvent ev) {
		if (log != null) {
			log.logEvent(ev);
		}
	}

}
