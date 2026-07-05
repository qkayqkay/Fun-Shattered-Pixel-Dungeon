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

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM100;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ImpBoss;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Lightning;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.CorrosionParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCorrosion;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class ImpSprite extends MobSprite {

	public ImpSprite() {
		super();
		texture( Assets.Sprites.IMP );
		TextureFilm frames = new TextureFilm( texture, 12, 14 );

		idle = new Animation( 10, true );
		idle.frames( frames,
				0, 1, 2, 3, 0, 1, 2, 3, 0, 0, 0, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
				0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 3, 0, 0, 0, 4, 4, 4, 4, 4, 4, 4, 4, 0, 0, 0, 4, 4, 4, 4, 4, 4, 4, 4 );

		run = new Animation( 20, true );
		run.frames( frames, 0 );

		attack = new Animation( 12, false );
		attack.frames( frames, 1, 2, 3 );

		zap = new Animation( 12, false );
		zap.frames( frames, 1, 2, 3 );

		die = new Animation( 10, false );
		die.frames( frames, 0, 3, 2, 1, 0, 3, 2, 1, 0 );

		play( idle );
	}

	@Override
	public void zap( int cell ) {
		zap( cell, null );
	}

	@Override
	public synchronized void zap( int cell, Callback callback ) {
		turnTo( ch.pos, cell );
		play( zap ); // purely cosmetic cast animation, doesn't drive turn release

		ImpBoss boss = (ImpBoss) ch;
		switch (boss.currentWand) {
			case ImpBoss.CORROSION_WAND:      zapCorrosion(cell, callback);     break;
			case ImpBoss.MAGIC_MISSILE_WAND:  zapMagicMissile(cell, callback);  break;
			case ImpBoss.LIGHTNING_WAND:
			default:                          zapLightning(cell, callback);     break;
		}
	}

	private void zapLightning( int cell, Callback callback ) {
		applyLightningDamage(cell);
		if (callback != null) callback.call(); // this is an instant effect, so release turn now
	}

	private void zapCorrosion( final int cell, final Callback callback ) {
		MagicMissile.boltFromChar(parent, MagicMissile.CORROSION, this, cell, new Callback() {
			@Override
			public void call() {
				applyCorrosionEffect(cell);
				if (callback != null) callback.call(); // compared to lightning, coro is bolt, so apply effect and then release turn
			}
		});
		Sample.INSTANCE.play(Assets.Sounds.GAS);
	}

	private void zapMagicMissile( final int cell, final Callback callback ) {
		MagicMissile.boltFromChar(parent, MagicMissile.MAGIC_MISSILE, this, cell, new Callback() {
			@Override
			public void call() {
				applyMagicMissileDamage(cell);
				if (callback != null) callback.call();
			}
		});
		Sample.INSTANCE.play(Assets.Sounds.ZAP);
	}


	private void applyLightningDamage( int cell ) {
		ImpBoss boss = (ImpBoss) ch;
		Char enemy = Actor.findChar(cell);
		if (enemy == null) return;

		if (boss.hit(boss, enemy, true)) {
			int dmg = Random.NormalIntRange(3, 10);
			dmg = Math.round(dmg * AscensionChallenge.statModifier(boss));
			enemy.damage(dmg, new ImpBoss.LightningBolt());

			if (enemy.sprite.visible) {
				enemy.sprite.centerEmitter().burst(SparkParticle.FACTORY, 3);
				enemy.sprite.flash();
			}
			if (enemy == Dungeon.hero) {
				PixelScene.shake(2, 0.3f);
				if (!enemy.isAlive()) {
					Badges.validateDeathFromEnemyMagic();
					Dungeon.fail(boss);
					GLog.n(Messages.get(boss, "zap_kill"));
				}
			}
		} else {
			enemy.sprite.showStatus(CharSprite.NEUTRAL, enemy.defenseVerb());
		}
	}

	private void applyCorrosionEffect( int cell ) {
		CorrosiveGas gas = Blob.seed(cell, 50, CorrosiveGas.class);
		CellEmitter.get(cell).burst(Speck.factory(Speck.CORROSION), 10);
		gas.setStrength(2, WandOfCorrosion.class);
		GameScene.add(gas);
		Sample.INSTANCE.play(Assets.Sounds.GAS);

		for (int i : PathFinder.NEIGHBOURS9) {
			Char c = Actor.findChar(cell + i);
			if (c != null) {
				//Buff.affect(c, Ooze.class).set(Ooze.DURATION); idrk if I want this yet
				CellEmitter.center(c.pos).burst(CorrosionParticle.SPLASH, 5);
			}
		}
	}

	private void applyMagicMissileDamage( int cell ) {
		ImpBoss boss = (ImpBoss) ch;
		Char enemy = Actor.findChar(cell);
		if (enemy == null) return;

		if (boss.hit(boss, enemy, true)) {
			int dmg = Random.NormalIntRange(2, 8);
			enemy.damage(dmg, new ImpBoss.LightningBolt());
		} else {
			enemy.sprite.showStatus(CharSprite.NEUTRAL, enemy.defenseVerb());
		}
	}

	@Override
	public void onComplete( Animation anim ) {
		if (anim == die) {
			emitter().burst( Speck.factory( Speck.WOOL ), 15 );
			killAndErase();
		} else {
			super.onComplete( anim ); // handles attack == anim -> onAttackComplete(), zap == anim -> onZapComplete()
		}
	}
}