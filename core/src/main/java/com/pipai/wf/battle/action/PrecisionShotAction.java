package com.pipai.wf.battle.action;

import com.pipai.wf.battle.agent.Agent;
import com.pipai.wf.battle.damage.DamageCalculator;
import com.pipai.wf.battle.damage.DamageResult;
import com.pipai.wf.battle.damage.PercentageModifier;
import com.pipai.wf.battle.damage.PercentageModifierList;
import com.pipai.wf.battle.damage.TargetedActionCalculator;
import com.pipai.wf.battle.damage.WeaponDamageFunction;
import com.pipai.wf.battle.log.BattleEvent;
import com.pipai.wf.battle.weapon.Rifle;
import com.pipai.wf.battle.weapon.Weapon;
import com.pipai.wf.exception.IllegalActionException;
import com.pipai.wf.unit.ability.Ability;
import com.pipai.wf.unit.ability.PrecisionShotAbility;
import com.pipai.wf.util.UtilFunctions;

public class PrecisionShotAction extends TargetedWithAccuracyAction {

	public PrecisionShotAction(Agent performerAgent, Agent targetAgent) {
		super(performerAgent, targetAgent);
	}

	@Override
	public int toHit() {
		int total_aim = getHitCalculation().total();
		return UtilFunctions.clamp(1, 100, total_aim);
	}

	@Override
	public int toCrit() {
		int crit_prob = getCritCalculation().total();
		return UtilFunctions.clamp(1, 100, crit_prob);
	}

	@Override
	public PercentageModifierList getHitCalculation() {
		Agent a = getPerformer();
		Agent target = getTarget();
		return TargetedActionCalculator.baseHitCalculation(a, target);
	}

	@Override
	public PercentageModifierList getCritCalculation() {
		Agent a = getPerformer();
		Agent target = getTarget();
		PercentageModifierList calc = TargetedActionCalculator.baseCritCalculation(a, target);
		calc.add(new PercentageModifier("Precision Shot", 30));
		return calc;
	}

	@Override
	protected void performImpl() throws IllegalActionException {
		Agent a = getPerformer();
		Ability ability = a.getAbility(PrecisionShotAbility.class);
		if (ability == null) {
			throw new IllegalActionException(a.getName() + "does not have Precision Shot ability");
		}
		Agent target = getTarget();
		Weapon w = a.getCurrentWeapon();
		if (!(w instanceof Rifle)) {
			throw new IllegalActionException("Rifle needed to use Precision Shot");
		}
		if (w.needsAmmunition() && w.currentAmmo() == 0) {
			throw new IllegalActionException("Not enough ammo to fire " + w.name());
		}
		DamageResult result = DamageCalculator.rollDamageGeneral(this, new WeaponDamageFunction(w), 0);
		DamageResult adjustedResult = new DamageResult(result.hit, result.crit, result.damage + (result.hit ? 1 : 0), result.damageReduction);
		target.takeDamage(result.damage);
		a.setAP(0);
		ability.setCooldown(2);
		log(BattleEvent.rangedWeaponAttackEvent(a, target, w, adjustedResult));
	}

	@Override
	public int getAPRequired() {
		return 1;
	}

	@Override
	public String name() {
		return "Precision Shot";
	}

	@Override
	public String description() {
		return "Fires a shot that has +1 extra damage and +30% critical chance.";
	}

}
