package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.badlogic.gdx.graphics.Pixmap;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Stack;

public class ImpBossDeathFX extends Gizmo {

    private float worldX, worldY;
    private Image snapshot; // TODO I HATE THIS but ig it works...
    private TextureFilm impFrames; // TODO maybe rework the textures this shit not perfect imo
    private float elapsed = 0;
    private float timeSinceLastSpawn = 0;
    private float nextSpawnDelay = 0;
    private float magnitude = 1f;
    private boolean donutSpawned = false;
    private ArrayList<LightBurst> bursts = new ArrayList<>(); // this is kinda stupid imo but it works..?
    private int burstCount = 0;
    private LightDonut donut;

    private final int[] BREAK_FRAMES = { 5, 6, 7, 8, 9, 10, 11, 12 };
    private final Stack<Integer> nextBreakFrame = new Stack<>();
    private final float TOTAL_DURATION   = 7f;// maybe quantity based instead of time based? Idk
    private final float START_INTERVAL   = 1f; // gap between bursts at the start
    private final float END_INTERVAL     = 0.1f; // at the end

    private final float lightDonutLifetime = 7f;
    private int breakFrameIndex = 0;

    public ImpBossDeathFX( float worldX, float worldY, Image snapshot, TextureFilm impFrames) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.snapshot = snapshot;
        this.impFrames = impFrames;

        nextBreakFrame.push(11);
        nextBreakFrame.push(10);
        nextBreakFrame.push(9);
        nextBreakFrame.push(8);
        nextBreakFrame.push(7);
        nextBreakFrame.push(5);
        nextBreakFrame.push(3);
        nextBreakFrame.push(1);
    }

    @Override
    public void update() {
        super.update();
        elapsed += Game.elapsed;

        if (!donutSpawned) {
            timeSinceLastSpawn += Game.elapsed;

            if (elapsed >= TOTAL_DURATION) {
                for (LightBurst burst : bursts) burst.killAndErase();
                bursts.clear();

                donut = new LightDonut( worldX, worldY, 10f, 30f, lightDonutLifetime );
                parent.add(donut);

                GameScene.flash( 0xFFFFFF, true, 4 );

                donutSpawned = true;
                return;
            }

            if (timeSinceLastSpawn >= nextSpawnDelay) {
                spawnBurst();
                burstCount++;
                if (!nextBreakFrame.isEmpty() && burstCount >= nextBreakFrame.peek()) { // basically if it's time for the next frame, we break
                    snapshot.frame(impFrames.get(BREAK_FRAMES[breakFrameIndex]));
                    breakFrameIndex++;
                    nextBreakFrame.pop(); // and then pop
                }
                timeSinceLastSpawn = 0;
                magnitude += 0.2f;


                float progress = elapsed / TOTAL_DURATION;
                nextSpawnDelay = START_INTERVAL + (END_INTERVAL - START_INTERVAL) * progress;
            }
        } else {
            snapshot.alpha(Math.max(0, 1f - (elapsed - TOTAL_DURATION) / lightDonutLifetime)); // lerp, lasts as long as the circle, maybe change?

            if (donut.parent == null) {
                snapshot.killAndErase();
                killAndErase();
            }
        }
    }

    private void spawnBurst() { // spawns the light bursts
        PixelScene.shake(magnitude, 0.4f);
        LightBurst burst = new LightBurst( worldX, worldY, 100, 1000f );
        bursts.add(burst);
        parent.addToBack(burst);
        spawnChips();
    }


    private static SmartTexture chipTexture;

    private static SmartTexture getChipTexture() {
        if (chipTexture == null) {
            chipTexture = TextureCache.create("ImpChipPixel", 1, 1);
            Pixmap px = chipTexture.bitmap;
            px.setColor(0xFFFFFFFF);
            px.fill();
            chipTexture.bitmap(px);
        }
        return chipTexture;
    }

    private void spawnChips() {
        for (int i = 0; i < 3; i++) {
            ImpChip chip = new ImpChip(getChipTexture());
            chip.scale.set(2, 2);
            chip.color(0xFF0000);//this is a redish tint
            chip.x = worldX + Random.Float(-4, 4);
            chip.y = worldY + Random.Float(-4, 4);
            chip.speed.set(Random.Float(-20, 20), Random.Float(-50, -15)); // pop up
            chip.acc.set(0, 160); // gravity back down
            parent.add(chip);
        }
    }

    private static class ImpChip extends Image {
        private float age = 0;
        private static final float LIFETIME = 0.6f;

        ImpChip(SmartTexture tex) {
            super(tex);
        }

        @Override
        public void update() {
            super.update();
            age += Game.elapsed;
            alpha(Math.max(0, 1f - age / LIFETIME));
            if (age >= LIFETIME){
                killAndErase();
            }
        }
    }

}