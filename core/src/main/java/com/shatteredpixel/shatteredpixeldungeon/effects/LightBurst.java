package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.badlogic.gdx.graphics.Pixmap;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.gltextures.TextureCache;
import com.watabou.glwrap.Blending;
import com.watabou.noosa.Image;
import com.watabou.utils.Random;

public class LightBurst extends Image {
    private static final int BLOCK_SIZE = 2; // so this determines the size of the blocks, i didnt really like the super HD effect I was getting
    private static final int NUM_BANDS  = 8; // similarly, discrete brightness steps

    private int pWidth, pHeight, width2, height2;
    private float lifetime;
    private float age = 0;

    public LightBurst( float worldX, float worldY, int radiusPixels, float lifetime ) {
        super();

        this.lifetime = lifetime;

        pWidth = radiusPixels * 2;
        pHeight = radiusPixels * 2;

        width2 = 1; while (width2 < pWidth) width2 <<= 1;
        height2 = 1; while (height2 < pHeight) height2 <<= 1;

        String key = "LightBurst" + System.nanoTime(); // todo maybe stupid system
        texture(TextureCache.create(key, width2, height2));

        width = width2;
        height = height2;

        //maybe im going crazy but .centerToOrigin() is FUCKED. I genuinely cannot use it, it was hell for the spinning wheel and was hell here too.
        x = worldX - width2 / 2f;
        y = worldY - height2 / 2f;

        //this was originally a placeholder but fuck me im lazy
        paintHyperbola( Random.Float(0, 6.28f), 1500, Random.Int(5,8), 40, Random.Int(0,40) );
    }



    //the math methods below were written by AI :P
    private static float[] toRayFrame( float px, float py, float cx, float cy, float angleRad ) {
        float dx = px - cx;
        float dy = py - cy;
        float cos = (float)Math.cos(-angleRad);
        float sin = (float)Math.sin(-angleRad);
        float u = dx * cos - dy * sin;
        float v = dx * sin + dy * cos;
        return new float[]{u, v};
    }


    public void paintHyperbola( float angleRad, float length, float minWidth, float maxWidth, float curvature ) {
        Pixmap p = texture.bitmap;
        p.setBlending(Pixmap.Blending.None);

        float cx = width2 / 2f;
        float cy = height2 / 2f;

        //k is curvature expressed relative to the ray's length (0..1 range için t)
        float k = curvature / length;
        //shape(t) = sqrt(t^2 + k^2) - k, which is 0 at t=0 and grows roughly linearly for t >> k
        //normalize so shape(1) == 1, so we can lerp cleanly between minWidth and maxWidth
        float shapeAtEnd = (float)Math.sqrt(1f + k*k) - k;

        for (int by = 0; by < height2; by += BLOCK_SIZE) {
            for (int bx = 0; bx < width2; bx += BLOCK_SIZE) {

                float[] uv = toRayFrame(bx + BLOCK_SIZE/2f, by + BLOCK_SIZE/2f, cx, cy, angleRad);
                float u = uv[0], v = uv[1];

                if (u < 4 || u > length) continue; // give him a bit of space so it's not too much on top of him

                float t = u / length; // 0 at tip, 1 at far end
                float shape = ((float)Math.sqrt(t*t + k*k) - k) / shapeAtEnd; // 0..1

                float wLimit = minWidth + (maxWidth - minWidth) * shape;
                if (wLimit <= 0) continue;

                float dist = Math.abs(v) / wLimit;
                if (dist > 1f) continue;

                float lengthFade = 1f - t;
                float falloff = (1f - dist) * lengthFade;

                paintBand(p, bx, by, falloff);
            }
        }

        texture.bitmap(p);
    }

    //shared block-fill + banding, factored out of paintEllipse so all three shapes stay consistent
    private void paintBand( Pixmap p, int bx, int by, float falloff ) {
        int band = (int)(falloff * NUM_BANDS)-2;
        band = Math.min(Math.max(band, 0), NUM_BANDS - 1);
        int alpha = (int)(255 * (band + 1) / (float)NUM_BANDS);
        int color = (0xFFFFFF << 8) | alpha;

        for (int py = by; py < by + BLOCK_SIZE && py < height2; py++) {
            for (int px = bx; px < bx + BLOCK_SIZE && px < width2; px++) {
                p.drawPixel(px, py, color);
            }
        }
    }

    @Override
    public void update() {
        super.update();
        age += com.watabou.noosa.Game.elapsed;
        alpha(Math.max(0, 1f - age / lifetime)); // self explanatory
        if (age >= lifetime) {
            killAndErase();
        }
    }

    @Override
    public void draw() {
        Blending.setLightMode();
        super.draw();
        Blending.useDefault();
    }
}