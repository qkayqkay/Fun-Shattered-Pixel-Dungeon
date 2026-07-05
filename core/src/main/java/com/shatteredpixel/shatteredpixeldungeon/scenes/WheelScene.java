package com.shatteredpixel.shatteredpixeldungeon.scenes;


import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.items.Ankh;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ExitButton;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.gltextures.TextureCache;
import com.watabou.glwrap.Blending;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.*;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;
import com.watabou.utils.RectF;

public class WheelScene extends PixelScene {
    private SkinnedBlock water;
    Image wheel;

    private static final int NUM_CELLS = 7;
    private static final float SECTOR_ANGLE = 360f / NUM_CELLS;
    private static final float POINTER_OFFSET = -10f;
    private float spindownConstant = 0.985f; // we rig this value hehehehe


    private float resultDisplayTimer = 0f;
    private static final float RESULT_DISPLAY_TIME = 1.0f; //seconds to show the result before allowing another spin

    boolean spinning = false;
    boolean spunUp = false;
    private boolean doneSpinning = false;

    int timesGambled = 0;
    private float angularVel = 0;
    private float targetAngularVel = 0;
    private int targetCell; // yeah that's right this wheel is rigged, cry about it lmao

    {
        inGameScene = true;
    }

    @Override
    public void create() {
       super.create();

        RectF insets = getCommonInsets();
        int w = (int) (Camera.main.width - insets.left + insets.right);
        int h = (int) (Camera.main.height - insets.top + insets.bottom);


        water = new SkinnedBlock( // copied over from alchemy scene, just a nicer background
                w, h,
                Dungeon.level.waterTex() ){

            @Override
            protected NoosaScript script() {
                return NoosaScriptNoLighting.get();
            }

            @Override
            public void draw() {
                //water has no alpha component, this improves performance
                Blending.disable();
                super.draw();
                Blending.enable();
            }
        };
        water.autoAdjust = true;
        add(water);

        wheel = new Image( TextureCache.get( Assets.Interfaces.WHEEL ) );
        Image pointer = new Image( TextureCache.get( Assets.Interfaces.POINTER ) );

        wheel.originToCenter();
        wheel.x = wheel.width()/4+10f;
        wheel.y = 0;
        add(wheel);

        pointer.x = (w-pointer.width)/2 - 5 + wheel.width/2;
        pointer.y = (h-pointer.height)/2;
        add( pointer );

        PointerArea wheelHitbox = new PointerArea(0, 0, w, h) {
            @Override
            protected void onClick( PointerEvent event ) {
                if (!spinning) {
                    spinning = true;
                    spinWheel();
                }
            }
        };
        add( wheelHitbox );

        ExitButton btnExit = new ExitButton(){
            @Override
            protected void onClick() {
                if(timesGambled<3){

                }
                Game.switchScene(GameScene.class);

            }
        };
        btnExit.setPos( insets.left + w - btnExit.width(), insets.top );
        add( btnExit );

    }


    public static class WndConfirmCancel extends WndOptions {

        public WndConfirmCancel(){
            super(new ItemSprite(new ScrollOfEnchantment()),
                    Messages.titleCase(Messages.get(WheelScene.class, "cancel_title")),
                    Messages.get(WheelScene.class, "cancel_warn"),
                    Messages.get(WheelScene.class, "cancel_warn_yes"),
                    Messages.get(WheelScene.class, "cancel_warn_no"));
        }

        @Override
        protected void onSelect(int index) {
            super.onSelect(index);
            if (index == 1) {
                Game.switchScene(GameScene.class);
            }
        }

        @Override
        public void onBackPressed() {
            //do nothing
        }
    }

    @Override
    protected void onBackPressed() { // i hate that this is a thing in the first place because sometimes I misclick
    }

