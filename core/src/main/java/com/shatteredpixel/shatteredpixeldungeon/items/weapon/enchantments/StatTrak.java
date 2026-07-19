package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite.Glowing;
import com.watabou.utils.Bundle;

public class StatTrak extends Weapon.Enchantment {

    private static Glowing RED = new Glowing(0xBB0000);

    private int hits = 0;
    private int kills = 0;
    private int damageDealt = 0;

    @Override
    public int proc(Weapon weapon, Char attacker, Char defender, int damage) {
        hits++;
        damageDealt += damage;
        if (defender.buff(StatTracker.class) == null) {
            new StatTracker(this).attachTo(defender);
        }
        
        return damage;
    }

    @Override
    public Glowing glowing() {
        return RED;
    }

    @Override
    public String desc() {
        String description = Messages.get(this, "desc");
        if (kills > 0) {
            description += Messages.get(this, "desc_kills", kills);
        }
        if (hits > 0) {
            description += Messages.get(this, "desc_hits", hits);
        }
        if (damageDealt > 0) {
            description += Messages.get(this, "desc_damage", damageDealt);
        }
        return description;
    }

    private static String HITS = "hits";
    private static String KILLS = "kills";
    private static String DAMAGE = "damage";

    @Override
    public void restoreFromBundle(Bundle bundle) {
        this.hits = bundle.getInt(HITS);
        this.kills = bundle.getInt(KILLS);
        this.damageDealt = bundle.getInt(DAMAGE);
    }
    
    @Override
    public void storeInBundle(Bundle bundle) {
        bundle.put(HITS, this.hits);
        bundle.put(KILLS, this.kills);
        bundle.put(DAMAGE, this.damageDealt);
    }

    // This buff should get detached immediately so there's no reason to store it in bundle
    public static class StatTracker extends Buff {
        StatTrak statTrak;

        public StatTracker(StatTrak statTrak) {
            this.statTrak = statTrak;
        }

        @Override
        public boolean act() {
            detach();
            return true;
        }
        
        public void countKill() {
            if (statTrak != null) {
                statTrak.kills++;
            }
        }
    }
    
}
