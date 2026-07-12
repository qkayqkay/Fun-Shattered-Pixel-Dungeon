package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Bee;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Crab;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Scorpio;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Spinner;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Swarm;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite.Glowing;

public class ArthropodsBane extends Weapon.Enchantment {

    private static ItemSprite.Glowing GREEN = new ItemSprite.Glowing(0x22AA77);

    @Override
    public int proc(Weapon weapon, Char attacker, Char defender, int damage) {
        if (defender instanceof Crab    ||
            defender instanceof Swarm   ||
            defender instanceof Spinner ||
            defender instanceof Scorpio ||
            defender instanceof Bee) {

            Splash.at( defender.sprite.center(), 0x22AA77, 5);    
            return (int)(damage * 1.3f * procChanceMultiplier(attacker));
        }
        return damage;
    }

    @Override
    public Glowing glowing() {
        return GREEN;
    }
}