package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Succubus;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.CrystalKey;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.watabou.utils.Point;

// Doesn't generate rn, maybe we'll add it back in the future
public class CrystalJailRoom extends SpecialRoom {

    @Override
    public int minHeight() { return 4; }
	@Override
    public int minWidth() { return 4; }

    @Override
    public void paint(Level level) {
        Painter.fill( level, this, Terrain.WALL );
        Door door = entrance();
        door.set(Door.Type.REGULAR);

        Point chest1Pos = center();
        Point chest2Pos = center();
        Point enemyPos = center();
		
        if (door.x == left) {
            enemyPos.x = right - 1;
            chest1Pos.set(enemyPos.x, enemyPos.y - 1);
            chest2Pos.set(enemyPos.x, enemyPos.y + 1);
            Painter.set(level, right - 2, enemyPos.y, Terrain.CRYSTAL_DOOR);
            Painter.fill(level, left + 1, top + 1, width() - 4, height() - 2, Terrain.EMPTY);
            Painter.fill(level, enemyPos.x, enemyPos.y - 1, 1, 3, Terrain.EMPTY_SP);
        } else if (door.x == right) {
            enemyPos.x = left + 1;
            chest1Pos.set(enemyPos.x, enemyPos.y - 1);
            chest2Pos.set(enemyPos.x, enemyPos.y + 1);
            Painter.set(level, left + 2, enemyPos.y, Terrain.CRYSTAL_DOOR);
            Painter.fill(level, right - 1, top + 1, width() - 4, height() - 2, Terrain.EMPTY);
            Painter.fill(level, enemyPos.x, enemyPos.y - 1, 1, 3, Terrain.EMPTY_SP);
        } else if (door.y == top) {
            enemyPos.y = bottom - 1;
            chest1Pos.set(enemyPos.x - 1, enemyPos.y);
            chest2Pos.set(enemyPos.x + 1, enemyPos.y);
            Painter.set(level, enemyPos.x, bottom - 2, Terrain.CRYSTAL_DOOR);
            Painter.fill(level, left + 1, top + 1, width() - 2, height() - 4, Terrain.EMPTY);
            Painter.fill(level, enemyPos.x - 1, enemyPos.y, 3, 1, Terrain.EMPTY_SP);
        } else if (door.y == bottom) {
            enemyPos.y = top + 1;
            chest1Pos.set(enemyPos.x - 1, enemyPos.y);
            chest2Pos.set(enemyPos.x + 1, enemyPos.y);
            Painter.set(level, enemyPos.x, top + 2, Terrain.CRYSTAL_DOOR);
            Painter.fill(level, left + 1, bottom - 1, width() - 2, height() - 4, Terrain.EMPTY);
            Painter.fill(level, enemyPos.x - 1, enemyPos.y, 3, 1, Terrain.EMPTY_SP);
        }
        
        level.drop(new Gold(25), level.pointToCell(chest1Pos)).type = Heap.Type.CHEST;
        level.drop(new Gold(25), level.pointToCell(chest2Pos)).type = Heap.Type.CHEST;
        Succubus succubus = new Succubus();
        succubus.pos = level.pointToCell(enemyPos);
        level.mobs.add(succubus);
        level.addItemToSpawn( new CrystalKey( Dungeon.depth ) );
    }
}
