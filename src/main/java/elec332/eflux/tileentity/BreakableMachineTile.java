package elec332.eflux.tileentity;

import elec332.core.compat.handlers.WailaCompatHandler;
import elec332.core.server.ServerHelper;
import elec332.eflux.EFlux;
import elec332.eflux.api.EFluxAPI;
import elec332.eflux.api.energy.IEnergyReceiver;
import elec332.eflux.api.energy.container.EnergyContainer;
import elec332.eflux.api.energy.container.IEFluxPowerHandler;
import elec332.eflux.api.util.IBreakableMachine;
import elec332.eflux.api.util.IMultiMeterDataProviderMultiLine;
import elec332.eflux.util.BreakableMachineInventory;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.SpecialChars;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import java.util.List;

/**
 * Created by Elec332 on 1-5-2015.
 */
public abstract class BreakableMachineTile extends EnergyTileBase implements IEnergyReceiver, IMultiMeterDataProviderMultiLine, IBreakableMachine, WailaCompatHandler.IWailaInfoTile, IEFluxPowerHandler {

    public BreakableMachineTile(){
        super();
        this.energyContainer = new EnergyContainer(getMaxStoredPower(), this, this);
    }

    private boolean broken = false;
    protected EnergyContainer energyContainer;

    public abstract ItemStack getRandomRepairItem();

    protected abstract int getMaxStoredPower();

    @Override
    public int getOptimalRP() {
        return getRequestedRP();
    }

    public abstract int getRequestedRP();

    @Override
    public void onBroken(){
        if (!worldObj.isRemote) {
            breakableMachineInventory = new BreakableMachineInventory(this, getRandomRepairItem());
            for (EntityPlayerMP playerMP : ServerHelper.instance.getAllPlayersWatchingBlock(worldObj, pos)){
                playerMP.connection.sendPacket(getUpdatePacket());
            }
        }
    }

    @Override
    public void markObjectDirty() {
        markDirty();
    }

    @Override
    public boolean isBroken() {
        return broken;
    }

    private BreakableMachineInventory breakableMachineInventory;

    public BreakableMachineInventory getBreakableMachineInventory() {
        return breakableMachineInventory;
    }

    @Override
    public void setBroken(boolean broken) {
        if (!broken)
            this.breakableMachineInventory = null;
        this.broken = broken;
        notifyNeighborsOfChange();
        syncData();
    }

    @Override
    public boolean onBlockActivated(IBlockState state, EntityPlayer player, EnumHand hand, ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (broken) return openBrokenGui(player);
        return onBlockActivatedSafe(player, side, hitX, hitY, hitZ);
    }

    public boolean onBlockActivatedSafe(EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ){
        return false;
    }

    private boolean openBrokenGui(EntityPlayer player){
        player.openGui(EFlux.instance, 1, worldObj, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    @Override
    public int getRequestedEF(int rp) {
        return energyContainer.getRequestedEF(rp);
    }

    @Override
    public int receivePower(int rp, int ef) {
        return energyContainer.receivePower(rp, ef);
    }

    @Override
    public int requestedRP() {
        return energyContainer.requestedRP();
    }

    @Override
    public void readItemStackNBT(NBTTagCompound tagCompound) {
        super.readItemStackNBT(tagCompound);
        energyContainer.readFromNBT(tagCompound);
        this.broken = tagCompound.getBoolean("broken");
        if (broken) {
            this.breakableMachineInventory = new BreakableMachineInventory(this, ItemStack.loadItemStackFromNBT(tagCompound.getCompoundTag("itemRep")));
        }
    }

    @Override
    public void writeToItemStack(NBTTagCompound tagCompound) {
        super.writeToItemStack(tagCompound);
        energyContainer.writeToNBT(tagCompound);
        tagCompound.setBoolean("broken", broken);
        if (broken) {
            NBTTagCompound newTag = new NBTTagCompound();
            breakableMachineInventory.getRepairItem().writeToNBT(newTag);
            tagCompound.setTag("itemRep", newTag);
        }
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currentTip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        NBTTagCompound tag = accessor.getNBTData();
        if (tag != null){
            currentTip.add("Energy: "+tag.getInteger("energy")+"/"+tag.getInteger("maxEnergy"));
            if (tag.getBoolean("broken")){
                currentTip.add(SpecialChars.ALIGNCENTER+SpecialChars.ITALIC+"Broken");
            }
        }
        return currentTip;
    }

    @Override
    public NBTTagCompound getWailaTag(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
        tag.setInteger("energy", energyContainer.getStoredPower());
        tag.setInteger("maxEnergy", energyContainer.getMaxStoredEnergy());
        tag.setBoolean("broken", broken);
        return tag;
    }

    @Override
    public String[] getProvidedData() {
        return new String[]{
            "energy: "+energyContainer.getStoredPower(),
                "maxEnergy: "+energyContainer.getMaxStoredEnergy(),
                "broken: "+broken,
                "facing: "+getTileFacing()
        };
    }

    protected boolean canAcceptEnergyFrom(EnumFacing direction) {
        return true;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return (capability == EFluxAPI.RECEIVER_CAPABILITY && canAcceptEnergyFrom(facing)) || super.hasCapability(capability, facing);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        return capability == EFluxAPI.RECEIVER_CAPABILITY ? (canAcceptEnergyFrom(facing) ? (T)this : null) : super.getCapability(capability, facing);
    }

}
