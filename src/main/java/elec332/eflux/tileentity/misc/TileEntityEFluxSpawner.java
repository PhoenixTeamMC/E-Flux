package elec332.eflux.tileentity.misc;

import elec332.core.api.annotations.RegisterTile;
import elec332.core.tile.TileBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * Created by Elec332 on 20-2-2016.
 */
@RegisterTile(name = "TileEntityEFluxSpawner")
public class TileEntityEFluxSpawner extends TileBase {

    public TileEntityEFluxSpawner(){
        mobSpawner = new EFluxMobSpawner();
    }

    private boolean hasRedstone, ignorePlayer, brainDead;
    private final MobSpawnerBaseLogic mobSpawner;
    private boolean redstoneActivated;

    @Override
    public void update() {
        mobSpawner.updateSpawner();
    }

    public void readFromOldSpawner(NBTTagCompound tag){
        mobSpawner.readFromNBT(tag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        hasRedstone = tagCompound.getBoolean("Hr");
        ignorePlayer = tagCompound.getBoolean("ignoreP");
        brainDead = tagCompound.getBoolean("brainDead");
        redstoneActivated = tagCompound.getBoolean("rA");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("Hr", hasRedstone);
        tagCompound.setBoolean("ignoreP", ignorePlayer);
        tagCompound.setBoolean("brainDead", brainDead);
        tagCompound.setBoolean("rA", redstoneActivated);
    }

    @Override
    public void onNeighborTileChange(BlockPos neighbor) {
        super.onNeighborTileChange(neighbor);
        redstoneActivated = worldObj.isBlockPowered(pos);
    }

    private class EFluxMobSpawner extends MobSpawnerBaseLogic {

        @Override
        public void func_98267_a(int id) {
            //???
        }

        @Override
        public World getSpawnerWorld() {
            return getWorld();
        }

        @Override
        public BlockPos getSpawnerPosition() {
            return getPos();
        }

        @Override
        public boolean isActivated() {
            return redstoneAllowed() && playerAllowed();
        }

        @Override
        public Entity spawnNewEntity(Entity entityIn, boolean spawn) {
            Entity ret = super.spawnNewEntity(entityIn, spawn);
            if (brainDead && (ret instanceof EntityLiving)){
                EntityLiving entity = (EntityLiving) ret;
                while (entity != null){
                    makeBrainDead(entity);
                    entity = entity.riddenByEntity instanceof EntityLiving ? (EntityLiving) entity.riddenByEntity : null;
                }
            }
            return ret;
        }

        private boolean redstoneAllowed(){
            return !hasRedstone || redstoneActivated;
        }

        private boolean playerAllowed(){
            return ignorePlayer || super.isActivated();
        }

    }

    private void makeBrainDead(EntityLiving entity){
        entity.tasks.taskEntries.clear();
        entity.targetTasks.taskEntries.clear();
        entity.getNavigator().clearPathEntity();
        entity.setCanPickUpLoot(false);
    }

}