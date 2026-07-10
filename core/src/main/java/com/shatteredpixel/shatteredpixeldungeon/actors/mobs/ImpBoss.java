package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.effects.*;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.CorrosionParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.*;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCorrosion;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.TenguDartTrap;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DM100Sprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DM200Sprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ImpSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.lang.reflect.Array;
import java.util.*;
import java.util.Map;


public class ImpBoss extends Mob implements Callback {

    // WANDS: type "wand"
    public static final int CORROSION_WAND = 0;
    public static final int FROST_WAND = 1;
    public static final int FIREBLAST_WAND = 2;
    public static final int MAGIC_MISSILE_WAND = 3;
    public static final int LIGHTNING_WAND = 4;
    public static final int BLAST_WAVE_WAND = 5;

    // RINGS: type "ring"
    public static final int RING_OF_HASTE = 6; // DONE
    public static final int RING_OF_ENERGY = 7;
    public static final int RING_OF_ELEMENTS = 8; // DONE
    public static final int RING_OF_FUROR = 9;
    public static final int RING_OF_TENACITY = 10; //DONE
    public static final int RING_OF_ACCURACY = 11; // DONE
    public static final int RING_OF_EVASION = 12; // DONE
    public static final int RING_OF_SHARPSHOOTING = 13; // Not sure yet if I want this?

    // ARMOR: type "armor"
    //public  do I actually want armor...?


    public int currentWand;
    public ArrayList<Ring> equippedRings = new ArrayList<>();


    private Map<String, ArrayList<Integer>> equipped = new HashMap<>();
    {
        equipped.put("wand", new ArrayList<>()); // implemented
        equipped.put("ring", new ArrayList<>()); // TODO
        equipped.put("melee", new ArrayList<>()); // TODO
        equipped.put("armor", new ArrayList<>()); // TODO
    }


    public Map<String, ArrayList<Integer>> getEquipped(){
        return equipped;
    }

    public ArrayList<Integer> getEquipped(String type) {
        return getEquipped().get(type);
    }


    public boolean hasEquipped(int type) {
        for (String key : equipped.keySet()) {
            for (int i : equipped.get(key)) {
                if (i == type) {
                    return true;
                }
            }
        }
        return false;
    }


    private static final float TIME_TO_ZAP	= 1f;
    {
        spriteClass = ImpSprite.class;

        HP = HT = 10;
        defenseSkill = 12;

        //loot = Random.oneOf(Generator.Category.WEAPON, Generator.Category.ARMOR);
        //lootChance = 0.2f;

        properties.add(Property.BOSS);
        properties.add(Property.DEMONIC);

    }

    @Override
    public void die(Object cause) {
        ImpSprite impSprite = (ImpSprite) sprite;

        Image snapshot = new Image(sprite);
        snapshot.point(sprite.point());
        sprite.parent.add(snapshot);

        ImpBossDeathFX fx = new ImpBossDeathFX(sprite.center().x, sprite.center().y, snapshot, impSprite.frames);
        sprite.parent.add(fx);

        super.die(cause);
    }

    private Ring createRing(int type){
        switch(type){
            case(RING_OF_HASTE):
                return new RingOfHaste();
            case(RING_OF_ENERGY):
                return new RingOfEnergy();
            case(RING_OF_ELEMENTS):
                return new RingOfElements();
            case(RING_OF_FUROR):
                return new RingOfFuror();
            case(RING_OF_SHARPSHOOTING):
                return new RingOfSharpshooting();
            case(RING_OF_ACCURACY):
                return new RingOfAccuracy();
            case(RING_OF_EVASION):
                return new RingOfEvasion();
            case(RING_OF_TENACITY):
                return new RingOfTenacity();
            default:
                return null;
        }
    }
    private void equipRing(int ringType) {
        Ring ring = createRing(ringType);
        if (ring == null) return;

        ring.random();
        ring.upgrade(100);
        ring.cursed = false;

        equippedRings.add(ring);
        ring.activate(this); // attaches the RingBuff to the boss
    }

    /*private void equip(Wand wand){

    }

    private void equip(MeleeWeapon melee){

    }

    private void equip(Armor armor){

    }*/


    public int wandBoltType() {
        switch (currentWand) {
            case CORROSION_WAND: return MagicMissile.CORROSION;
            case MAGIC_MISSILE_WAND: return MagicMissile.MAGIC_MISSILE;
            case FROST_WAND: return MagicMissile.FROST;
            case BLAST_WAVE_WAND: return MagicMissile.FORCE;
            case FIREBLAST_WAND: return MagicMissile.FIRE_CONE;
            case LIGHTNING_WAND:
            default: return -1;
        }
    }

    public void resolveWandEffect( int cell ) {
        switch (currentWand) {
            case CORROSION_WAND: applyCorrosionEffect(cell); break;
            case MAGIC_MISSILE_WAND: applyMagicMissileDamage(cell); break;
            case FROST_WAND: applyFrostDamage(cell); break;
            case BLAST_WAVE_WAND: applyBlastDamage(cell); break;
            case FIREBLAST_WAND: applyFireblastDamage(cell); break;
            case LIGHTNING_WAND: applyLightningDamage(cell); break;
            default: applyLightningDamage(cell); break;
        }
    }


