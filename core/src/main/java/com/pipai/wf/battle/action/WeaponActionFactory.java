package com.pipai.wf.battle.action;

import com.pipai.wf.battle.agent.Agent;
import com.pipai.wf.battle.weapon.Rifle;
import com.pipai.wf.battle.weapon.SpellWeapon;
import com.pipai.wf.battle.weapon.Weapon;

public class WeaponActionFactory {

	public static TargetedWithAccuracyActionOWCapable defaultWeaponAction(Agent performer, Agent target) {
		Weapon weapon = performer.getCurrentWeapon();
		if (weapon instanceof SpellWeapon) {
			return new TargetedSpellWeaponAction(performer, target);
		} else if (weapon instanceof Rifle) {
			return new RifleAttackAction(performer, target);
		} else {
			return new RangedWeaponAttackAction(performer, target);
		}
	}

	public static Class<? extends TargetedWithAccuracyActionOWCapable> defaultWeaponActionClass(Agent performer) {
		Weapon weapon = performer.getCurrentWeapon();
		if (weapon instanceof SpellWeapon) {
			return TargetedSpellWeaponAction.class;
		} else if (weapon instanceof Rifle) {
			return RifleAttackAction.class;
		} else {
			return RangedWeaponAttackAction.class;
		}
	}

	public static String defaultWeaponActionName(Agent performer) {
		Weapon weapon = performer.getCurrentWeapon();
		if (weapon instanceof SpellWeapon) {
			return new TargetedSpellWeaponAction(performer, null).name();
		} else if (weapon instanceof Rifle) {
			return new RifleAttackAction(performer, null).name();
		} else {
			return new RangedWeaponAttackAction(performer, null).name();
		}
	}

}
