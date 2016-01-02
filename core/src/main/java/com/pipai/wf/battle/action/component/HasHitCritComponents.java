package com.pipai.wf.battle.action.component;

import com.pipai.wf.battle.damage.AccuracyPercentages;
import com.pipai.wf.util.UtilFunctions;

public interface HasHitCritComponents extends HitAccuracyComponent, CritAccuracyComponent, AccuracyPercentages {

	@Override
	default int toHit() {
		int total_aim = getHitCalculation().total();
		return UtilFunctions.clamp(1, 100, total_aim);
	}

	@Override
	default int toCrit() {
		int crit_prob = getCritCalculation().total();
		return UtilFunctions.clamp(1, 100, crit_prob);
	}

}
