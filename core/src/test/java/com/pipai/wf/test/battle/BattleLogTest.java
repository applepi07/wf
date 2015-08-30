package com.pipai.wf.test.battle;

import static org.junit.Assert.*;

import java.util.LinkedList;

import org.junit.Test;

import com.pipai.wf.battle.BattleController;
import com.pipai.wf.battle.Team;
import com.pipai.wf.battle.action.MoveAction;
import com.pipai.wf.battle.action.OverwatchAction;
import com.pipai.wf.battle.action.RangedWeaponAttackAction;
import com.pipai.wf.battle.action.ReadySpellAction;
import com.pipai.wf.battle.action.ReloadAction;
import com.pipai.wf.battle.action.SwitchWeaponAction;
import com.pipai.wf.battle.action.TargetedSpellWeaponAction;
import com.pipai.wf.battle.action.TargetedWithAccuracyAction;
import com.pipai.wf.battle.action.WeaponActionFactory;
import com.pipai.wf.battle.agent.Agent;
import com.pipai.wf.battle.agent.AgentState;
import com.pipai.wf.battle.agent.AgentStateFactory;
import com.pipai.wf.battle.log.BattleEvent;
import com.pipai.wf.battle.map.BattleMap;
import com.pipai.wf.battle.map.MapString;
import com.pipai.wf.battle.map.GridPosition;
import com.pipai.wf.battle.spell.FireballSpell;
import com.pipai.wf.battle.weapon.Pistol;
import com.pipai.wf.battle.weapon.SpellWeapon;
import com.pipai.wf.exception.BadStateStringException;
import com.pipai.wf.exception.IllegalActionException;
import com.pipai.wf.test.MockGUIObserver;
import com.pipai.wf.test.WfConfiguredTest;
import com.pipai.wf.unit.ability.FireballAbility;
import com.pipai.wf.util.UtilFunctions;

public class BattleLogTest extends WfConfiguredTest {

	@Test
	public void testMoveLog() {
		BattleMap map = new BattleMap(3, 4);
		GridPosition playerPos = new GridPosition(1, 0);
		map.addAgent(AgentStateFactory.newBattleAgentState(Team.PLAYER, playerPos, 3, 5, 2, 5, 65, 0));
		BattleController battle = new BattleController(map);
		MockGUIObserver observer = new MockGUIObserver();
		battle.registerObserver(observer);
		Agent agent = map.getAgentAtPos(playerPos);
		assertFalse(agent == null);
		LinkedList<GridPosition> path = new LinkedList<GridPosition>();
		GridPosition dest = new GridPosition(2, 0);
		path.add(agent.getPosition());
		path.add(dest);
		MoveAction move = new MoveAction(agent, path, 1);
		try {
			battle.performAction(move);
		} catch (IllegalActionException e) {
			fail(e.getMessage());
		}
		BattleEvent ev = observer.ev;
		assertTrue(ev.getType() == BattleEvent.Type.MOVE);
		assertTrue(ev.getPerformer() == agent);
		assertTrue(ev.getPath().peekLast().equals(dest));
		assertTrue(ev.getChainEvents().size() == 0);
	}

	@Test
	public void testIllegalMoveLog() {
		String rawMapString = "3 4\n"
				+ "s 2 1";
		BattleMap map = null;
		try {
			map = new BattleMap(new MapString(rawMapString));
		} catch (BadStateStringException e) {
			fail(e.getMessage());
		}
		GridPosition playerPos = new GridPosition(1, 0);
		map.addAgent(AgentStateFactory.newBattleAgentState(Team.PLAYER, playerPos, 3, 5, 2, 5, 65, 0));
		BattleController battle = new BattleController(map);
		MockGUIObserver observer = new MockGUIObserver();
		battle.registerObserver(observer);
		Agent agent = map.getAgentAtPos(playerPos);
		assertFalse(agent == null);
		LinkedList<GridPosition> path = new LinkedList<GridPosition>();
		GridPosition dest = new GridPosition(1, 1);
		path.add(agent.getPosition());
		path.add(dest);
		MoveAction move = new MoveAction(agent, path, 1);
		try {
			battle.performAction(move);
		} catch (IllegalActionException e) {
			fail(e.getMessage());
		}
		BattleEvent ev = observer.ev;
		assertTrue(ev.getType() == BattleEvent.Type.MOVE);
		assertTrue(ev.getPerformer() == agent);
		assertTrue(ev.getPath().peekLast().equals(dest));
		assertTrue(ev.getChainEvents().size() == 0);
		LinkedList<GridPosition> illegalPath = new LinkedList<GridPosition>();
		illegalPath.add(agent.getPosition());
		illegalPath.add(new GridPosition(2, 1));
		MoveAction badmove = new MoveAction(agent, illegalPath, 1);
		try {
			battle.performAction(badmove);
			fail("Expected IllegalMoveException was not thrown");
		} catch (IllegalActionException e) {
			BattleEvent ev2 = observer.ev;
			assertTrue(ev2 == ev);	// Check that notifyBattleEvent was not called
		}
	}

