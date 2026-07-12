package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.badlogic.gdx.utils.IntMap;
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
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.*;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCorrosion;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ImpSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.*;
import java.util.Map;


public class ImpBoss extends Mob implements Callback {

    // WANDS: type "wand"
    public static final int CORROSION_WAND = 0; // aoe/area of denial
    public static final int FROST_WAND = 1; // just a minor debuff, a decent opening
    public static final int MAGIC_MISSILE_WAND = 2; // general damage
    public static final int LIGHTNING_WAND = 3; // good for multiple targets, but also pretty good for general damage
    public static final int BLAST_WAVE_WAND = 4; // pushes target back.
    public static final int FIREBLAST_WAND = 5; // im fairly certain this wand was removed, but if not, then aoe.
    // TODO WHERE'S DISINT??

    // RINGS: type "ring"
    public static final int RING_OF_HASTE = 6; // DONE
    public static final int RING_OF_ENERGY = 7;
    public static final int RING_OF_ELEMENTS = 8; // DONE
    public static final int RING_OF_FUROR = 9;
    public static final int RING_OF_TENACITY = 10; //DONE
    public static final int RING_OF_ACCURACY = 11; // DONE
    public static final int RING_OF_EVASION = 12; // DONE
    public static final int RING_OF_SHARPSHOOTING = 13; // Not sure yet if I want this?

    public static final int GREAT_AXE = 14;
    public static final int WAR_SCYTHE = 15;
    public static final int GLAIVE = 16;
    public static final int GLOVES = 17;
    public static final int GREAT_SWORD = 18;
    public static final int GREAT_SHIELD = 19;
    public static final int ASSASSIN_BLADE = 20;
    public static final int WHIP = 21;
    public static final int KATANA = 22;

    // ARMOR: type "armor"
    //public  do I actually want armor...?


    public int currentWand;
    public int currentMeleeWeapon;
    public ArrayList<Ring> equippedRings = new ArrayList<>();
    private Map<String, ArrayList<Integer>> equipped = new HashMap<>();

    private int mood = 0; // mood determines what type of weapon the imp wants to use right now, either wands(0) or melees(1). I might add throwies later
    private int turnsSinceMoodSwap = 0;
    private int turnsSinceWeaponSwap = 0; // turns since any weapon(wand or melee) was swapped. Resets when mood swaps
    private int turnsSinceLastAbility = 0;

    private int lastEnemyPos;

