package elec332.eflux.tileentity.energy.machine;

import elec332.eflux.tileentity.TileEntityProcessingMachineSingleSlot;
import elec332.eflux.util.EnumMachines;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Created by Elec332 on 10-5-2015.
 */
public class TileGrinder extends TileEntityProcessingMachineSingleSlot {

    @Override
    public int getRequiredPowerPerTick() {
        return 400;
    }

    @Override
    protected int getMaxStoredPower() {
        return 9000;
    }

    @Override
    protected int getProcessTime() {
        return 20;
    }

    @Override
    public ItemStack getRandomRepairItem() {
        return null;
    }

    @Override
    public float getAcceptance() {
        return 1.0f;
    }

    @Override
    public int getEFForOptimalRP() {
        return 40;
    }

    /**
     * @param direction The requested direction
     * @return The Redstone Potential at which the machine wishes to operate
     */
    @Override
    public int requestedRP(ForgeDirection direction) {
        return 3;
    }

    @Override
    public Object getGuiClient(EntityPlayer player) {
        return basicGui(player);
    }

    @Override
    public EnumMachines getMachine() {
        return EnumMachines.GRINDER;
    }
}