    private int pickTargetCell() {
        float roll = Random.Float();
        if (roll <= 0.14f) return 0;
        else if (roll <= 0.40f) return 1;
        else if (roll <= 0.48f) return 2;
        else if (roll <= 0.79f) return 3;
        else if (roll <= 0.791f) return 4;
        else if (roll <= 0.85f) return 5;
        else if (roll <= 1) return 6;
        System.out.println("ARGH WHAT?? Roll was: "+roll);
        return -1;
    }

    private void spinWheel(){
        spinning = true;
        this.targetCell = pickTargetCell();

        float v = angularVel;
        float spinupAngle = 0f;
        while (v < 20f) {
            v += 0.1f;
            spinupAngle += v;
        }
        float v0 = v;

        float targetAngle = targetCell * SECTOR_ANGLE + SECTOR_ANGLE / 2f + POINTER_OFFSET + Random.Float(-7, 7);

        int extraSpins = 2;
        float projected = (wheel.angle + spinupAngle) % 360f;
        float delta = ((targetAngle - projected) % 360f + 360f) % 360f;
        float distanceNeeded = delta + extraSpins * 360f;

        spindownConstant = 1f - (v0 / distanceNeeded);

        targetAngularVel = 20f;
    }

    private void runCellWinLogic(){ // prob worst method name ive ever made
        doneSpinning = true;
        if(targetCell == 0) {
            Buff.affect( Dungeon.hero, Poison.class).set(4 + Dungeon.hero.lvl/2f);
            Sample.INSTANCE.play( Assets.Sounds.DEBUFF, 1, 1, Random.Float( 0.9f, 1.1f ) );
        } else if (targetCell == 1) {
            Dungeon.gold += 100;
            Statistics.goldCollected += 100;
            Badges.validateGoldCollected();
            Sample.INSTANCE.play( Assets.Sounds.GOLD, 1, 1, Random.Float( 0.9f, 1.1f ) );
        } else if (targetCell == 2) {
            Dungeon.gold += 500;
            Statistics.goldCollected += 500;
            Badges.validateGoldCollected();
            Sample.INSTANCE.play( Assets.Sounds.GOLD, 1, 1, Random.Float( 0.9f, 1.1f ) );
        } else if (targetCell == 3) {
            Dungeon.gold *= 0.7f;
            Sample.INSTANCE.play( Assets.Sounds.DEBUFF, 1, 1, Random.Float( 0.9f, 1.1f ) );
        } else if (targetCell == 4) {
            Ankh ankh = new Ankh();
            ankh.collect();
            Sample.INSTANCE.play( Assets.Sounds.ITEM, 1, 1, Random.Float( 0.9f, 1.1f ) );
        } else if (targetCell == 5) {
            Dungeon.gold -= 500;
            Sample.INSTANCE.play( Assets.Sounds.DEBUFF, 1, 1, Random.Float( 0.9f, 1.1f ) );
        } else if (targetCell == 6) {
            Dungeon.gold += 1000;
            Statistics.goldCollected += 1000;
            Badges.validateGoldCollected();
            Sample.INSTANCE.play( Assets.Sounds.GOLD, 1, 1, Random.Float( 0.9f, 1.1f ) );
        }
    }

    private void resetWheel(){
        spinning = false;
        spunUp = false;
        doneSpinning = false;
        resultDisplayTimer = 0f;
        targetAngularVel = 0f;
        timesGambled++;
    }

    @Override
    public void update() {
        super.update();
        if(!spinning) {
            angularVel = 0.2f;
        }
        else{
            if(!spunUp){
                angularVel += 0.1f;
                if(angularVel >= targetAngularVel){
                    spunUp = true;
                    targetAngularVel = 0;
                }
            }
            else{
                angularVel *= spindownConstant;
                if(angularVel <= 0.02) angularVel = 0;
                if(angularVel == 0 && !doneSpinning) {
                    runCellWinLogic();
                }
            }
        }
        wheel.angle += angularVel;

        if (doneSpinning) {
            resultDisplayTimer += Game.elapsed;
            if (resultDisplayTimer >= RESULT_DISPLAY_TIME) {
                resetWheel();
            }
        }
    }
}
