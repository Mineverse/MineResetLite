package dev.sprock.minereset;

import com.koletar.jj.mineresetlite.types.Mine;
import com.koletar.jj.mineresetlite.MineResetLitePlugin;
import org.bukkit.World;
import org.bukkit.block.Block;

public class MineResetAPI
{
    public static Mine getMine(World world, int x, int y, int z)
    {
        //if I have to
        for (Mine mine : MineResetLitePlugin.instance.getMines()) {
            if (mine.getWorld() == world && mine.isInRegion(x, y, z))
            {
                return mine;
            }
        }
        return null;
    }

    public static Mine getMine(Block block)
    {
        return getMine(block.getWorld(), block.getX(), block.getY(), block.getZ());
    }
}
