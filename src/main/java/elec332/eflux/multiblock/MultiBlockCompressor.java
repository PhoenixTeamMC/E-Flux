package elec332.eflux.multiblock;

import elec332.eflux.util.EnumMachines;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.List;

/**
 * Created by Elec332 on 27-8-2015.
 */
public class MultiBlockCompressor extends EFluxMultiBlockProcessingMachine {

    public MultiBlockCompressor() {
        super(3, 4);
    }

    @Override
    protected void registerMachineSlots(List<Slot> registerList) {
        registerList.add(new Slot(inventory, registerList.size(), 56, 17));
        registerList.add(new Slot(inventory, registerList.size(), 56, 53));
        oneOutPutSlot(registerList);
    }

    @Override
    public int getRequiredPowerPerTick() {
        return 50;
    }

    @Override
    public int getProcessTime() {
        return 70;
    }

    @Override
    protected int getMaxStoredPower() {
        return 2000;
    }

    @Override
    public ItemStack getRandomRepairItem() {
        return new ItemStack(Blocks.iron_block);
    }

    @Override
    public float getAcceptance() {
        return 0.34f;
    }

    @Override
    protected int getOptimalRP() {
        return 5;
    }

    @Override
    public int getEFForOptimalRP() {
        return 10;
    }

    @Override
    public EnumMachines getMachine() {
        return EnumMachines.COMPRESSOR;
    }

    @Override
    public ResourceLocation getBackgroundImageLocation() {
        return new ResourceLocation("textures/gui/container/furnace.png");
    }
}