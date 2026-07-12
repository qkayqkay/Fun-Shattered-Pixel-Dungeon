package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

public class RGB extends Weapon.Enchantment {

    @Override
    public int proc(Weapon weapon, Char attacker, Char defender, int damage) {
        return damage;
    }

    private static ItemSprite.Glowing GLOW = new ItemSprite.Glowing(true, 4f);
    private static ItemSprite.Glowing FAST_GLOW = new ItemSprite.Glowing(true, 0.4f);

    @Override
    public ItemSprite.Glowing glowing() {
        if (Dungeon.hero != null && procChanceMultiplier(Dungeon.hero) > 1f) {
            return FAST_GLOW;
        }
        return GLOW;
    }
}
