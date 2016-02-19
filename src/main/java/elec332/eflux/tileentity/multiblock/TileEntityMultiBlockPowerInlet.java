package elec332.eflux.tileentity.multiblock;

import elec332.core.api.annotations.RegisterTile;
import elec332.eflux.api.EFluxAPI;
import elec332.eflux.api.energy.EnergyAPIHelper;
import elec332.eflux.api.energy.IEnergyReceiver;
import elec332.eflux.api.util.IMultiMeterDataProviderMultiLine;
import elec332.eflux.multiblock.EFluxMultiBlockMachine;
import elec332.eflux.multiblock.MultiBlockInterfaces;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

/**
 * Created by Elec332 on 28-7-2015.
 */
@RegisterTile(name = "TileEntityEFluxMultiBlockPowerInlet")
public class TileEntityMultiBlockPowerInlet extends TileMultiBlockInteraction<MultiBlockInterfaces.IEFluxMultiBlockPowerAcceptor> implements IEnergyReceiver, IMultiMeterDataProviderMultiLine {

    @Override
    public void onTileUnloaded() {
        if (!worldObj.isRemote) {
            EnergyAPIHelper.postUnloadEvent(this);
        }
    }

    @Override
    public void onTileLoaded() {
        if (!worldObj.isRemote) {
            EnergyAPIHelper.postLoadEvent(this);
        }
    }

    int latsRP, lastEF;


    /**
     * @return The Redstone Potential at which the machine wishes to operate
     */
    @Override
    public int requestedRP() {
        return getMultiBlockHandler() == null ? 0 : getMultiBlockHandler().requestedRP();
    }

    /**
     * @param rp        The Redstone Potential in the network
     * @return The amount of EnergeticFlux requested for the Redstone Potential in the network
     */
    @Override
    public int getRequestedEF(int rp) {
        return getMultiBlockHandler() == null ? 0 : getMultiBlockHandler().getRequestedEF(rp);
    }

    /**
     * @param rp        the RedstonePotential in the network
     * @param ef        the amount of EnergeticFlux that is being provided
     * @return The amount of EnergeticFlux that wasn't used
     */
    @Override
    public int receivePower(int rp, int ef) {
        lastEF = ef;
        latsRP = rp;
        return getMultiBlockHandler() == null ? ef : getMultiBlockHandler().receivePower(rp, ef);
    }

    @Override
    public String[] getProvidedData() {
        String[] ret = new String[]{
                "Power: "+((EFluxMultiBlockMachine)getMultiBlock()).getStoredPower(),
                "Broken: "+((EFluxMultiBlockMachine)getMultiBlock()).isBroken(),
                "RP: "+latsRP,
                "EF: "+lastEF
        };
        latsRP = 0;
        lastEF = 0;
        return ret;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return (capability == EFluxAPI.RECEIVER_CAPABILITY && facing == getTileFacing()) || super.hasCapability(capability, facing);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        return capability == EFluxAPI.RECEIVER_CAPABILITY ? (facing == getTileFacing() ? (T)this : null) : super.getCapability(capability, facing);
    }

}
