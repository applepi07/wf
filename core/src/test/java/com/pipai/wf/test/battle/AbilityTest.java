package com.pipai.wf.test.battle;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.pipai.wf.battle.BattleController;
import com.pipai.wf.battle.Team;
import com.pipai.wf.battle.action.PrecisionShotAction;
import com.pipai.wf.battle.action.ReloadAction;
import com.pipai.wf.battle.action.TargetedWithAccuracyActionOWCapable;
import com.pipai.wf.battle.agent.Agent;
import com.pipai.wf.battle.agent.AgentState;
import com.pipai.wf.battle.agent.AgentStateFactory;
import com.pipai.wf.battle.log.BattleEvent;
import com.pipai.wf.battle.map.BattleMap;
import com.pipai.wf.battle.map.GridPosition;
import com.pipai.wf.battle.spell.FireballSpell;
import com.pipai.wf.battle.weapon.Pistol;
import com.pipai.wf.battle.weapon.Rifle;
import com.pipai.wf.exception.IllegalActionException;
import com.pipai.wf.test.MockGUIObserver;
import com.pipai.wf.unit.ability.FireballAbility;
import com.pipai.wf.unit.ability.PrecisionShotAbility;
import com.pipai.wf.unit.ability.QuickReloadAbility;
import com.pipai.wf.unit.ability.RegenerationAbility;
import com.pipai.wf.unit.ability.SnapShotAbility;
import com.pipai.wf.util.UtilFunctions;

public class AbilityTest {

	@Test
	public void testHealOnTurnEnd() {
		BattleMap map = new BattleMap(1, 1);
		GridPosition playerPos = new GridPosition(0, 0);
		AgentState as = AgentStateFactory.newBattleAgentState(Team.PLAYER, playerPos, 3, 5, 2, 5, 65, 0);
		as.abilities.add(new RegenerationAbility(1));
		map.addAgent(as);
		Agent agent = map.getAgentAtPos(playerPos);
		agent.setHP(agent.getMaxHP() - 1);
		assertFalse(agent == null);
		assertTrue(agent.getHP() == agent.getMaxHP() - 1);

		BattleController battle = new BattleController(map);
		battle.endTurn();	// Ends player turn
		battle.endTurn();	// Ends enemy turn
		// Regeneration should proc at end of enemy turn
		assertTrue(agent.getHP() == agent.getMaxHP());
	}

	@Test
	public void testSpellGranted() {
		BattleMap map = new BattleMap(1, 1);
		GridPosition playerPos = new GridPosition(0, 0);
		AgentState as = AgentStateFactory.newBattleAgentState(Team.PLAYER, playerPos, 3, 5, 2, 5, 65, 0);
		as.abilities.add(new FireballAbility());
		map.addAgent(as);
		Agent agent = map.getAgentAtPos(playerPos);
		assertTrue(agent.getAbilities().hasSpell(FireballSpell.class));
	}

	@Test
	public void testNoQuickReload() {
		BattleMap map = new BattleMap(1, 1);
		GridPosition playerPos = new GridPosition(0, 0);
		AgentState as = AgentStateFactory.newBattleAgentState(Team.PLAYER, playerPos, 3, 5, 2, 5, 65, 0);
		as.weapons.add(new Pistol());
		map.addAgent(as);
		Agent agent = map.getAgentAtPos(playerPos);
		BattleController battle = new BattleController(map);
		try {
			battle.performAction(new ReloadAction(agent));
		} catch (IllegalActionException e) {
			fail(e.getMessage());
		}
		assertTrue(agent.getAP() == 0);
	}

	@Test
	public void testQuickReload() {
		BattleMap map = new BattleMap(1, 1);
		GridPosition playerPos = new GridPosition(0, 0);
		AgentState as = AgentStateFactory.newBattleAgentState(Team.PLAYER, playerPos, 3, 5, 2, 5, 65, 0);
		as.abilities.add(new QuickReloadAbility());
		as.weapons.add(new Pistol());
		map.addAgent(as);
		Agent agent = map.getAgentAtPos(playerPos);
		BattleController battle = new BattleController(map);
		try {
			battle.performAction(new ReloadAction(agent));
		} catch (IllegalActionException e) {
			fail(e.getMessage());
		}
		assertTrue(agent.getAP() == 1);
	}

