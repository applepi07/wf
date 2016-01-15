package com.pipai.wf.artemis.system.battleanimation;

import com.artemis.ComponentMapper;
import com.pipai.wf.artemis.components.AnchoredPositionComponent;
import com.pipai.wf.artemis.components.TextComponent;
import com.pipai.wf.artemis.components.TimedDestroyComponent;
import com.pipai.wf.artemis.system.TileGridPositionUtils;
import com.pipai.wf.battle.event.ReloadEvent;

import net.mostlyoriginal.api.event.common.Subscribe;

public class ReloadEventAnimationHandler extends BattleEventAnimationHandler {

	private ComponentMapper<TextComponent> mText;
	private ComponentMapper<AnchoredPositionComponent> mAnchoredPosition;
	private ComponentMapper<TimedDestroyComponent> mTimedDestroy;

	@Subscribe
	public void handleReloadEvent(ReloadEvent event) {
		int id = world.create();
		mText.create(id).text = "Reload";
		mAnchoredPosition.create(id).anchor = TileGridPositionUtils.gridPositionToTileCenter(event.performer.getPosition());
		mTimedDestroy.create(id).time = 90;
	}

}
