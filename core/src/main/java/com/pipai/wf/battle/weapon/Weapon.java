package com.pipai.wf.battle.weapon;

import com.pipai.wf.util.UtilFunctions;

public abstract class Weapon {
	
	public static final int STANDARD_RANGE = 12;
	
	protected int currentAmmo;
	
	public Weapon() {
		currentAmmo = baseAmmoCapacity();
	}
	
	public abstract int flatAimModifier();
	
	public abstract int situationalAimModifier(float distance, boolean flanked);
	
	public abstract int flatCritProbabilityModifier();
	
	public abstract int situationalCritProbabilityModifier(float distance, boolean flanked);

	public abstract int minBaseDamage();
	
	public abstract int maxBaseDamage();
	
	public int rollForDamage() {
		return UtilFunctions.randInt(minBaseDamage(), maxBaseDamage());
	}
	
	public abstract boolean needsAmmunition();
	
	public abstract int baseAmmoCapacity();
	
	public void expendAmmo(int amount) {
		currentAmmo -= amount;
	}
	
	public int currentAmmo() {
		return currentAmmo;
	}
	
	public void reload() {
		currentAmmo = baseAmmoCapacity();
	}
	
	public abstract String name();
	
}
