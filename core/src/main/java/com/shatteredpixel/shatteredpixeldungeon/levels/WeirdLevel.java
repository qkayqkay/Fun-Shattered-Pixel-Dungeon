package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Cookie;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.SegmentedRoom;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

public class WeirdLevel extends Level {

    private static int SIZE = 60;

    @Override
	public String tilesTex() {
		return Assets.Environment.TILES_PRISON;
	}
	
	@Override
	public String waterTex() {
		return Assets.Environment.WATER_HALLS;
	}

    @Override
    protected boolean build() {
        setSize(SIZE, SIZE);
        SegmentedRoom room = new SegmentedRoom();
        room.set(0, 0, SIZE - 1, SIZE - 1);
        room.paint(this);
        buildFlagMaps();

        int center = pointToCell(room.center());
        for (int n : PathFinder.NEIGHBOURS9) {
            if (passable[center + n]) {
                center = center + n;
                break;
            }
        }

        transitions.add(new LevelTransition(this, center, LevelTransition.Type.REGULAR_EXIT));
        return true;
    }

    @Override
    protected void createMobs() {
    }

    @Override
    protected void createItems() {

        int bonusItemPos;
        switch (Random.Int(4)) {
            case 0:
                bonusItemPos = pointToCell(new Point(1, 1));
                break;
            case 1:
                bonusItemPos = pointToCell(new Point(1, SIZE - 2));
                break;
            case 2:
                bonusItemPos = pointToCell(new Point(SIZE - 2, SIZE - 2));
                break;
            default:
                bonusItemPos = pointToCell(new Point(SIZE - 2, 1));
                break;
        }
        drop(new Cookie(), bonusItemPos);
    }
}