	@Test
	public void testAttackLog() {
		BattleMap map = new BattleMap(5, 5);
		GridPosition playerPos = new GridPosition(1, 1);
		GridPosition enemyPos = new GridPosition(2, 1);
		AgentState playerState = AgentStateFactory.newBattleAgentState(Team.PLAYER, playerPos, 3, 5, 2, 5, 1000, 0);
		playerState.weapons.add(new Pistol());
		map.addAgent(playerState);
		map.addAgent(AgentStateFactory.newBattleAgentState(Team.ENEMY, enemyPos, 3, 5, 2, 5, 65, 0));
		BattleController battle = new BattleController(map);
		MockGUIObserver observer = new MockGUIObserver();
		battle.registerObserver(observer);
		Agent player = map.getAgentAtPos(playerPos);
		Agent enemy = map.getAgentAtPos(enemyPos);
		assertFalse(player == null || enemy == null);
		TargetedWithAccuracyAction atk = (TargetedWithAccuracyAction)((Pistol)player.getCurrentWeapon()).getAction(player, enemy);
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

	@Test
	public void testOverwatchLog() {
		BattleMap map = new BattleMap(5, 5);
		GridPosition playerPos = new GridPosition(1, 1);
		GridPosition enemyPos = new GridPosition(2, 2);
		AgentState playerState = AgentStateFactory.newBattleAgentState(Team.PLAYER, playerPos, 3, 5, 2, 5, 65, 0);
		playerState.weapons.add(new Pistol());
		map.addAgent(playerState);
		map.addAgent(AgentStateFactory.newBattleAgentState(Team.ENEMY, enemyPos, 3, 5, 2, 5, 65, 0));
		BattleController battle = new BattleController(map);
		MockGUIObserver observer = new MockGUIObserver();
		battle.registerObserver(observer);
		Agent player = map.getAgentAtPos(playerPos);
		Agent enemy = map.getAgentAtPos(enemyPos);
		assertFalse(player == null || enemy == null);
		OverwatchAction ow = new OverwatchAction(player);
		try {
			battle.performAction(ow);
		} catch (IllegalActionException e) {
			fail(e.getMessage());
		}
		BattleEvent ev = observer.ev;
		assertTrue(ev.getType() == BattleEvent.Type.OVERWATCH);
		assertTrue(ev.getPerformer() == player);
		assertTrue(ev.getPreparedOWName() == "Attack");
		assertTrue(ev.getChainEvents().size() == 0);
		//Test Overwatch Activation
		LinkedList<GridPosition> path = new LinkedList<GridPosition>();
		GridPosition dest = new GridPosition(2, 1);
		path.add(enemy.getPosition());
		path.add(dest);
		MoveAction move = new MoveAction(enemy, path, 1);
		try {
			battle.performAction(move);
		} catch (IllegalActionException e) {
			fail(e.getMessage());
		}
		BattleEvent moveEv = observer.ev;
		assertTrue(moveEv.getType() == BattleEvent.Type.MOVE);
		assertTrue(moveEv.getPerformer() == enemy);
		LinkedList<BattleEvent> chain = moveEv.getChainEvents();
		assertTrue(chain.size() == 1);
		BattleEvent owEv = chain.peekFirst();
		assertTrue(owEv.getType() == BattleEvent.Type.OVERWATCH_ACTIVATION);
		assertTrue(owEv.getPerformer() == player);
		assertTrue(owEv.getTarget() == enemy);
		assertTrue(owEv.getActivatedOWAction() instanceof RangedWeaponAttackAction);
		assertTrue(owEv.getChainEvents().size() == 0);
		// Overwatch will always have a chance to miss since it clamps before applying aim penalty
		int expectedHP = UtilFunctions.clamp(0, enemy.getMaxHP(), enemy.getMaxHP() - owEv.getDamage());
		assertTrue(enemy.getHP() == expectedHP);
		assertTrue(player.getHP() == player.getMaxHP());
	}

	@Test
	public void testReloadLog() {
		BattleMap map = new BattleMap(3, 4);
		GridPosition playerPos = new GridPosition(1, 0);
		AgentState playerState = AgentStateFactory.newBattleAgentState(Team.PLAYER, playerPos, 3, 5, 2, 5, 65, 0);
		playerState.weapons.add(new Pistol());
		map.addAgent(playerState);
		BattleController battle = new BattleController(map);
		MockGUIObserver observer = new MockGUIObserver();
		battle.registerObserver(observer);
		Agent agent = map.getAgentAtPos(playerPos);
		try {
			battle.performAction(new ReloadAction(agent));
		} catch (IllegalActionException e) {
			fail(e.getMessage());
		}
		BattleEvent ev = observer.ev;
		assertTrue(ev.getType() == BattleEvent.Type.RELOAD);
		assertTrue(ev.getPerformer() == agent);
		assertTrue(ev.getChainEvents().size() == 0);
	}

	@Test
	public void testSwitchWeaponLog() {
		BattleMap map = new BattleMap(3, 4);
		GridPosition playerPos = new GridPosition(1, 0);
		GridPosition enemyPos = new GridPosition(2, 2);
		AgentState playerState = AgentStateFactory.newBattleAgentState(Team.PLAYER, playerPos, 3, 5, 2, 5, 65, 0);
		playerState.weapons.add(new Pistol());
		playerState.weapons.add(new SpellWeapon());
		map.addAgent(playerState);
		map.addAgent(AgentStateFactory.newBattleAgentState(Team.ENEMY, enemyPos, 3, 5, 2, 5, 65, 0));
		BattleController battle = new BattleController(map);
		MockGUIObserver observer = new MockGUIObserver();
		battle.registerObserver(observer);
		Agent agent = map.getAgentAtPos(playerPos);
		assertTrue(agent.getCurrentWeapon() instanceof Pistol);
		try {
			battle.performAction(new SwitchWeaponAction(agent));
		} catch (IllegalActionException e) {
			fail(e.getMessage());
		}
		BattleEvent ev = observer.ev;
		assertTrue(ev.getType() == BattleEvent.Type.SWITCH_WEAPON);
		assertTrue(ev.getPerformer() == agent);
		assertTrue(ev.getChainEvents().size() == 0);
		assertTrue(agent.getCurrentWeapon() instanceof SpellWeapon);
	}

	@Test
	public void testReadyFireballLog() {
		BattleMap map = new BattleMap(3, 4);
		GridPosition playerPos = new GridPosition(1, 0);
		AgentState playerState = AgentStateFactory.newBattleAgentState(Team.PLAYER, playerPos, 3, 5, 2, 5, 65, 0);
		playerState.weapons.add(new SpellWeapon());
		playerState.abilities.add(new FireballAbility());
		map.addAgent(playerState);
		BattleController battle = new BattleController(map);
		MockGUIObserver observer = new MockGUIObserver();
		battle.registerObserver(observer);
		Agent agent = map.getAgentAtPos(playerPos);
		try {
			battle.performAction(new SwitchWeaponAction(agent));
			battle.performAction(new ReadySpellAction(agent, new FireballSpell()));
		} catch (IllegalActionException e) {
			fail(e.getMessage());
		}
		BattleEvent ev = observer.ev;
		assertTrue(ev.getType() == BattleEvent.Type.READY);
		assertTrue(ev.getPerformer() == agent);
		assertTrue(ev.getSpell() instanceof FireballSpell);
		assertTrue(ev.getChainEvents().size() == 0);
	}

	@Test
	public void testCastFireballLog() {
		BattleMap map = new BattleMap(3, 4);
		GridPosition playerPos = new GridPosition(1, 0);
		GridPosition enemyPos = new GridPosition(2, 2);
		AgentState playerState = AgentStateFactory.newBattleAgentState(Team.PLAYER, playerPos, 3, 5, 2, 5, 1000, 0);
		playerState.weapons.add(new SpellWeapon());
		playerState.abilities.add(new FireballAbility());
		map.addAgent(playerState);
		map.addAgent(AgentStateFactory.newBattleAgentState(Team.ENEMY, enemyPos, 3, 5, 2, 5, 65, 0));
		BattleController battle = new BattleController(map);
		MockGUIObserver observer = new MockGUIObserver();
		battle.registerObserver(observer);
		Agent agent = map.getAgentAtPos(playerPos);
		Agent target = map.getAgentAtPos(enemyPos);
		try {
			battle.performAction(new ReadySpellAction(agent, new FireballSpell()));
			battle.performAction(WeaponActionFactory.defaultWeaponAction(agent, target));
		} catch (IllegalActionException e) {
			fail(e.getMessage());
		}
		BattleEvent ev = observer.ev;
		assertTrue(ev.getType() == BattleEvent.Type.CAST_TARGET);
		assertTrue(ev.getPerformer() == agent);
		assertTrue(ev.getTarget() == target);
		assertTrue(ev.getSpell() instanceof FireballSpell);
		assertTrue(ev.getChainEvents().size() == 0);
		// Player has 1000 aim, cannot miss
		assertTrue(ev.getDamageResult().hit);
		int expectedHP = UtilFunctions.clamp(0, target.getMaxHP(), target.getMaxHP() - ev.getDamage());
		assertTrue(target.getHP() == expectedHP);
		assertTrue(agent.getHP() == agent.getMaxHP());
	}

	@Test
	public void testFireballOverwatchLog() {
		BattleMap map = new BattleMap(5, 5);
		GridPosition playerPos = new GridPosition(1, 1);
		GridPosition enemyPos = new GridPosition(2, 2);
		AgentState playerState = AgentStateFactory.newBattleAgentState(Team.PLAYER, playerPos, 3, 5, 2, 5, 65, 0);
		playerState.weapons.add(new SpellWeapon());
		playerState.abilities.add(new FireballAbility());
		map.addAgent(playerState);
		map.addAgent(AgentStateFactory.newBattleAgentState(Team.ENEMY, enemyPos, 3, 5, 2, 5, 65, 0));
		BattleController battle = new BattleController(map);
		MockGUIObserver observer = new MockGUIObserver();
		battle.registerObserver(observer);
		Agent player = map.getAgentAtPos(playerPos);
		Agent enemy = map.getAgentAtPos(enemyPos);
		assertFalse(player == null || enemy == null);
		try {
			battle.performAction(new ReadySpellAction(player, new FireballSpell()));
			battle.performAction(new OverwatchAction(player));
		} catch (IllegalActionException e) {
			fail(e.getMessage());
		}
		BattleEvent ev = observer.ev;
		assertTrue(ev.getType() == BattleEvent.Type.OVERWATCH);
		assertTrue(ev.getPerformer() == player);
		assertTrue(ev.getPreparedOWName() == "Fireball");
		assertTrue(ev.getChainEvents().size() == 0);
		//Test Overwatch Activation
		LinkedList<GridPosition> path = new LinkedList<GridPosition>();
		GridPosition dest = new GridPosition(2, 1);
		path.add(enemy.getPosition());
		path.add(dest);
		MoveAction move = new MoveAction(enemy, path, 1);
		try {
			battle.performAction(move);
		} catch (IllegalActionException e) {
			fail(e.getMessage());
		}
		BattleEvent moveEv = observer.ev;
		assertTrue(moveEv.getType() == BattleEvent.Type.MOVE);
		assertTrue(moveEv.getPerformer() == enemy);
		LinkedList<BattleEvent> chain = moveEv.getChainEvents();
		assertTrue(chain.size() == 1);
		BattleEvent owEv = chain.peekFirst();
		assertTrue(owEv.getType() == BattleEvent.Type.OVERWATCH_ACTIVATION);
		assertTrue(owEv.getPerformer() == player);
		assertTrue(owEv.getTarget() == enemy);
		assertTrue(owEv.getActivatedOWAction() instanceof TargetedSpellWeaponAction);
		assertTrue(owEv.getChainEvents().size() == 0);
		// Overwatch will always have a chance to miss since it clamps before applying aim penalty
		int expectedHP = UtilFunctions.clamp(0, enemy.getMaxHP(), enemy.getMaxHP() - owEv.getDamage());
		assertTrue(enemy.getHP() == expectedHP);
		assertTrue(player.getHP() == player.getMaxHP());
	}

}
