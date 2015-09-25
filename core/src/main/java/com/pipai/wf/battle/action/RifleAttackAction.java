package com.pipai.wf.battle.action;

import com.pipai.wf.battle.agent.Agent;
import com.pipai.wf.battle.damage.DamageCalculator;
import com.pipai.wf.battle.damage.DamageResult;
import com.pipai.wf.battle.damage.PercentageModifier;
import com.pipai.wf.battle.damage.PercentageModifierList;
import com.pipai.wf.battle.damage.TargetedActionCalculator;
import com.pipai.wf.battle.damage.WeaponDamageFunction;
import com.pipai.wf.battle.log.BattleEvent;
import com.pipai.wf.battle.weapon.Weapon;
import com.pipai.wf.exception.IllegalActionException;
import com.pipai.wf.unit.ability.SnapShotAbility;

public class RifleAttackAction extends RangedWeaponAttackAction{

	public RifleAttackAction(Agent performerAgent, Agent targetAgent) {
		super(performerAgent, targetAgent);
	}

	@Override
	public PercentageModifierList getHitCalculation() {
		Agent a = getPerformer();
		Agent target = getTarget();
		PercentageModifierList p = TargetedActionCalculator.baseHitCalculation(a, target);
		if (a.hasUsedAP() && a.getAbilities().hasAbility(SnapShotAbility.class)) {
			p.add(new PercentageModifier("Snap Shot", -10));
		}
		return p;
	}

	@Override
	protected void performImpl() throws IllegalActionException {
		Agent a = getPerformer();
		Agent target = getTarget();
		Weapon w = a.getCurrentWeapon();
		if (!a.getAbilities().hasAbility(SnapShotAbility.class) && a.hasUsedAP()) {
			throw new IllegalActionException("Cannot fire rifle after moving");
		}
		if (w.needsAmmunition() && w.currentAmmo() == 0) {
			throw new IllegalActionException("Not enough ammo to fire " + w.name());
		}
		DamageResult result = DamageCalculator.rollDamageGeneral(this, new WeaponDamageFunction(w), 0);
		a.setAP(0);
		target.takeDamage(result.damage);
		log(BattleEvent.rangedWeaponAttackEvent(a, target, w, result));
	}
}
