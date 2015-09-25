package com.pipai.wf.battle.weapon;

import com.pipai.wf.battle.action.RifleAttackAction;
import com.pipai.wf.battle.action.TargetedAction;
import com.pipai.wf.battle.action.TargetedActionable;
import com.pipai.wf.battle.agent.Agent;
import com.pipai.wf.math.LinearFunction;

public class Rifle extends Weapon implements TargetedActionable {

	@Override
	public int flatAimModifier() {
		return 0;
	}

	@Override
	public int rangeAimModifier(float distance) {
		int minOptimalRange = 7;
		int maxRangePenalty = -20;

		if (distance <= minOptimalRange) {
			return (int) (new LinearFunction(minOptimalRange, 0, 0, maxRangePenalty).eval(distance));
		}

		return 0;
	}

	@Override
	public int flatCritProbabilityModifier() {
		return 20;
	}

	@Override
	public int rangeCritModifier(float distance) {
		return 0;
	}

	@Override
	public int minBaseDamage() {
		return 4;
	}

	@Override
	public int maxBaseDamage() {
		return 6;
	}

	@Override
	public boolean needsAmmunition() {
		return true;
	}

	@Override
	public int baseAmmoCapacity() {
		return 3;
	}

	@Override
	public String name() {
		return "Rifle";
	}

	@Override
	public TargetedAction getAction(Agent performer, Agent target) {
		return new RifleAttackAction(performer, target);
	}

}
