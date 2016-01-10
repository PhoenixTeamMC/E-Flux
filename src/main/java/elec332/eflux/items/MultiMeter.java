package elec332.eflux.items;

import elec332.core.api.wrench.IRightClickCancel;
import elec332.core.util.RegisterHelper;
import elec332.core.world.WorldHelper;
import elec332.eflux.EFlux;
import elec332.eflux.api.util.IMultiMeterDataProvider;
import elec332.eflux.api.util.IMultiMeterDataProviderMultiLine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/**
 * Created by Elec332 on 5-4-2015.
 */
public class MultiMeter extends Item implements IRightClickCancel {
    public MultiMeter(String name) {
        setCreativeTab(EFlux.creativeTab);
        setUnlocalizedName(EFlux.ModID + "." + name);
        //setTextureName(EFlux.ModID + ":" + name);
        setContainerItem(this);
        setMaxStackSize(1);
        RegisterHelper.registerItem(this, name);
    }

    public boolean doesContainerItemLeaveCraftingGrid(ItemStack itemStack) {
        return false;
    }

    public ItemStack getContainerItem(ItemStack itemStack) {
        return new ItemStack(this, 1, itemStack.getItemDamage() + 1);
    }

    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float HitX, float HitY, float HitZ) {
        TileEntity tileEntity = WorldHelper.getTileAt(world, pos);
        if (!world.isRemote) {
            if (tileEntity instanceof IMultiMeterDataProvider)
                player.addChatComponentMessage(new ChatComponentText(((IMultiMeterDataProvider) tileEntity).getProvidedData()));
            if (tileEntity instanceof IMultiMeterDataProviderMultiLine)
                for (String s : ((IMultiMeterDataProviderMultiLine) tileEntity).getProvidedData())
                    player.addChatComponentMessage(new ChatComponentText(s));
            //TODO: more provided info
            return true;
        }
        return false;
    }
}
