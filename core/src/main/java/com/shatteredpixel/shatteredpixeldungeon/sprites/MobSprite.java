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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Rabies;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Game;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.noosa.tweeners.ScaleTweener;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

public class MobSprite extends CharSprite {

	private static final float FADE_TIME	= 3f;
	private static final float FALL_TIME	= 1f;

	public boolean disguised = false;
	public Class<? extends Mob> disguiseMobClass;
	private float disguiseTimer = 0;


	@Override
	public void update() {
		sleeping = ch != null && ch.isAlive() && ((Mob)ch).state == ((Mob)ch).SLEEPING;
		super.update();
		updateDelirium();
	}

	private boolean midActionAnimation() { // basically we dont want the swap to occur while an animation is happening.
		return curAnim != null && !curAnim.looped && !finished;
	}

	private void updateDelirium() {
		Mob mob = (ch instanceof Mob) ? (Mob) ch : null;
		Rabies rabies = Dungeon.hero.buff(Rabies.class);
		boolean active = mob != null && mob.isAlive() && rabies != null && rabies.stage >= 4;

		if (midActionAnimation()) { // there might be another solution but im lazy... don't swap mid attack/operate/zap/die, try again next frame
			return;
		}

		if (active) {
			disguiseTimer -= Game.elapsed;
			if (!disguised || disguiseTimer <= 0) {
				disguiseTimer = 1f;
				disguiseMobClass = Delirium.randomDisguise(mob);
				disguise(Delirium.spriteClassFor(disguiseMobClass));

				disguised = true;
			}
		} else if (disguised) {
			disguise(mob.spriteClass);
			disguised = false;
			disguiseMobClass = null;
		}
	}
	
	@Override
	public void onComplete( Animation anim ) {
		
		super.onComplete( anim );
		
		if (anim == die && parent != null) {
			parent.add( new AlphaTweener( this, 0, FADE_TIME ) {
				@Override
				protected void onComplete() {
					MobSprite.this.killAndErase();
				}
			} );
		}
	}
	
	public void fall() {
		
		origin.set( width / 2, height - DungeonTilemap.SIZE / 2 );
		angularSpeed = Random.Int( 2 ) == 0 ? -720 : 720;
		am = 1;

		hideEmo();

		if (health != null){
			health.killAndErase();
		}
		
		if (parent != null) parent.add( new ScaleTweener( this, new PointF( 0, 0 ), FALL_TIME ) {
			@Override
			protected void onComplete() {
				MobSprite.this.killAndErase();
				parent.erase( this );
			}
			@Override
			protected void updateValues( float progress ) {
				super.updateValues( progress );
				y += 12 * Game.elapsed;
				am = 1 - progress;
			}
		} );
	}



	public void disguise(Class<? extends CharSprite> disguiseSpriteClass) {
		MobSprite temp = (MobSprite) Reflection.newInstance(disguiseSpriteClass);
		if (temp == null) return;

		texture = temp.texture;
		idle    = temp.idle;
		run     = temp.run;
		attack  = temp.attack;
		operate = temp.operate;
		zap     = temp.zap;
		die     = temp.die;

		idle(); // refresh currently displayed frame immediately
	}
}
