package com.pipai.wf.unit.ability;

public class SnapShotAbility extends PassiveAbility {

	public SnapShotAbility() {
		super(0);
	}

	@Override
	public String name() {
		return "Snap Shot";
	}

	@Override
	public String description() {
		return "Can fire rifle after moving with -10 to-hit penalty";
	}

	@Override
	public Ability clone() {
		return new SnapShotAbility();
	}

}
