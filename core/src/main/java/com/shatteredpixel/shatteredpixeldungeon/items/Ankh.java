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

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite.Glowing;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Ankh extends Item {

	public static final String AC_BLESS = "BLESS";
	public static final String AC_ACTIVATE = "ACTIVATE";


	{
		image = ItemSpriteSheet.ANKH;

		//You tell the ankh no, don't revive me, and then it comes back to revive you again in another run.
		//I'm not sure if that's enthusiasm or passive-aggression.
		bones = true;

	}

	float activationChance = 0.07f;
	boolean isActivated = false;
	private boolean blessed = false;
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions(hero);
		Waterskin waterskin = hero.belongings.getItem(Waterskin.class);
		if (waterskin != null && waterskin.isFull() && !blessed){
			actions.add( AC_BLESS );
		}
		if(!this.isActivated) {
			actions.add(AC_ACTIVATE);
		}
		return actions;
	}

	@Override
	public void execute( final Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals( AC_BLESS )) {

			Waterskin waterskin = hero.belongings.getItem(Waterskin.class);
			if (waterskin != null){
				blessed = true;
				waterskin.empty();
				GLog.p( Messages.get(this, "bless") );
				hero.spend( 1f );
				hero.busy();


				Sample.INSTANCE.play( Assets.Sounds.DRINK );
				CellEmitter.get(hero.pos).start(Speck.factory(Speck.LIGHT), 0.2f, 3);
				hero.sprite.operate( hero.pos );
			}
		}
		if(action.equals(AC_ACTIVATE)){
			Emitter e = curUser.sprite.centerEmitter();
			e.pos(e.x-2, e.y-6, 4, 4);

			float trueActivationChance = blessed ? activationChance*1.02f : activationChance;
			hero.spend( 1f );
			hero.busy();
			hero.sprite.operate( hero.pos );
			if(Random.Float() < trueActivationChance){
				isActivated = true;
				Sample.INSTANCE.play( Assets.Sounds.MASTERY );
				e.start(Speck.factory(Speck.MASK), 0.05f, 20);
				GLog.p(Messages.get(this, "activate_success"));
			}
			else{
				Sample.INSTANCE.play(Assets.Sounds.EVOKE);
				e.burst( Speck.factory( Speck.FORGE ), 3 );
				GLog.n(Messages.get(this, "activate_fail"));
				activationChance+= 0.01f;
			}
		}
	}
	
	@Override
	public String desc() {
		if (blessed) {
			if (isActivated) {
				return Messages.get(this, "desc_blessed_activated");
			}
			return Messages.get(this, "desc_blessed_unactivated");
		}
		else {
			if (isActivated) {
				return Messages.get(this, "desc_activated");
			}
			return Messages.get(this, "desc_unactivated");
		}

	}

	public boolean isBlessed(){
		return blessed;
	}

	public boolean isActivated(){
		return isActivated;
	}

	public void bless(){
		blessed = true;
	}

	private static final Glowing WHITE = new Glowing( 0xFFFFCC );

	@Override
	public Glowing glowing() {
		return isBlessed() ? WHITE : null;
	}

	private static final String BLESSED = "blessed";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( BLESSED, blessed );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		blessed	= bundle.getBoolean( BLESSED );
	}
	
	@Override
	public int value() {
		return 50 * quantity;
	}
}
