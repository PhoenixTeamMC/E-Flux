package elec332.eflux.tileentity.energy.generator;

import elec332.core.api.annotations.RegisterTile;
import elec332.core.tile.TileBase;
import elec332.eflux.api.energy.IEnergySource;
import elec332.eflux.api.event.TransmitterLoadedEvent;
import elec332.eflux.api.event.TransmitterUnloadedEvent;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;

/**
 * Created by Elec332 on 29-4-2015.
 */
@RegisterTile(name = "TileEntityEFluxCoalGenerator")
public class CoalGenerator extends TileBase implements IEnergySource {

    /**
     * @param direction the direction from which a connection is requested
     * @return weather the tile can connect and provide power to the given side
     */
    @Override
    public boolean canProvidePowerTo(EnumFacing direction) {
        return direction != getTileFacing();
    }

    /**
     * @param rp        the RedstonePotential in the network
     * @param direction the direction where the power will be provided to
     * @param execute   weather the power is actually drawn from the tile,
     *                  this flag is always true for IEnergySource.
     * @return The amount of EnergeticFlux the tile can provide for the given Redstone Potential.
     */
    @Override
    public int provideEnergy(int rp, EnumFacing direction, boolean execute) {
        return 200; //getStackInSlot(0) != null?20:0;
    }

    @Override
    public void onTileLoaded() {
        if (!worldObj.isRemote)
            MinecraftForge.EVENT_BUS.post(new TransmitterLoadedEvent(this));
    }

    @Override
    public void onTileUnloaded() {
        if (!worldObj.isRemote)
            MinecraftForge.EVENT_BUS.post(new TransmitterUnloadedEvent(this));
    }
}