	@Test
	public void testPrecisionShotCooldown() {
		BattleMap map = new BattleMap(3, 4);
		GridPosition playerPos = new GridPosition(1, 0);
		GridPosition enemyPos = new GridPosition(2, 2);
		AgentState playerState = AgentStateFactory.newBattleAgentState(Team.PLAYER, playerPos, 3, 5, 2, 5, 65, 0);
		playerState.abilities.add(new PrecisionShotAbility());
		playerState.weapons.add(new Rifle());
		map.addAgent(playerState);
		map.addAgent(AgentStateFactory.newBattleAgentState(Team.ENEMY, enemyPos, 3, 5, 2, 5, 65, 0));
		BattleController battle = new BattleController(map);
		Agent agent = map.getAgentAtPos(playerPos);
		Agent target = map.getAgentAtPos(enemyPos);
		try {
			battle.performAction(new PrecisionShotAction(agent, target));
			assertTrue(agent.getInnateAbilities().getAbility(PrecisionShotAbility.class).isOnCooldown());
		} catch (IllegalActionException e) {
			fail(e.getMessage());
		}

		battle.endTurn();
		assertTrue(agent.getInnateAbilities().getAbility(PrecisionShotAbility.class).isOnCooldown());
		try {
			battle.performAction(new PrecisionShotAction(agent, target));
			fail("Expected IllegalActionException not thrown");
		} catch (IllegalActionException e) {}

		battle.endTurn();
		assertFalse(agent.getInnateAbilities().getAbility(PrecisionShotAbility.class).isOnCooldown());
		try {
			battle.performAction(new PrecisionShotAction(agent, target));
		} catch (IllegalActionException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testPrecisionShotWeaponReq() {
		BattleMap map = new BattleMap(3, 4);
		GridPosition playerPos = new GridPosition(1, 0);
		GridPosition enemyPos = new GridPosition(2, 2);
		AgentState playerState = AgentStateFactory.newBattleAgentState(Team.PLAYER, playerPos, 3, 5, 2, 5, 65, 0);
		playerState.abilities.add(new PrecisionShotAbility());
		playerState.weapons.add(new Pistol());
		map.addAgent(playerState);
		map.addAgent(AgentStateFactory.newBattleAgentState(Team.ENEMY, enemyPos, 3, 5, 2, 5, 65, 0));
		BattleController battle = new BattleController(map);
		Agent agent = map.getAgentAtPos(playerPos);
		Agent target = map.getAgentAtPos(enemyPos);
		try {
			battle.performAction(new PrecisionShotAction(agent, target));
			fail("Expected exception not thrown");
		} catch (IllegalActionException e) {
		}
	}


	@Test
	public void testNoSnapShot() {
		BattleMap map = new BattleMap(5, 5);
		GridPosition playerPos = new GridPosition(1, 1);
		GridPosition enemyPos = new GridPosition(2, 1);
		AgentState playerState = AgentStateFactory.newBattleAgentState(Team.PLAYER, playerPos, 3, 5, 2, 5, 1000, 0);
		playerState.weapons.add(new Rifle());
		map.addAgent(playerState);
		map.addAgent(AgentStateFactory.newBattleAgentState(Team.ENEMY, enemyPos, 3, 5, 2, 5, 65, 0));
		BattleController battle = new BattleController(map);
		MockGUIObserver observer = new MockGUIObserver();
		battle.registerObserver(observer);
		Agent player = map.getAgentAtPos(playerPos);
		Agent enemy = map.getAgentAtPos(enemyPos);
		assertFalse(player == null || enemy == null);
		assertFalse(player.hasUsedAP());
		player.useAP(1);
		assertTrue(player.hasUsedAP());
		TargetedWithAccuracyActionOWCapable atk = (TargetedWithAccuracyActionOWCapable) ((Rifle) player.getCurrentWeapon()).getAction(player, enemy);
		assertTrue(atk.toHit() == 100);
		try {
			battle.performAction(atk);
			fail("Expected exception not thrown");
		} catch (IllegalActionException e) {
		}
	}

	@Test
	public void testSnapShot() {
		BattleMap map = new BattleMap(5, 5);
		GridPosition playerPos = new GridPosition(1, 1);
		GridPosition enemyPos = new GridPosition(2, 1);
		AgentState playerState = AgentStateFactory.newBattleAgentState(Team.PLAYER, playerPos, 3, 5, 2, 5, 1000, 0);
		playerState.abilities.add(new SnapShotAbility());
		playerState.weapons.add(new Rifle());
		map.addAgent(playerState);
		map.addAgent(AgentStateFactory.newBattleAgentState(Team.ENEMY, enemyPos, 3, 5, 2, 5, 65, 0));
		BattleController battle = new BattleController(map);
		MockGUIObserver observer = new MockGUIObserver();
		battle.registerObserver(observer);
		Agent player = map.getAgentAtPos(playerPos);
		Agent enemy = map.getAgentAtPos(enemyPos);
		assertFalse(player == null || enemy == null);
		assertFalse(player.hasUsedAP());
		player.useAP(1);
		assertTrue(player.hasUsedAP());
		TargetedWithAccuracyActionOWCapable atk = (TargetedWithAccuracyActionOWCapable) ((Rifle) player.getCurrentWeapon()).getAction(player, enemy);
		assertTrue(atk.toHit() == 100);
		try {
			battle.performAction(atk);
		} catch (IllegalActionException e) {
			fail(e.getMessage());
		}
		BattleEvent ev = observer.ev;
		assertTrue(ev.getType() == BattleEvent.Type.RANGED_WEAPON_ATTACK);
		assertTrue(ev.getPerformer() == player);
		assertTrue(ev.getTarget() == enemy);
		assertTrue(ev.getChainEvents().size() == 0);
		// Player has 1000 aim, cannot miss
		assertTrue(ev.getDamageResult().hit);
		int expectedHP = UtilFunctions.clamp(0, enemy.getMaxHP(), enemy.getMaxHP() - ev.getDamage());
		assertTrue(enemy.getHP() == expectedHP);
		assertTrue(player.getHP() == player.getMaxHP());
	}
}
