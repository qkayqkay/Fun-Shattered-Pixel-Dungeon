package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.*;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

public class Delirium {
    private static final Class<? extends Mob>[] SEWERS = new Class[]{Rat.class, Albino.class, Snake.class, Crab.class, HermitCrab.class, Gnoll.class, Swarm.class, Wraith.class, Slime.class};
    private static final Class<? extends Mob>[] PRISON = new Class[]{Skeleton.class, Thief.class, DM100.class, Guard.class, Necromancer.class, Swarm.class, Wraith.class, Bandit.class};
    private static final Class<? extends Mob>[] CAVES = new Class[]{Bat.class, Brute.class, Shaman.class, Spinner.class, DM200.class, Wraith.class, ArmoredBrute.class};
    private static final Class<? extends Mob>[] LIBRARY = new Class[]{Ghoul.class, Elemental.class, Warlock.class, Monk.class, Golem.class, Wraith.class};
    private static final Class<? extends Mob>[] HALLS = new Class[]{Succubus.class, Acidic.class, Eye.class, Scorpio.class, Wraith.class};


    public static Class<? extends Mob> randomDisguise(Mob real) {
        Class<? extends Mob>[] pool = poolFor(Dungeon.depth);
        Class<? extends Mob> pick;
        do { pick = Random.element(pool); } while (pick == real.getClass());
        return pick;
    }

    private static Class<? extends Mob>[] poolFor(int depth) {
        if (depth <= 5) return SEWERS;
        else if (depth <= 10) return PRISON;
        else if (depth <= 15) return CAVES;
        else if (depth <= 20) return LIBRARY;
        else if (depth <= 25) return HALLS;
        return SEWERS;
    }

    public static Class<? extends CharSprite> spriteClassFor(Class<? extends Mob> mobClass) {
        Mob temp = Reflection.newInstance(mobClass);
        return temp != null ? temp.spriteClass : null;
    }

    public static String disguisedName(Mob real) {
        if (real.sprite instanceof MobSprite) {
            MobSprite ms = (MobSprite) real.sprite;
            if (ms.disguised && ms.disguiseMobClass != null) {
                return Messages.get(ms.disguiseMobClass, "name");
            }
        }
        return real.name();
    }
}