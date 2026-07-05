package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DM100Sprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DM200Sprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ImpSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class ImpBoss extends Mob implements Callback {

    // WANDS
    public static final int CORROSION_WAND = 0;
    public static final int REGROWTH_WAND = 1;
    public static final int FROST_WAND = 2;
    public static final int FIREBLAST_WAND = 3;
    public static final int MAGIC_MISSILE_WAND = 4;
    public static final int LIGHTNING_WAND = 5;
    public static final int BLAST_WAVE_WAND = 6;

    public int currentWand = LIGHTNING_WAND;


    private ArrayList<Integer> currentlyEquipped = new ArrayList<Integer>();


    public ArrayList<Integer> getEquipped(){
        return currentlyEquipped;
    }

    public Integer getEquipped(int type) {
        int index = currentlyEquipped.indexOf(type);
        return index == -1 ? null : currentlyEquipped.get(index);
    }


    public boolean hasEquipped(int type) {
        return getEquipped(type) != null;
    }


    private static final float TIME_TO_ZAP	= 1f;
    {
        spriteClass = ImpSprite.class;

        HP = HT = 1;
        defenseSkill = 12;

        //loot = Random.oneOf(Generator.Category.WEAPON, Generator.Category.ARMOR);
        //lootChance = 0.2f;

        properties.add(Property.BOSS);
        properties.add(Property.DEMONIC);

    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange( 2, 8 );
    }

    @Override
    public int attackSkill( Char target ) {
        return 11;
    }

    @Override
    public int drRoll() {
        return super.drRoll() + Random.NormalIntRange(0, 4);
    }

    @Override
    protected boolean canAttack( Char enemy ) {
        return super.canAttack(enemy)
                || new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos;
    }

    //used so resistances can differentiate between melee and magical attacks
    public static class LightningBolt{}

    private void pickWand() {
        if (!currentlyEquipped.isEmpty()) {
            currentWand = Random.element(currentlyEquipped);
        }
    }

    private void handleWands(Char enemy){ // TODO do I want to keep this structure?
        if(hasEquipped(0)){ // corrosion wand

        }
        if(hasEquipped(1)){ // regrowth

        }
        if(hasEquipped(2)){ // frost

        }
        if(hasEquipped(3)){ // fireblast

        }
        if(hasEquipped(4)){ // magic missile

        }
        if(hasEquipped(5)){ //lightning

        }
        if(hasEquipped(6)){ // blastwave

        }

    }

    @Override
    protected boolean doAttack( Char enemy ) {

        if (Dungeon.level.adjacent( pos, enemy.pos )
                || new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos != enemy.pos) {

            return super.doAttack( enemy );

        } else {

            pickWand(); // TODO rework this, this is only for testing

            spend(TIME_TO_ZAP);
            Invisibility.dispel(this);

            if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
                sprite.zap(enemy.pos, this); // I pass the boss obj itself as the callback -> call() -> next()
                return false;
            } else {
                //resolveZapEffect(enemy.pos); TODO write this eventually prob
                return true;
            }
        }
    }


    public void onZapComplete() {
        next();
    }

    @Override
    public void call() {
        next();
    }


}
