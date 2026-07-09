package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.badlogic.gdx.graphics.Pixmap;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.glwrap.Blending;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.utils.PointF;

public class LightDonut extends Image {

    // so i used to make it on the fly, but that was fucked as hell so screw it, fixed texture. Annoyingly, this looks better lol

    // so the texture is made and baked in this script actually, so these parameters will affect the ring
    private static final int REF_OUTER_RADIUS = 64; // outer radius
    private static final float REF_THICKNESS_RATIO = 0.6f; // thickness as a fraction of outer radius
    private static final int BLOCK_SIZE = 3;  // granularity of the baked ring.
    private static final int NUM_BANDS  = 6; // number of color bands. 6 looks kinda nice?

    private static SmartTexture ringTexture;

    private final float startOuterRadius;
    private final float growthRate;
    private final float lifetime;
    private float age = 0;

    public LightDonut( float worldX, float worldY, float startOuterRadius,
                       float growthRate, float lifetime ) {
        super( getRingTexture() );

        this.startOuterRadius = startOuterRadius;
        this.growthRate = growthRate;
        this.lifetime = lifetime;


        originToCenter();
        x = worldX - width/2;
        y = worldY - height/2;

        updateScale();
    }

    // create the texture
    private static SmartTexture getRingTexture() {
        if (ringTexture != null) return ringTexture;

        int size2 = 1;
        while (size2 < REF_OUTER_RADIUS * 2) size2 <<= 1;

        ringTexture = TextureCache.create("LightDonutRing", size2, size2);
        Pixmap p = ringTexture.bitmap;
        p.setBlending(Pixmap.Blending.None);
        p.setColor(0x00000000);
        p.fill();

        float cx = size2 / 2f;
        float cy = size2 / 2f;
        float outer = REF_OUTER_RADIUS;
        float thickness = outer * REF_THICKNESS_RATIO;
        float inner = outer - thickness;
        float mid = (inner + outer) / 2f;
        float halfThickness = thickness / 2f;

        for (int py = 0; py < size2; py += BLOCK_SIZE) {
            for (int px = 0; px < size2; px += BLOCK_SIZE) {
                float dx = (px + BLOCK_SIZE/2f) - cx;
                float dy = (py + BLOCK_SIZE/2f) - cy;
                float r = (float)Math.sqrt(dx*dx + dy*dy);

                if (r < inner || r > outer) continue;

                float distFromMid = Math.abs(r - mid) / halfThickness;
                float falloff = 1f - distFromMid;

                int band = (int)(falloff * NUM_BANDS);
                band = Math.min(Math.max(band, 0), NUM_BANDS - 1);
                int alpha = (int)(255 * (band + 1) / (float)NUM_BANDS);
                int color = (0xFFFFFF << 8) | alpha;

                for (int by = py; by < py + BLOCK_SIZE && by < size2; by++) {
                    for (int bx = px; bx < px + BLOCK_SIZE && bx < size2; bx++) {
                        p.drawPixel(bx, by, color);
                    }
                }
            }
        }

        ringTexture.bitmap(p);
        return ringTexture;
    }

    private void updateScale() { // just scale that bish up
        float currentOuterRadius = startOuterRadius + growthRate * age;
        float s = currentOuterRadius / REF_OUTER_RADIUS;
        scale.set(s, s);
    }

    @Override
    public void update() {
        super.update();

        age += Game.elapsed;
        if (age >= lifetime) {
            killAndErase();
            return;
        }

        updateScale();
        alpha(Math.max(0, 1f - age / lifetime));
    }

    @Override
    public void draw() {
        Blending.setLightMode();
        super.draw();
        Blending.useDefault();
    }
}