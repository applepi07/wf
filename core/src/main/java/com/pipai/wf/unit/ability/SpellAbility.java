package com.pipai.wf.unit.ability;

import com.pipai.wf.battle.spell.Spell;

public abstract class SpellAbility extends Ability {

	public SpellAbility(AbilityType t, int level) {
		super(t, level);
	}
	
	@Override
	public final String name() {
		return grantSpell().name();
	}

	@Override
	public final String description() {
		return grantSpell().description();
	}
	
	@Override
	public abstract Spell grantSpell();
	
}
