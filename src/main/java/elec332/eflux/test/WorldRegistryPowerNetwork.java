package elec332.eflux.test;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import elec332.core.player.PlayerHelper;
import elec332.eflux.EFlux;
import elec332.eflux.api.event.TransmitterLoadedEvent;
import elec332.eflux.api.transmitter.IEnergyReceiver;
import elec332.eflux.api.transmitter.IEnergySource;
import elec332.eflux.api.transmitter.IEnergyTile;
import elec332.eflux.api.transmitter.IPowerTransmitter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by Elec332 on 16-4-2015.
 */
public class WorldRegistryPowerNetwork {

    private static Map<World, WorldRegistryPowerNetwork> mappings = new WeakHashMap<World, WorldRegistryPowerNetwork>();
    private static List<World> registeredWorlds = new ArrayList<World>();

    public static WorldRegistryPowerNetwork get(World world){
        if (world == null)
            throw new IllegalArgumentException();
        WorldRegistryPowerNetwork ret = mappings.get(world);
        if (ret == null){
            ret = new WorldRegistryPowerNetwork(world);
            mappings.put(world, ret);
        }
        return ret;
    }
    //////////////////////////////////////////////////////

    private WorldRegistryPowerNetwork(World world){
        this.world = world;
        this.grids = new WeakHashMap<EFluxCableGrid, Object>();
        //FMLCommonHandler.instance().bus().register(this);  Didn't work, dunno why
        registeredWorlds.add(world);
        EFlux.logger.info("Created new WorldHandler");
    }

    private World world;  //Dunno why I have this here (yet)
    //private List<EFluxCableGrid> grids;
    private WeakHashMap<EFluxCableGrid, Object> grids;

    public EFluxCableGrid genNewPowerGrid(IEnergyTile base){
        EFluxCableGrid grid = new EFluxCableGrid(world);
        grid.addToGrid(base, genCoords((TileEntity)base));
        grids.put(grid, null);
        return grid;
    }

    protected void removeGrid(EFluxCableGrid grid){
        grids.remove(grid);
    }

    public void addTile(IEnergyTile tile){
        if(!world.isRemote){
        TileEntity theTile = ((TileEntity) tile);
        Vec3 tileCoords = genCoords(theTile);
        boolean hasDoneSomething = false;
        try {
            for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
                TileEntity possTile = world.getTileEntity(theTile.xCoord + direction.offsetX, theTile.yCoord + direction.offsetY, theTile.zCoord + direction.offsetZ);

                if (possTile != null && possTile instanceof IEnergyTile) {
                    Vec3 possTileLoc = genCoords(possTile);
                    if (possTile instanceof IPowerTransmitter){
                        EFluxCableGrid tileGrid = getFirstGrid(tileCoords);
                        EFluxCableGrid possGrid = getFirstGrid(possTileLoc);
                        if (tileGrid == null && possGrid == null) {
                            genNewPowerGrid(tile).addToGrid((IEnergyTile) possTile, possTileLoc);
                            sm("Both null");
                        } else if (tileGrid == null) {
                            possGrid.addToGrid(tile, tileCoords);
                            sm("Tile null");
                        } else if (possGrid == null) {
                            tileGrid.addToGrid((IEnergyTile) possTile, possTileLoc);
                            sm("poss null");
                        } else {
                            possGrid.mergeGrids(tileGrid);
                            sm("else");
                        }
                        EFlux.logger.info("Placed and added to grid");
                    } else
                    if (possTile instanceof IEnergyReceiver && ((IEnergyReceiver) possTile).canAcceptEnergyFrom(direction.getOpposite())) {
                        genNewPowerGrid(tile).addToGrid((IEnergyTile) possTile, possTileLoc);
                        EFlux.logger.info("Placed and added to grid");
                    /*Vec3 loc = genCoords(possTile);
                    for (EFluxCableGrid grid : grids.keySet()){
                        if (grid.canAddReceiver(loc))
                            grid.addToGrid(tile, genCoords(theTile));
                    }*/
                    } else
                    if (possTile instanceof IEnergySource && ((IEnergySource) possTile).canProvidePowerTo(direction.getOpposite())) {
                        EFluxCableGrid tileGrid = getFirstGrid(tileCoords);
                        EFluxCableGrid possGrid = getFirstGrid(possTileLoc);
                        if (tileGrid == null && possGrid == null) {
                            genNewPowerGrid(tile).addToGrid((IEnergyTile) possTile, possTileLoc);
                        } else if (tileGrid == null) {
                            possGrid.addToGrid(tile, tileCoords);
                        } else if (possGrid == null) {
                            tileGrid.addToGrid((IEnergyTile) possTile, possTileLoc);
                        } else possGrid.mergeGrids(tileGrid);
                        EFlux.logger.info("Placed and added to grid");
                    /*Vec3 loc = genCoords(possTile);
                    for (EFluxCableGrid grid : grids.keySet()){
                        if (grid.canAddProvider(loc))
                            grid.addToGrid(tile, genCoords(theTile));
                    }*/
                    }
                    hasDoneSomething = true;
                }
            }
        } catch (Throwable t){
            System.out.println("ERR");
        }

        if (!hasDoneSomething) {
            genNewPowerGrid(tile);
            EFlux.logger.info("Placed and added to grid _ NVT");
        }
        /*
        for (EFluxCableGrid grid : grids.keySet()){
            if (grid.addToGrid(tile))
                break;
        }*/}
    }

    public void sm(String s){
        ((EntityPlayer) world.playerEntities.get(0)).addChatMessage(new ChatComponentText(s));
    }

    public void removeTile(IEnergyTile tile){
        EFluxCableGrid[] grids = getGrid(genCoords((TileEntity) tile));
        if (grids == null || grids.length == 0)
            return;
        for (EFluxCableGrid grid : grids) {
            List<Vec3> vec3List = new ArrayList<Vec3>();
            vec3List.addAll(grid.getLocations());
            this.grids.remove(grid);
            for (Vec3 vec : vec3List) {
                TileEntity tileEntity1 = getTile(vec);
                if (tileEntity1 instanceof IEnergyTile)
                    MinecraftForge.EVENT_BUS.post(new TransmitterLoadedEvent((IEnergyTile) tileEntity1));
            }
        }
    }

    private EFluxCableGrid[] getGrid(Vec3 vec){
        List<EFluxCableGrid> ret = new ArrayList<EFluxCableGrid>();
        if (vec != null){
            for (EFluxCableGrid grid : grids.keySet()){
                if (grid.hasTile(vec))
                    ret.add(grid);
            }
        }
        return ret.toArray(new EFluxCableGrid[ret.size()]);
    }

    private EFluxCableGrid getFirstGrid(Vec3 vec){
        //if (vec != null){
            for (EFluxCableGrid grid : grids.keySet()){
                if (grid.hasTile(vec))
                    return grid;
            }
        //}
        return null;
    }

    protected void onServerTickInternal(TickEvent event){
        if (event.phase == TickEvent.Phase.START) {
            EFlux.logger.info("Tick!");
            int i = 0;
            for (EFluxCableGrid grid : grids.keySet()) {
                i++;
                grid.onTick();
                EFlux.logger.info(i);
            }
        }
    }


    private Vec3 genCoords(TileEntity tileEntity){
        return Vec3.createVectorHelper(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
    }

    private TileEntity getTile(Vec3 vec){
        return world.getTileEntity((int)vec.xCoord, (int)vec.yCoord, (int)vec.zCoord);
    }

    /*public static void onServerTick(TickEvent.ServerTickEvent event){
        for (World world : registeredWorlds){
            get(world).onServerTickInternal(event);
        }
    }*/
}
