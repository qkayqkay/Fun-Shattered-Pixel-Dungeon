package com.shatteredpixel.shatteredpixeldungeon.items.spells;

import java.util.ArrayList;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char.Alignment;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class GravityShift extends TargetedSpell {

    {
        image = ItemSpriteSheet.PHASE_SHIFT;
		usesTargeting = true;
	}

    @Override
    protected void affectTarget(Ballistica bolt, Hero hero) {
        if (bolt.path.size() > 1) {
            int direction = bolt.path.get(1) - bolt.path.get(0);
            ArrayList<Char> affectedChars = new ArrayList<>();

            for (Char ch : Actor.chars()) {
                if (!Char.hasProp(ch, Char.Property.IMMOVABLE) && ch.alignment != Alignment.ALLY && Dungeon.level.heroFOV[ch.pos]) {
                    Actor.delayChar(ch, 1f);
                    affectedChars.add(ch);
                }
            }
            new GravityShiftTracker(affectedChars, direction).attachTo(hero);
        }
        onSpellused();
    }
    

    private static class GravityShiftTracker extends Buff {
        {
            actPriority = BUFF_PRIO - 9; //acts after other buffs
        }

        ArrayList<Char> affectedChars;
        ArrayList<Char> blocked = new ArrayList<>();
        int direction;

        public GravityShiftTracker(ArrayList<Char> affectedChars, int direction) {
            this.affectedChars = affectedChars;
            this.direction = direction;
        }

	    @Override
        public boolean act() {
            // Idk what's going on here, I just copied it from GravityChaosTracker
		    for (Char ch : Actor.chars()){
			    try {
				    synchronized (ch.sprite) {
					    if (ch.sprite.isMoving) {
						    ch.sprite.wait();
				    	}
				    }
			    } catch (InterruptedException e) {

			    }
		    }

            if (!blocked.isEmpty()) {
                boolean blockedremoved = false;
                for (Char ch : blocked.toArray(new Char[0])) {
                    Ballistica path = new Ballistica(ch.pos, ch.pos + direction, Ballistica.MAGIC_BOLT);
                    if (!(path.dist == 1 && Actor.findChar(path.collisionPos) != null)) {
                        WandOfBlastWave.throwChar(ch, path, 3, false, false, this);
                        blocked.remove(ch);
                        blockedremoved = true;
                    }
                }
                if (!blockedremoved || blocked.isEmpty()) {
                    blocked.clear();
                    detach();
                    return true;
                } else {
                    return true;
                }
            }

            for (Char ch : affectedChars){

			    if (ch instanceof Mob && ((Mob) ch).state == ((Mob) ch).SLEEPING){
				    ((Mob) ch).state = ((Mob) ch).WANDERING;
			    }
			    Ballistica path = new Ballistica(ch.pos, ch.pos + direction, Ballistica.MAGIC_BOLT);
				if (path.dist == 1 && Actor.findChar(path.collisionPos) != null){
					blocked.add(ch);
				} else {
                    WandOfBlastWave.throwChar(ch, path, 3, false, false, this);
				}
		    }

		    if (blocked.isEmpty()){
				detach();
		    }
		    return true;
	    }
    }
}
