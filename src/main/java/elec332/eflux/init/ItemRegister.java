package elec332.eflux.init;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import elec332.core.helper.RegisterHelper;
import elec332.core.main.ElecCore;
import elec332.eflux.items.*;
import net.minecraft.item.Item;

/**
 * Created by Elec332 on 24-2-2015.
 */
public class ItemRegister {
    public static final ItemRegister instance = new ItemRegister();
    private ItemRegister(){
    }

    public static Item wrench, multimeter, EFluxItems;

    public void init(FMLInitializationEvent event){
        if (ElecCore.developmentEnvironment)
            new Breaker();
        multimeter = new MultiMeter("MultiMeter");
        wrench = new Wrench("Wrench");
        Components.init();
        EFluxItems = new EFluxItems();
        RegisterHelper.registerItem(EFluxItems, "GenericItems");
    }
}
