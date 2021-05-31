package net.kunmc.lab.walkvoid.listener;

import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.BlockTileEntity;
import net.minecraft.server.v1_16_R3.Blocks;
import net.minecraft.server.v1_16_R3.GeneratorAccess;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.World;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.block.CapturedBlockState;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_16_R3.block.data.CraftBlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.lang.reflect.Field;

public class WalkListener implements Listener {
    @EventHandler
    public void on(PlayerMoveEvent e) {
        if (e.getFrom().getBlockX() != e.getTo().getBlockX() || e.getFrom().getBlockZ() != e.getTo().getBlockZ()) {
            for (int i = e.getFrom().getBlockY(); i > -1; i--) {
                setTypeAndData((CraftBlock) e.getFrom().getWorld().getBlockAt(e.getFrom().getBlockX(), i, e.getFrom().getBlockZ()), ((CraftBlockData) org.bukkit.Material.AIR.createBlockData()).getState(), false);
            }
        }
    }

    public static boolean setTypeAndDataWithoutLight(World world, BlockPosition blockposition, IBlockData iblockdata, int i, int j) {
        if (world.captureTreeGeneration) {
            CapturedBlockState blockstate = (CapturedBlockState)world.capturedBlockStates.get(blockposition);
            if (blockstate == null) {
                blockstate = CapturedBlockState.getTreeBlockState(world, blockposition, i);
                world.capturedBlockStates.put(blockposition.immutableCopy(), blockstate);
            }

            blockstate.setData(iblockdata);
            return true;
        } else if (world.isOutsideWorld(blockposition)) {
            return false;
        } else {
            net.minecraft.server.v1_16_R3.Chunk chunk = world.getChunkAtWorldCoords(blockposition);
            net.minecraft.server.v1_16_R3.Block block = iblockdata.getBlock();
            boolean captured = false;
            if (world.captureBlockStates && !world.capturedBlockStates.containsKey(blockposition)) {
                CapturedBlockState blockstate = CapturedBlockState.getBlockState(world, blockposition, i);
                world.capturedBlockStates.put(blockposition.immutableCopy(), blockstate);
                captured = true;
            }

            IBlockData iblockdata1 = chunk.setType(blockposition, iblockdata, (i & 64) != 0, (i & 1024) == 0);
            if (iblockdata1 == null) {
                if (world.captureBlockStates && captured) {
                    world.capturedBlockStates.remove(blockposition);
                }

                return false;
            } else {
                IBlockData iblockdata2 = world.getType(blockposition);
                if (!world.captureBlockStates) {
                    try {
                        world.notifyAndUpdatePhysics(blockposition, chunk, iblockdata1, iblockdata, iblockdata2, i, j);
                    } catch (StackOverflowError var10) {
                        world.lastPhysicsProblem = new BlockPosition(blockposition);
                        throw var10;
                    }
                }
                return true;
            }
        }
    }

    public static boolean setTypeAndData(CraftBlock block, IBlockData blockData, boolean applyPhysics) {
        GeneratorAccess world = null;
        try {
            Field fworld = CraftBlock.class.getDeclaredField("world");
            fworld.setAccessible(true);
            world= (GeneratorAccess) fworld.get(block);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        if (!blockData.isAir() && blockData.getBlock() instanceof BlockTileEntity && blockData.getBlock() != block.getNMS().getBlock()) {
            if (block.getWorld() instanceof net.minecraft.server.v1_16_R3.World) {
                ((net.minecraft.server.v1_16_R3.World) block.getWorld()).removeTileEntity(block.getPosition());
            } else {
                //block.getWorld().setTypeAndData(block.getPosition(), Blocks.AIR.getBlockData(), 0);
                setTypeAndDataWithoutLight((World) block.getWorld(), block.getPosition(), Blocks.AIR.getBlockData(),0, 0);
            }
        }

        if (applyPhysics) {
            return setTypeAndDataWithoutLight((World) block.getWorld(),block.getPosition(),blockData, 3, 0);
        } else {
            IBlockData old = world.getType(block.getPosition());
            //boolean success = world.setTypeAndData(block.getPosition(), blockData, 1042);
            boolean success = setTypeAndDataWithoutLight((World) world, block.getPosition(),blockData,1042, 0);
            if (success) {
                world.getMinecraftWorld().notify(block.getPosition(), old, blockData, 3);
            }

            return success;
        }
    }
}
