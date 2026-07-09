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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
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
import com.watabou.glwrap.Texture;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class ImpSprite extends MobSprite {

	public TextureFilm frames;
	public ImpSprite() {
		super();
		texture( Assets.Sprites.IMP );
		frames = new TextureFilm( texture, 12, 14 );

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
		die.frames( frames, 5, 6, 7, 8, 9, 10, 11 );

		play( idle );
	}

	@Override
	public void zap( int cell ) {
		zap( cell, null );
	}

	@Override
	public synchronized void zap( int cell, final Callback callback ) {
		turnTo( ch.pos, cell );
		play( zap );

		final ImpBoss boss = (ImpBoss) ch;
		int boltType = boss.wandBoltType();

		if (boltType < 0) {
			boss.resolveWandEffect(cell);
			if (callback != null) callback.call();
		} else {
			MagicMissile.boltFromChar(parent, boltType, this, cell, new Callback() {
				@Override
				public void call() {
					boss.resolveWandEffect(cell);
					if (callback != null) callback.call();
				}
			});
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