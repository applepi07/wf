package com.pipai.wf.battle.weapon;

import com.pipai.wf.battle.spell.Spell;

public class Wand extends SpellWeapon {

	@Override
	public boolean needsAmmunition() {
		return true;
	}

	@Override
	public int baseAmmoCapacity() {
		return 1;
	}

	@Override
	public void ready(Spell spell) {
		super.ready(spell);
		reload();
	}

	@Override
	public void cast() {
		super.cast();
		expendAmmo(1);
	}

	@Override
	public String getName() {
		return "Wand";
	}

	@Override
	public String getDescription() {
		return "A typical wand";
	}

}
