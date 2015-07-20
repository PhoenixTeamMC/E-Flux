package elec332.eflux.tileentity.energy.machine.chunkLoader;

import com.google.common.collect.Lists;
import elec332.core.main.ElecCore;
import elec332.core.player.PlayerHelper;
import elec332.core.server.ServerHelper;
import elec332.core.util.BlockLoc;
import elec332.core.util.IRunOnce;
import elec332.core.world.WorldHelper;
import elec332.eflux.EFlux;
import elec332.eflux.handler.ChunkLoaderPlayerProperties;
import elec332.eflux.tileentity.BreakableReceiverTile;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by Elec332 on 25-5-2015.
 */
public class MainChunkLoaderTile extends BreakableReceiverTile {

    public MainChunkLoaderTile(){
        repairItems = Lists.newArrayList(new ItemStack(Items.ender_eye), new ItemStack(Items.ender_pearl));
        active = true;
        this.changed = true;
        this.tickets = Lists.newArrayList();
    }

    private List<ItemStack> repairItems;
    private UUID owner;
    private boolean active;
    private int neededPower;
    public boolean changed;

    private List<ForgeChunkManager.Ticket> tickets;

    private List<BlockLoc> getLocations(){
        return ChunkLoaderPlayerProperties.get(owner).getLocations();
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        calculatePower();
        //if (storedPower >= neededPower && active){
        //    storedPower -= neededPower;
            handle(true);
        //} else handle(false);
    }

    private void calculatePower(){
        this.neededPower = 3; //TODO: Dynamic, per-chunk cost
    }

    public void addLoader(ChunkLoaderSubTile tile){
        getLocations().add(tile.myLocation());
        changed = true;
    }

    public void removeLoader(ChunkLoaderSubTile tile){
        getLocations().remove(tile.myLocation());
        changed = true;
    }

    private void handle(boolean b){
        if ((changed || !b) && !worldObj.isRemote){
            for (ForgeChunkManager.Ticket ticket : tickets){
                ForgeChunkManager.releaseTicket(ticket);
            }
            tickets.clear();
            if (b) {
                for (BlockLoc loc : getLocations()) {
                    ForgeChunkManager.Ticket ticket = WorldHelper.requestTicket(worldObj, loc, EFlux.instance);
                    tickets.add(ticket);
                    WorldHelper.forceChunk(ticket);
                    PlayerHelper.sendMessageToPlayer(worldObj.func_152378_a(owner), "Put ticket on blockLoc: " + loc.toString());
                }
            }
            changed = false;
        }
    }

    @Override
    public void writeToItemStack(NBTTagCompound tagCompound) {
        super.writeToItemStack(tagCompound);
        if (owner != null)
            tagCompound.setString("Owner", owner.toString());
    }

    @Override
    public void readItemStackNBT(NBTTagCompound tagCompound) {
        super.readItemStackNBT(tagCompound);
        this.owner = UUID.fromString(tagCompound.getString("Owner"));
    }

    @Override
    public ItemStack getRandomRepairItem() {
        Collections.shuffle(repairItems);
        return repairItems.get(0);
    }

    public boolean canPlace(EntityPlayer player){
        return (owner == null || isOwner(player)) && !ChunkLoaderPlayerProperties.get(PlayerHelper.getPlayerUUID(player)).hasHandler();
    }

    @Override
    public void onBlockPlacedBy(final EntityLivingBase entityLiving, ItemStack stack) {
        super.onBlockPlacedBy(entityLiving, stack);
        ElecCore.tickHandler.registerCall(new IRunOnce() {
            @Override
            public void run() {
                if (!ServerHelper.isServer(worldObj))
                    return;
                if (entityLiving instanceof EntityPlayer && !ChunkLoaderPlayerProperties.get(PlayerHelper.getPlayerUUID((EntityPlayer) entityLiving)).hasHandler()) {
                    if (MainChunkLoaderTile.this.owner == null)
                        MainChunkLoaderTile.this.owner = PlayerHelper.getPlayerUUID((EntityPlayer) entityLiving);
                    PlayerHelper.sendMessageToPlayer((EntityPlayer) entityLiving, "Placed chunkloader at " + myLocation().toString());
                    ChunkLoaderPlayerProperties.get(PlayerHelper.getPlayerUUID((EntityPlayer) entityLiving)).setMainChunkLoader(MainChunkLoaderTile.this);
                    MainChunkLoaderTile.this.getLocations().add(myLocation());
                    MainChunkLoaderTile.this.changed = true;
                }
            }
        });
    }

    public boolean isOwner(EntityPlayer player){
        return player != null && PlayerHelper.getPlayerUUID(player).equals(owner);
    }

    @Override
    public boolean onBlockActivated(EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        return isOwner(player) && super.onBlockActivated(player, side, hitX, hitY, hitZ);
    }

    @Override
    public float getAcceptance() {
        return 0.5f;
    }

    @Override
    public int getEFForOptimalRP() {
        return 0;
    }

    @Override
    protected int getMaxStoredPower() {
        return 0;
    }

    @Override
    public void onBlockRemoved() {
        super.onBlockRemoved();
        handle(false);
        if (owner != null) {
            getLocations().remove(myLocation());
            ChunkLoaderPlayerProperties.get(owner).setMainChunkLoader(null);
        }
    }

    /**
     * @param direction the direction from which a connection is requested
     * @return weather the tile can connect and accept power from the given side
     */
    @Override
    public boolean canAcceptEnergyFrom(ForgeDirection direction) {
        return true;
    }

    /**
     * @param direction The requested direction
     * @return The Redstone Potential at which the machine wishes to operate
     */
    @Override
    public int requestedRP(ForgeDirection direction) {
        return 33;
    }

    @Override
    public String[] getProvidedData() {
        return new String[0];
    }
}