package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;


import java.util.ArrayList;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
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

public class Rabies extends Buff {
    {
        revivePersists = true;
        type = buffType.NEGATIVE;
    }

    private static final int[] stage_thresholds = {3, 5, 7, 9, 12};
    public int stage = 1;
    int floorsTravelled = 0;
    int lastFloor = Dungeon.depth;
    int durationOnWater = 0;
    int vertigoMult = 0;
    public float waterStepMult = 0;
    public float drinkFailProb = 0;
    public float drinkBreakProb = 0;

    @Override
    public boolean act() {
        if(Dungeon.depth > lastFloor){
            GLog.i("NEW FLOOR");
            lastFloor = Dungeon.depth;
            floorsTravelled++;
        }
        // acumulates over 12 floors
        if (floorsTravelled < stage_thresholds[0])       stage = 1; // does nothing
        else if (floorsTravelled < stage_thresholds[1])  stage = 2; // attack 20% faster
        else if (floorsTravelled < stage_thresholds[2])  stage = 3; // start become hydrophobic. You'll fail to drink hp pots and stepping on water makes you disoriented
        else if (floorsTravelled < stage_thresholds[3])  stage = 4; // delirious, and way more hydrophobic. You'll also break potions sometimes
        else if (floorsTravelled < stage_thresholds[4])  stage = 5; // start taking damage

        if(stage == 3) {
            waterStepMult = 0.04f;
            drinkFailProb = 0.30f;
            drinkBreakProb = 0f;
            vertigoMult = 2;
        } else if (stage == 4 || stage == 5) {
            waterStepMult = 0.10f;
            drinkFailProb = 0.60f;
            drinkBreakProb = 0.3f;
            vertigoMult = 4;

        }

        if(stage >= 3) {
            if (Dungeon.level.water[target.pos]) {
                durationOnWater++;
            } else if (durationOnWater != 0) {
                durationOnWater = 0;
            }
            if(Random.Float() < durationOnWater*waterStepMult){
                Buff.prolong(target, Vertigo.class, Random.Int(vertigoMult,2*vertigoMult));
            }
        }


        if(stage == 5){
            target.damage(1, Rabies.class);
        }

        spend(TICK);
        return true;
    }

    public int attemptDrink(){
        if(Random.Float() <= drinkFailProb){
            return 1; // fail to drink
        }
        else if(Random.Float() < drinkBreakProb){
            return 2; // break drink
        }
        return 0;
    }


    @Override
    public boolean attachTo(Char target) {
        return super.attachTo(target);
    }
    public int icon() {
        switch(stage) {
            case 1:
                return BuffIndicator.NONE;
            case 2:
                return BuffIndicator.RABIES_2;
            case 3:
                return BuffIndicator.RABIES_3;
            case 4:
                return BuffIndicator.RABIES_4;
            case 5:
                return BuffIndicator.RABIES_5;
            default:
                return BuffIndicator.NONE;
        }
    }

    @Override
    public String desc() {
        int turnsRemaining = stage_thresholds[stage - 1] - floorsTravelled;
        return Messages.get(this, "desc_stage" + stage, turnsRemaining);
    }
}
