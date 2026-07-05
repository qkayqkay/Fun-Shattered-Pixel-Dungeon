package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;


import java.util.ArrayList;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Point;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;

public class Forgetful extends Buff {
    {
        revivePersists = true;
    }

    private static final float ACTIVATION_DELAY = 10f; // Activates every 10 turns for testing purposes, will be much larger in the final version (possibly randomized)

    @Override
    public boolean act() {
        if (!(target instanceof Hero) || Dungeon.level.feeling != Level.Feeling.DEMENTIA) {
            detach();
            return true;
        }

        switch (Random.Int(3)) {
            case 0:
                forgetConsumable();
                break;
            case 1:
                forgetEquipment();
                break;
            case 2:
                forgetRoom();
                break;
        }
        GLog.w(Messages.get(this, "proc"));
        spend(ACTIVATION_DELAY);

        return true;
    }
    
    @Override
    public boolean attachTo(Char target) {
        spend(ACTIVATION_DELAY);
        return super.attachTo(target);
    }

    // Forget a scroll or potion type
    private void forgetConsumable() {
        if (Random.Float() > 0.5f) {
            Class<? extends Scroll> scrollClass = Random.element(Scroll.getKnown());
            if (scrollClass != null) Reflection.newInstance(scrollClass).setKnown(false);
        } else {
            Class<? extends Potion> potionClass = Random.element(Potion.getKnown());
            if (potionClass != null) Reflection.newInstance(potionClass).setKnown(false);
        }
    }

    private void forgetEquipment() {
        Hero hero = (Hero) target;
        ArrayList<EquipableItem> identifiedItemsInInventory = new ArrayList<>();
        for (Item i : hero.belongings) {
            if (i instanceof EquipableItem && i.isIdentified()) identifiedItemsInInventory.add((EquipableItem) i);
        }
        EquipableItem itemToForget = Random.element(identifiedItemsInInventory);
        if (itemToForget != null) {
            itemToForget.cursedKnown = false;
            itemToForget.levelKnown = false;
            if (itemToForget instanceof Armor) {
                Armor a = (Armor) itemToForget;
                BrokenSeal seal = a.checkSeal();
                a.reset(); 
                if (seal != null) a.affixSeal(seal); // Resetting an armor removes the seal, so it needs to be attached back
            } else {
                itemToForget.reset();
            }
            Item.updateQuickslot();
        }
    }

    private void forgetRoom() {
        if (!(Dungeon.level instanceof RegularLevel)) return;
        RegularLevel level = (RegularLevel) Dungeon.level;
        for (int i = 0; i < 100; i++) { // Try 100 different positions and hopefully find a revealed tile
            int position = Random.Int(level.length());
            if (level.visited[position] || level.mapped[position]) {
                Room r = level.room(position);
                if (r != null) {
                    for (Point p : r.getPoints()) {
                        int cell = level.pointToCell(p);
                        if (!level.heroFOV[cell] && level.map[cell] != Terrain.WALL) {
                            level.visited[cell] = false;
                            level.mapped[cell] = false;
                        }
                    }
                    Dungeon.observe();
                    GameScene.updateFog();
                    return;
                }
            }
        }
    }
}