    private void applyLightningDamage( int cell ) {
        Char enemy = Actor.findChar(cell);
        if (enemy == null) return;

        if (this.hit(this, enemy, true)) {
            int dmg = Random.NormalIntRange(3, 10);
            dmg = Math.round(dmg * AscensionChallenge.statModifier(this));
            enemy.damage(dmg, new ImpBoss.LightningBolt());

            if (enemy.sprite.visible) {
                enemy.sprite.centerEmitter().burst(SparkParticle.FACTORY, 3);
                enemy.sprite.flash();
            }
            if (enemy == Dungeon.hero) {
                PixelScene.shake(2, 0.3f);
                if (!enemy.isAlive()) {
                    Badges.validateDeathFromEnemyMagic();
                    Dungeon.fail(this);
                    GLog.n(Messages.get(this, "zap_kill"));
                }
            }
        } else {
            enemy.sprite.showStatus(CharSprite.NEUTRAL, enemy.defenseVerb());
        }
    }

    private void applyCorrosionEffect( int cell ) {
        CorrosiveGas gas = Blob.seed(cell, 50, CorrosiveGas.class);
        CellEmitter.get(cell).burst(Speck.factory(Speck.CORROSION), 10);
        gas.setStrength(2, WandOfCorrosion.class);
        GameScene.add(gas);
        Sample.INSTANCE.play(Assets.Sounds.GAS);

        /*for (int i : PathFinder.NEIGHBOURS9) {
            Char c = Actor.findChar(cell + i);
            if (c != null) {
                //Buff.affect(c, Ooze.class).set(Ooze.DURATION); idrk if I want this yet
                CellEmitter.center(c.pos).burst(CorrosionParticle.SPLASH, 5);
            }
        }*/
    }

    private void applyMagicMissileDamage( int cell ) {
        Sample.INSTANCE.play(Assets.Sounds.ZAP);
        Char enemy = Actor.findChar(cell);
        if (enemy == null) return;

        if (this.hit(this, enemy, true)) {
            int dmg = Random.NormalIntRange(2, 8);
            enemy.damage(dmg, new ImpBoss.MagicBolt());
        } else {
            enemy.sprite.showStatus(CharSprite.NEUTRAL, enemy.defenseVerb());
        }
    }

    private void applyFrostDamage(int cell){
        Sample.INSTANCE.play(Assets.Sounds.ZAP);
        Char enemy = Actor.findChar(cell);
        int damage = this.damageRoll();
        int level = 1; //todo prob take this into account
        if(enemy == null) return;
        if (enemy.buff(Frost.class) != null){
            return; //do nothing, can't affect a frozen target
        }
        if (enemy.buff(Chill.class) != null){
            //6.67% less damage per turn of chill remaining, to a max of 10 turns (50% dmg)
            float chillturns = Math.min(10, enemy.buff(Chill.class).cooldown());
            damage = (int)Math.round(damage * Math.pow(0.9333f, chillturns));
        } else {
            enemy.sprite.burst( 0xFF99CCFF, level / 2 + 2 );
        }
        if(hit(this, enemy, true)){
            enemy.damage(damage, new ImpBoss.FrostBolt());
        }
    }

    private void applyBlastDamage( int cell ) {
        Sample.INSTANCE.play(Assets.Sounds.BLAST);
        WandOfBlastWave.BlastWave.blast(cell);

        for (int i : PathFinder.NEIGHBOURS9) {
            Char c = Actor.findChar(cell + i);
            if (c != null && hit(this, c, true)) {
                c.damage(damageRoll(), this);
                if (c.isAlive()) {
                    Ballistica trajectory = new Ballistica(c.pos, c.pos + i, Ballistica.MAGIC_BOLT);
                    WandOfBlastWave.throwChar(c, trajectory, 3, false, true, this);
                }
            }
        }
    }

    private void applyFireblastDamage( int cell ) {
        Sample.INSTANCE.play(Assets.Sounds.ZAP);
        Sample.INSTANCE.play(Assets.Sounds.BURNING);

        GameScene.add(Blob.seed(cell, 2, Fire.class));

        Char enemy = Actor.findChar(cell);
        if (enemy == null) return;

        if (hit(this, enemy, true)) {
            enemy.damage(damageRoll(), this);
            if (enemy.isAlive()) {
                Buff.affect(enemy, Burning.class).reignite(enemy);
            }
        } else {
            enemy.sprite.showStatus(CharSprite.NEUTRAL, enemy.defenseVerb());
        }
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange( 2, 2 );
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
    public static class MagicBolt{}
    public static class FrostBolt{}

    private void pickWand() {
        if (!getEquipped("wand").isEmpty()) {
            currentWand = FIREBLAST_WAND;//Random.element(currentlyEquipped);
        }
        currentWand = FROST_WAND;

    }

    @Override
    public float speed() {
        float speed = super.speed();
        speed *= RingOfHaste.speedMultiplier(this);
        return speed;
    }

    @Override
    public int defenseSkill(Char enemy) {
        int evasion = super.defenseSkill(enemy);
        evasion *= RingOfEvasion.evasionMultiplier( this );
        return Math.max(1, Math.round(evasion));
    }

    @Override
    public int attackSkill(Char target){
        //KingOfWeapon = this.weapon;
        float accuracy = 30f;
        accuracy *= RingOfAccuracy.accuracyMultiplier(target);
        return Math.max(30, Math.round(30 * accuracy/* * wep.accuracyFactor( this, target )*/)); // TODO weapon logic in here
    }

    @Override
    public void damage(int dmg, Object src) {
        dmg = (int)Math.ceil(dmg * RingOfTenacity.damageMultiplier( this ));
        super.damage(dmg, src);
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


    @Override
    protected boolean act() {
        boolean result = super.act();
        boolean found = false;
        for(Ring ring : equippedRings){
            if(ring instanceof RingOfHaste){
                found = true;
            }
        }
        if(found == false){
            //this.equipRing(12);
        }
        return result;
    }
}
