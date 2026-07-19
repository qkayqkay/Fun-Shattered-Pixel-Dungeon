package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite.Glowing;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

public class StatTrak extends Weapon.Enchantment {

    private static Glowing RED = new Glowing(0xBB0000);

    private int hits = 0;
    private int kills = 0;
    private int damageDealt = 0;
    private int abilityUsed = 0;

    @Override
    public int proc(Weapon weapon, Char attacker, Char defender, int damage) {
        hits++;
        if (hits % 100 == 0) { // Big damage on every 100th attack
            damage *= 5 * procChanceMultiplier(attacker);
            GLog.h(Messages.get(this, "100_hits", hits));
        }
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
        if (abilityUsed > 0) {
            description += Messages.get(this, "desc_ability", abilityUsed);
        }
        return description;
    }

    public void countAbility() {
        abilityUsed++;
    }

    private static String HITS = "hits";
    private static String KILLS = "kills";
    private static String DAMAGE = "damage";
    private static String ABILITY = "ability";

    @Override
    public void restoreFromBundle(Bundle bundle) {
        this.hits = bundle.getInt(HITS);
        this.kills = bundle.getInt(KILLS);
        this.damageDealt = bundle.getInt(DAMAGE);
        this.abilityUsed = bundle.getInt(ABILITY);
    }
    
    @Override
    public void storeInBundle(Bundle bundle) {
        bundle.put(HITS, this.hits);
        bundle.put(KILLS, this.kills);
        bundle.put(DAMAGE, this.damageDealt);
        bundle.put(ABILITY, this.abilityUsed);
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
                if (statTrak.kills == 100) {
                    GLog.h(Messages.get(statTrak, "100_kills"));
                }
            }
        }
    }
    
}
