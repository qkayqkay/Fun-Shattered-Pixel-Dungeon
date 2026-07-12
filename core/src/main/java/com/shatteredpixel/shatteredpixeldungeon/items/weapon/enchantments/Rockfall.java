/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DelayedRockFall;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.GameMath;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Rockfall extends Weapon.Enchantment {

	private static ItemSprite.Glowing BROWN = new ItemSprite.Glowing( 0xFF965221 );

	private static final int BASE_RADIUS = 1;
	private static final int MAX_RADIUS  = 3;

	@Override
	public int proc(Weapon weapon, Char attacker, Char defender, int damage) {
		int level = Math.max( 0, weapon.buffedLvl() );

		// lvl 0 - 33%
		// lvl 1 - 50%
		// lvl 2 - 60%
		float procChance = (level+1f)/(level+3f) * procChanceMultiplier(attacker);
		if (Random.Float() < procChance) {
			triggerRockfall( attacker, defender, level, procChance );
		}

		return damage;
	}

	private static void triggerRockfall( Char attacker, Char defender, int level, float procChance ) {
		float powerMulti = Math.max(1f, procChance);
		int radius = Math.min( MAX_RADIUS, (BASE_RADIUS + level/2)*(int) powerMulti );
		int numRocks = (3 + level)*(int) powerMulti;

		int width = Dungeon.level.width();
		ArrayList<Integer> candidates = new ArrayList<>();

		for (int y = -radius; y <= radius; y++) {
			for (int x = -radius; x <= radius; x++) {

				int cell = defender.pos + x + y*width;
				if(Dungeon.hero.pos == cell) continue;
				if(Dungeon.level.findMob(cell) != null && Random.Int(3) == 0) continue; // mobs aren't harmed as often

				if (!Dungeon.level.insideMap(cell)) continue;
				if (Dungeon.level.solid[cell]) continue;
				if (Dungeon.level.distance(defender.pos, cell) > radius) continue; //keep the spread roughly circular
				if (Dungeon.level.traps.containsKey(cell) && Random.Int(5) != 0) continue; //mostly leave traps alone

				candidates.add(cell);
			}
		}

		if (candidates.isEmpty()) return;

		Random.shuffle(candidates);
		ArrayList<Integer> rockCells = new ArrayList<>();
		for (int cell : candidates) {
			if (rockCells.size() >= numRocks) break;
			rockCells.add(cell);
		}
		for (int cell : rockCells) {
			attacker.sprite.parent.add(new TargetedCell(cell, 0xFF0000));
		}
		float warning = GameMath.gate( Actor.TICK, (int)Math.ceil(defender.cooldown()), 3*Actor.TICK );
		Buff.append( attacker, RockFallTrigger.class, warning ).setRockPositions( rockCells );
	}

	public static class RockFallTrigger extends DelayedRockFall {

		@Override
		public void affectChar(Char ch) {
			ch.damage(Random.NormalIntRange(6, 12), this);
			if (ch.isAlive()) {
				Buff.prolong(ch, Paralysis.class, 3);
			} else if (ch == Dungeon.hero){
				Dungeon.fail( target );
				GLog.n( Messages.get( Rockfall.class, "rockfall_kill") );
			}
		}
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return BROWN;
	}
}