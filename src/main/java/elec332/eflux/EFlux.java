package elec332.eflux;

import com.google.common.collect.Lists;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import elec332.core.config.ConfigWrapper;
import elec332.core.helper.MCModInfo;
import elec332.core.modBaseUtils.ModInfo;
import elec332.core.multiblock.MultiBlockRegistry;
import elec332.core.network.NetworkHandler;
import elec332.core.server.ServerHelper;
import elec332.core.util.EventHelper;
import elec332.eflux.compat.Compat;
import elec332.eflux.compat.rf.RFCompat;
import elec332.eflux.grid.power.EventHandler;
import elec332.eflux.handler.ChunkLoaderPlayerProperties;
import elec332.eflux.handler.PlayerEventHandler;
import elec332.eflux.init.BlockRegister;
import elec332.eflux.init.CommandRegister;
import elec332.eflux.init.ItemRegister;
import elec332.eflux.init.MultiBlockRegister;
import elec332.eflux.items.circuits.CircuitHandler;
import elec332.eflux.proxies.CommonProxy;
import elec332.eflux.recipes.RecipeRegistry;
import elec332.eflux.util.CalculationHelper;
import elec332.eflux.util.Config;
import elec332.eflux.util.EnumMachines;
import elec332.eflux.util.RecipeItemStack;
import elec332.eflux.world.WorldGenOres;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * Created by Elec332 on 24-2-2015.
 */
@Mod(modid = EFlux.ModID, name = EFlux.ModName, dependencies = ModInfo.DEPENDENCIES+"@[#ELECCORE_VER#,)",
        acceptedMinecraftVersions = ModInfo.ACCEPTEDMCVERSIONS, useMetadata = true, canBeDeactivated = true)
public class EFlux {

    public static final String ModName = "E-Flux";
    public static final String ModID = "EFlux";
    public static File baseFolder;

    @SidedProxy(clientSide = "elec332.eflux.proxies.ClientProxy", serverSide = "elec332.eflux.proxies.CommonProxy")
    public static CommonProxy proxy;

    @Mod.Instance(ModID)
    public static EFlux instance;
    public static Configuration config;
    public static CreativeTabs creativeTab;
    public static Logger logger;
    public static ConfigWrapper configWrapper;
    public static Random random;
    public static NetworkHandler networkHandler;
    public static MultiBlockRegistry multiBlockRegistry;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        baseFolder = new File(event.getModConfigurationDirectory(), "E-Flux");
        config = new Configuration(new File(baseFolder, "EFlux.cfg"));
        creativeTab = new CreativeTabs("EFlux") {
            @Override
            public Item getTabIconItem() {
                return Item.getItemFromBlock(Blocks.anvil);  //TODO: replace with mod item, once we got a nice one
            }
        };
        logger = event.getModLog();
        configWrapper = new ConfigWrapper(config);
        random = new Random();
        networkHandler = new NetworkHandler(ModID);
        multiBlockRegistry = new MultiBlockRegistry();

        //DEBUG///////////////////
        logger.info(new RecipeItemStack(Items.iron_ingot).setStackSize(3).equals(new RecipeItemStack("ingotIron").setStackSize(2)));

        logger.info(CalculationHelper.calcRequestedEF(23, 20, 40, 1000, 0.15f));
        logger.info(CalculationHelper.calcRequestedEF(17, 20, 40, 1000, 0.15f));
        logger.info(CalculationHelper.calcRequestedEF(16, 20, 40, 1000, 0.15f));
        logger.info(CalculationHelper.calcRequestedEF(24, 20, 40, 1000, 0.15f));
        logger.info(Math.sqrt(Math.abs(Math.cos(10))));
        /////////////////////////

        GameRegistry.registerItem(new Item(){
            @Override
            public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
                return multiBlockRegistry.getStructureRegistry().attemptCreate(world, x, y, z, ForgeDirection.getOrientation(side));
            }
        }.setCreativeTab(creativeTab), "itemTestMB");


        //setting up mod stuff
        configWrapper.registerConfig(new Config());
        configWrapper.refresh();
        Compat.instance.loadList();
        Compat.instance.addHandler(new RFCompat());
        logger.info("RF API loaded: "+Compat.RF);
        logger.info("RFTools: "+Compat.RFTools);


        MCModInfo.CreateMCModInfo(event, "Created by Elec332",
                "E-Flux",
                "website link", "logo",
                new String[]{"Elec332"});
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) throws IOException{
        ServerHelper.instance.registerExtendedProperties("EFluxChunks", ChunkLoaderPlayerProperties.class);
        ItemRegister.instance.init(event);
        BlockRegister.instance.init(event);
        new WorldGenOres(new File(baseFolder, "Ores.cfg")).register();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
        MultiBlockRegister.init();
        configWrapper.refresh();
        EventHelper.registerHandler(EventHelper.Handler.BOTH, new EventHandler());
        CircuitHandler.register();
        registerRecipes();
        EventHelper.registerHandlerForge(new PlayerEventHandler());
        ForgeChunkManager.setForcedChunkLoadingCallback(instance, new ForgeChunkManager.LoadingCallback() {
            @Override
            public void ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world) {
                //Dummy, just load my chunks please.....
            }
        });
        //register items/blocks

    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event){
        //Nope
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        CommandRegister.instance.init(event);
    }

    public static void systemPrintDebug(Object s){
        if (Config.DebugLog)
            System.out.println(s);
    }

    private void registerRecipes(){
        RecipeRegistry.instance.registerRecipe(EnumMachines.COMPRESSOR, "ingotIron", new ItemStack(Items.dye, 3, 5));
        RecipeRegistry.instance.registerRecipe(EnumMachines.COMPRESSOR, Lists.newArrayList(new RecipeItemStack("gemDiamond"), new RecipeItemStack(Items.beef)), new ItemStack(Items.experience_bottle, 6));
    }
}