    {
        equipped.put("wand", new ArrayList<>()); // implemented
        equipped.put("ring", new ArrayList<>()); // implemented
        equipped.put("melee", new ArrayList<>()); // implemented
        equipped.put("armor", new ArrayList<>()); // TODO do I care tho?
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


    private static final float TIME_TO_ZAP	= 3f;
    private static final float TIME_TO_MELEE_ABILITY	= 1f;

    {
        spriteClass = ImpSprite.class;

        HP = HT = 20;
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
    private void equipMelee(int meleeType){
        currentMeleeWeapon = meleeType;
        equipped.get("melee").add(meleeType);
    }

    private void equipRing(int ringType) {
        Ring ring = createRing(ringType);
        if (ring == null) return;

        ring.random();
        ring.upgrade(100); /// TODO LOL probably +2 to +4, just like the normal imp?
        ring.cursed = false;

        equippedRings.add(ring);
        ring.activate(this); // this actually attaches the RingBuff to the boss
    }

    public boolean canUseMeleeAbility() {
        return meleeAbilityBuffClass() != null && buff(meleeAbilityBuffClass()) == null && mood == 1 && state != SLEEPING;
    }

    public void activateMeleeAbility() {

        switch (currentMeleeWeapon) {
            case GREAT_AXE:      Buff.affect(this, GreatAxe.class, 10f);   break;
            case GREAT_SHIELD:   Buff.affect(this, Shielding.class, 5f);      break;
            case GLOVES:         Buff.affect(this, HastenedStrikes.class, 3f); break;
            case WAR_SCYTHE:     Buff.affect(this, Bleeding.class, 10f);   break;
            case GREAT_SWORD:    Buff.affect(this, Crippling.class, 5f);       break;
            case ASSASSIN_BLADE:
                blinkTowardEnemy();
                Buff.affect(this, Blink.class, 4f);
                break;
            case KATANA:         Buff.affect(this, HalfZatoichi.class, 5f);         break;
            default: return; // WHIP has no activation as it's passive
        }
        spend(TIME_TO_MELEE_ABILITY);
    }

    private Class<? extends FlavourBuff> meleeAbilityBuffClass() {
        switch (currentMeleeWeapon) {
            case GREAT_AXE:      return GreatAxe.class;
            case GREAT_SHIELD:   return Shielding.class;
            case GLOVES:         return HastenedStrikes.class;
            case WAR_SCYTHE:     return Bleeding.class;
            case GREAT_SWORD:    return Crippling.class;
            case ASSASSIN_BLADE: return Blink.class;
            case KATANA:         return HalfZatoichi.class;
            default:             return null;
        }
    }

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
            case LIGHTNING_WAND: applyLightningDamage(cell); break; // TODO idrk if I like the lightning logic
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
        if (enemy == null) return;

        int damage = wandDamageRoll();
        if (enemy.buff(Frost.class) != null){
            return;
        }
        if (enemy.buff(Chill.class) != null){
            float chillturns = Math.min(10, enemy.buff(Chill.class).cooldown());
            damage = (int)Math.round(damage * Math.pow(0.9333f, chillturns));
        } else {
            enemy.sprite.burst( 0xFF99CCFF, 2 );
        }

        if (hit(this, enemy, true)){
            enemy.damage(damage, new ImpBoss.FrostBolt());
            if (enemy.isAlive()){
                if (Dungeon.level.water[enemy.pos])
                    Buff.affect(enemy, Chill.class, 4f);
                else
                    Buff.affect(enemy, Chill.class, 2f);
            }
        }
    }

    private void applyBlastDamage( int cell ) {
        Sample.INSTANCE.play(Assets.Sounds.BLAST);
        WandOfBlastWave.BlastWave.blast(cell);

        for (int i : PathFinder.NEIGHBOURS9) {
            Char c = Actor.findChar(cell + i);
            if (c != this && c !=null && hit(this, c, true)) {
                c.damage(wandDamageRoll(), this);
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
            enemy.damage(wandDamageRoll(), this);
            if (enemy.isAlive()) {
                Buff.affect(enemy, Burning.class).reignite(enemy);
            }
        } else {
            enemy.sprite.showStatus(CharSprite.NEUTRAL, enemy.defenseVerb());
        }
    }

    @Override
    public int damageRoll() {
        int dmg;
        switch (currentMeleeWeapon) {
            case GREAT_AXE:      dmg = Random.NormalIntRange(27, 35);  break;
            case WAR_SCYTHE:     dmg = Random.NormalIntRange(15, 25);  break;
            case GLAIVE:         dmg = Random.NormalIntRange(27, 35);  break;
            case GLOVES:         dmg = Random.NormalIntRange(12, 18);  break;
            case GREAT_SWORD:    dmg = Random.NormalIntRange(17, 29);  break;
            case GREAT_SHIELD:   dmg = Random.NormalIntRange(14, 20);  break;
            case ASSASSIN_BLADE: dmg = Random.NormalIntRange(16, 22);  break;
            case WHIP:           dmg = Random.NormalIntRange(14, 20);  break;
            case KATANA:         dmg = Random.NormalIntRange(16, 22);  break;
            default:              dmg = Random.NormalIntRange(2, 2); break;
        }

        if (buff(GreatAxe.class) != null) dmg = Math.round(dmg * 1.5f);
        if (buff(Crippling.class)    != null) dmg = Math.round(dmg * 1.5f);
        if (buff(Blink.class) != null) dmg *= 2;
        return dmg;
    }

    private int wandDamageRoll() {
        switch (currentWand) {
            case FIREBLAST_WAND:   return Random.NormalIntRange(11, 15);
            case FROST_WAND:   return Random.NormalIntRange(12, 16);
            case BLAST_WAVE_WAND:  return Random.NormalIntRange(2, 5);
            default:                return Random.NormalIntRange(2, 4);
        }
    }


    @Override
    public int drRoll() {
        return super.drRoll() + Random.NormalIntRange(0, 4);
    }

    @Override
    protected boolean canAttack(Char enemy) {
        if (mood == 1) { // melee mood
            if (currentMeleeWeapon == WHIP) {
                PathFinder.Path path = Dungeon.findPath(this, enemy.pos, Dungeon.level.passable, Dungeon.level.discoverable, true);
                if (path != null && path.size() <= 8) return true;
            }
            return Dungeon.level.adjacent(pos, enemy.pos);
        } else { // wand mood
            return new Ballistica(pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos;
        }
    }

    @Override
    protected boolean doAttack(Char enemy) {
        if (mood == 1) {
            return super.doAttack(enemy);
        } else {
            spend(TIME_TO_ZAP);
            Invisibility.dispel(this);

            if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
                int targetCell = enemy.pos;
                if(lastEnemyPos != enemy.pos){
                    if(currentWand == CORROSION_WAND){ // ts is ripped straight from spinner.java
                        Ballistica b = new Ballistica(lastEnemyPos, enemy.pos, Ballistica.WONT_STOP);
                        int collisionIndex = 0;
                        for (int i = 0; i < b.path.size(); i++){
                            if (b.path.get(i) == enemy.pos){
                                collisionIndex = i;
                                break;
                            }
                        }

                        if (b.path.size() > collisionIndex+1) {
                            int coroPos = b.path.get(collisionIndex + Random.Int(2)+1);
                            //ensure we aren't shooting the web through walls
                            int projectilePos = new Ballistica(pos, coroPos, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID).collisionPos;

                            if (coroPos != enemy.pos && projectilePos == coroPos && Dungeon.level.passable[coroPos]) {
                                targetCell = coroPos;
                            }
                        }
                    }
                    if(currentWand == BLAST_WAVE_WAND){
                        Ballistica b = new Ballistica(pos, enemy.pos, Ballistica.MAGIC_BOLT);
                        if (b.dist >= 2) {
                            int blastPos = b.path.get(b.dist - 1);
                            if (blastPos != pos && Dungeon.level.passable[blastPos]) {
                                targetCell = blastPos;
                            }
                        }
                    }
                }
                sprite.zap(targetCell, this);
                return false;
            } else {
                return true;
            }
        }
    }

    // TODO used so resistances can differentiate between melee and magical attacks. TODO This is actually not taken into account i got lazy
    public static class LightningBolt{}
    public static class MagicBolt{}
    public static class FrostBolt{}

    private void pickWand() {
        currentWand = calculateWandScores();
    }

    private void pickMelee(){
        currentMeleeWeapon = calculateWeaponScores();
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
        float accuracy = 30f;
        accuracy *= meleeAccuracyMultiplier();
        accuracy *= RingOfAccuracy.accuracyMultiplier(target);
        return Math.max(10, Math.round(accuracy));
    }

    private float meleeAccuracyMultiplier() {
        switch (currentMeleeWeapon) {
            case GREAT_AXE:      return 0.9f;
            case WAR_SCYTHE:     return 0.8f;
            case GLAIVE:         return 0.9f;
            case GLOVES:         return 1.1f;
            case GREAT_SWORD:    return 0.9f;
            case GREAT_SHIELD:   return 1.0f;
            case ASSASSIN_BLADE: return (buff(Blink.class) != null) ? Float.POSITIVE_INFINITY : 1.0f;
            case WHIP:           return 1.0f;
            case KATANA:         return 1.2f;
            default:              return 1.0f;
        }
    }

    @Override
    public void damage(int dmg, Object src) {
        dmg = (int) Math.ceil(dmg * RingOfTenacity.damageMultiplier(this));
        if (buff(Shielding.class) != null) dmg = Math.round(dmg * 0.5f);
        super.damage(dmg, src);
    }

    @Override
    public float attackDelay() {
        float delay = super.attackDelay();
        if (buff(HastenedStrikes.class) != null) delay /= 2f;
        return delay;
    }

    @Override
    public int attackProc(Char enemy, int damage) {
        damage = super.attackProc(enemy, damage);
        postHitMeleeAbility(enemy);

        if (buff(GreatAxe.class) != null) buff(GreatAxe.class).detach();
        if (buff(Blink.class) != null) buff(Blink.class).detach();

        return damage;
    }


    public void onZapComplete() {
        next();
    }

    @Override
    public void call() {
        next();
    }


    private void processTurn(){
        if(turnsSinceMoodSwap >= 15 && Random.Int(2) == 0){
            int newMood = evaluateMoodSwap();
            if(newMood != mood){
                mood = newMood;
                turnsSinceMoodSwap = 0;
                turnsSinceWeaponSwap = 5;
                if (sprite != null) sprite.showStatus(CharSprite.POSITIVE, Messages.get(ImpBoss.class, "mood"+mood));
            }
        }

        if(turnsSinceWeaponSwap >= 3 && Random.Int(3) == 0){
            if(mood == 0){
                pickWand();
                if (sprite != null) sprite.showStatus(CharSprite.POSITIVE, Messages.get(ImpBoss.class, "gear"+currentWand));
            }
            else if(mood == 1){
                pickMelee();
                if (sprite != null) sprite.showStatus(CharSprite.POSITIVE, Messages.get(ImpBoss.class, "gear"+currentMeleeWeapon));
            }
            turnsSinceWeaponSwap = 0;
        }

        if(canUseMeleeAbility() && turnsSinceLastAbility >= 5){
            activateMeleeAbility();
            turnsSinceLastAbility = 0;
        }
        turnsSinceLastAbility++;
        lastEnemyPos = enemy.pos;
    }

    @Override
    protected boolean act() {
        boolean result = super.act();
        if(enemy!=null && state != SLEEPING) {
            processTurn();
            turnsSinceMoodSwap++;
            turnsSinceWeaponSwap++;
        }
        return result;
    }


    public static class GreatAxe extends FlavourBuff {
        {
        type = buffType.POSITIVE;
        announced = true;
        }

        @Override
        public int icon() {
            return BuffIndicator.RAGE;
        }
    }
    public static class Shielding extends FlavourBuff {
        {
        type = buffType.POSITIVE;
        announced = true;
        }

        @Override
        public int icon() {
            return BuffIndicator.SEAL_SHIELD;
        }
    }
    public static class HastenedStrikes extends FlavourBuff {
        {
        type = buffType.POSITIVE;
        announced = true;
        }

        @Override
        public int icon() {
            return BuffIndicator.MOMENTUM;
        }
    }
    public static class Bleeding extends FlavourBuff {
        {
        type = buffType.POSITIVE;
        announced = true;
        }

        @Override
        public int icon() {
            return BuffIndicator.BLEEDING;
        }
    }
    public static class Crippling extends FlavourBuff {
        {
        type = buffType.POSITIVE;
        announced = true;
        }

        @Override
        public int icon() {
            return BuffIndicator.CHALLENGE;
        }
    }
    public static class Blink extends FlavourBuff {
        {
        type = buffType.POSITIVE;
        announced = true;
        }

        @Override
        public int icon() {
            return BuffIndicator.INVISIBLE;
        }
    }


    public static class HalfZatoichi extends FlavourBuff {
        {
            type = buffType.POSITIVE;
            announced = true;
        }
        public boolean hitLanded = false;

        @Override
        public void detach() {
            Char ch = target;
            super.detach();
            if (ch != null) {
                int amt = Math.round(ch.HP * (hitLanded ? 0.10f : -0.05f));
                ch.HP = Math.max(0, Math.min(ch.HT, ch.HP + amt));
            }
        }

        @Override
        public int icon() {
            return BuffIndicator.HERB_HEALING;
        }
    }


    private void postHitMeleeAbility(Char enemy) {
        if (buff(Bleeding.class) != null) {
            Buff.affect(enemy, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding.class).set(Math.round(damageRoll() * 0.3f));
        }
        if (buff(Crippling.class) != null) {
            Buff.affect(enemy, Cripple.class, 2f);
        }
        HalfZatoichi b = buff(HalfZatoichi.class);
        if (b != null) b.hitLanded = true;
    }

    private void blinkTowardEnemy() {
        if (enemy == null) return;
        if(this.state == SLEEPING) return; // duh

        Ballistica route = new Ballistica(pos, enemy.pos, Ballistica.PROJECTILE);
        int steps = Math.min(6, route.dist);
        int cell = route.path.get(Math.max(0, steps - 1));

        if (Actor.findChar(cell) != null && cell != pos) {
            cell = route.path.get(Math.max(0, steps - 2));
        }

        if (Dungeon.level.passable[cell] && Actor.findChar(cell) == null) {
            ScrollOfTeleportation.appear(this, cell);
        }
    }

    private MoodData getMoodData(){ // basically gets a bunch of values that are taken into account for picking a certain weapon
        float dist = Dungeon.level.distance(pos, enemy.pos);
        float HPFrac = (float) HP/HT;
        float enemyHPFrac = (float) enemy.HP/enemy.HT;
        boolean enemyChilled = enemy.buff(Chill.class) != null;
        boolean hasLineOfSight = new Ballistica(pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos;
        boolean nearHazard = false;
        for(IntMap.Entry<Trap> t : Dungeon.level.traps){
            if(Dungeon.level.distance(t.key, enemy.pos) <= 3){
                nearHazard = true;
                break;
            }
        }
        int crowded = 0;
        for (Mob m : Dungeon.level.mobs) {
            if (m != this && Dungeon.level.distance(m.pos, enemy.pos) <= 2 && m.alignment != Alignment.ENEMY) crowded++;
            if (m != this && Dungeon.level.distance(m.pos, enemy.pos) <= 2 && m.alignment == Alignment.ENEMY) crowded--;
        }

        return new MoodData(dist, HPFrac, enemyHPFrac, enemyChilled, hasLineOfSight, nearHazard, crowded);


    }


    public static class MoodData {
        public final float dist;
        public final float HPFrac;
        public final float enemyHPFrac;
        public final boolean enemyChilled;
        public final boolean hasLineOfSight;
        public final boolean nearHazard;
        public final int crowded;

        public MoodData(float dist, float HPFrac, float enemyHPFrac,
                        boolean enemyChilled, boolean hasLineOfSight, boolean nearHazard, int crowded) {
            this.dist = dist;
            this.HPFrac = HPFrac;
            this.enemyHPFrac = enemyHPFrac;
            this.enemyChilled = enemyChilled;
            this.hasLineOfSight = hasLineOfSight;
            this.nearHazard = nearHazard;
            this.crowded = crowded;
        }
    }

    private int calculateWandScores(){
        MoodData data = getMoodData();
        int[] candidates = new int[5];
        Arrays.fill(candidates, 1);
        candidates[MAGIC_MISSILE_WAND]++; // solid generalist pick

        if (data.crowded >= 2) {
            candidates[CORROSION_WAND]  += 3; // aoe denial
            candidates[LIGHTNING_WAND]  += 1; // also hits multiple targets
        }

        if(data.dist > 6){
            candidates[CORROSION_WAND] += 1; // great far away!
        }
        else{
            candidates[CORROSION_WAND] -= 2; // terrible close by...
        }

        if (!data.enemyChilled) {
            candidates[FROST_WAND] += 2; // good opener, no value once already applied
        }

        if (data.dist <= 2) {
            candidates[BLAST_WAVE_WAND] += 2; // only useful on something close enough to push
            if (data.nearHazard) candidates[BLAST_WAVE_WAND] += 2; // even better with something to push them into
        } else {
            candidates[BLAST_WAVE_WAND] = Math.max(0, candidates[BLAST_WAVE_WAND] - 1); // nothing to push at range
        }

        float[] weights = new float[5];
        for (int i = 0; i < 5; i++) weights[i] = candidates[i];
        return Random.chances(weights);
    }

    private int calculateWeaponScores(){
        MoodData data = getMoodData();
        int[] candidates = new int[9];
        Arrays.fill(candidates, 1);
        candidates[4] ++; // greatsword is a good weapon in general

        if(data.HPFrac > 0.8){
            candidates[0]+= 2; // good opening weapon
        }
        if(data.HPFrac <= 0.3){
            candidates[5] += 2; // good for defense later on
            candidates[8] += 2; // the katana's ability heals
        }

        candidates[0] += (int) (3 - (data.dist)/2); // good for close quarters

        if(data.dist >= 6){
            candidates[7] += 2; // whip is really good far away
            candidates[6] += 2; // assassin's blade blink allows the imp to get much closer
        }

        candidates[2] += (int) ((data.dist) / 2 - 3); // charge attack is really good far away, allows the imp to close distance.

        if(data.enemyHPFrac >= 0.7){
            candidates[1] += 2; // scythe is good at lowering HP
        }
        if(data.enemyHPFrac <= 0.3){
            candidates[3] += 2; // gloves to finish player off
        }

        float[] weights = new float[9];
        for (int i = 0; i < 9; i++) {
            weights[i] = candidates[i];
        }
        return Random.chances(weights)+14; // shift the index back too
    }

    private int evaluateMoodSwap(){
        if (turnsSinceMoodSwap <= 5) return mood;
        MoodData data = getMoodData();
        int[] candidates = new int[2]; // 0 = wands, 1 = melee
        Arrays.fill(candidates, 1);

        if(data.nearHazard){
            candidates[0]+=2;
        }
        else{
            candidates[1]+=2;
        }
        if(data.dist >= 6){
            candidates[0]+=2;
        }
        else{
            candidates[1]+=2;
        }
        if(data.enemyChilled){
            candidates[1]++;
        }
        else{
            candidates[0]++;
        }
        if(data.HPFrac > 0.5){
            candidates[1]++;
        }
        else{
            candidates[0]++;
        }
        if(data.dist >= 6){
            candidates[0]+=3;
        }
        else{
            candidates[1]+=3;
        }

        float[] weights = new float[2];
        for (int i = 0; i < 2; i++) {
            weights[i] = candidates[i];
        }
        int result = Random.chances(weights);
        if(mood != result){
            turnsSinceMoodSwap = 0;
            turnsSinceWeaponSwap = 0;
            return result;
        }
        return mood;
    }

}
