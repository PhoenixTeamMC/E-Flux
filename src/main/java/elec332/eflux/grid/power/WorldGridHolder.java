package elec332.eflux.grid.power;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import elec332.eflux.EFlux;
import elec332.eflux.api.energy.IEnergyReceiver;
import elec332.eflux.api.energy.IEnergySource;
import elec332.eflux.api.energy.IEnergyTile;
import elec332.eflux.api.energy.IPowerTransmitter;
import elec332.core.util.BlockLoc;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.*;

/**
 * Created by Elec332 on 23-4-2015.
 */
public class WorldGridHolder {

    public WorldGridHolder(World world){
        this.world = world;
        this.grids = new ArrayList<EFluxCableGrid>();
        this.registeredTiles = new HashMap<BlockLoc, PowerTile>();
        this.pending = new ArrayDeque<PowerTile>();
        //this.pendingRemovals = new ArrayList<PowerTile>();
        this.oldInt = 0;
        FMLCommonHandler.instance().bus().register(this);
    }

    private World world;  //Dunno why I have this here (yet)
    private List<EFluxCableGrid> grids;
    private Queue<PowerTile> pending;
    //private List<PowerTile> pendingRemovals;
    private Map<BlockLoc, PowerTile> registeredTiles;
    private int oldInt;

    public EFluxCableGrid registerGrid(EFluxCableGrid grid){
        this.grids.add(grid);
        return grid;
    }

    protected void removeGrid(EFluxCableGrid grid){
        grids.remove(grid);
    }

    public void addTile(IEnergyTile tile){
        TileEntity theTile = (TileEntity) tile;
        PowerTile powerTile = new PowerTile(theTile);
        registeredTiles.put(genCoords(theTile), powerTile);
        addTile(powerTile);
        EFlux.logger.info("Tile placed at "+genCoords(theTile).toString());
    }

    public void addTile(PowerTile powerTile){
        if(!world.isRemote) {
            EFlux.logger.info("Processing tile at "+powerTile.getLocation().toString());
            TileEntity theTile = powerTile.getTile();
            for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
                EFlux.logger.info("Processing tile at "+powerTile.getLocation().toString() + " for side "+direction.toString());
                TileEntity possTile = world.getTileEntity(theTile.xCoord + direction.offsetX, theTile.yCoord + direction.offsetY, theTile.zCoord + direction.offsetZ);
                if (possTile != null && possTile instanceof IEnergyTile) {
                    PowerTile powerTile1 = getPowerTile(genCoords(possTile));
                    if (powerTile1 == null || !powerTile1.hasInit()) {
                        pending.add(powerTile);
                        break;
                    }
                    if (canConnect(powerTile, direction, powerTile1)) {
                        EFluxCableGrid grid = powerTile1.getGridFromSide(direction.getOpposite());
                        powerTile.getGridFromSide(direction).mergeGrids(grid);
                    }
                } else {
                    EFlux.logger.info("There is no tile at side "+direction.toString() + " that is valid for connection");
                }
            }
        }
    }

    private boolean canConnect(PowerTile powerTile1, ForgeDirection direction, PowerTile powerTile2){
        TileEntity mainTile = powerTile1.getTile();
        boolean flag1 = false;
        boolean flag2 = false;
        if (powerTile1.getConnectType() == powerTile2.getConnectType() && (powerTile1.getConnectType() == PowerTile.ConnectType.SEND || powerTile1.getConnectType() == PowerTile.ConnectType.RECEIVE))
            return false; //We don't want to receivers or 2 sources connecting, do we?
        if (mainTile instanceof IPowerTransmitter){
            return canConnectFromSide(direction.getOpposite(), powerTile2);
        } else {
            if (powerTile1.getConnectType() == PowerTile.ConnectType.SEND_RECEIVE){
                if (((IEnergySource) mainTile).canProvidePowerTo(direction))
                    flag1 = canConnectFromSide(direction.getOpposite(), powerTile2);
                if (((IEnergyReceiver)mainTile).canAcceptEnergyFrom(direction))
                    flag2 = canConnectFromSide(direction.getOpposite(), powerTile2);
                return flag1 || flag2;
            } else if (powerTile1.getConnectType() == PowerTile.ConnectType.RECEIVE){
                if (((IEnergyReceiver) mainTile).canAcceptEnergyFrom(direction))
                    return canConnectFromSide(direction.getOpposite(), powerTile2);
            } else if (powerTile1.getConnectType() == PowerTile.ConnectType.SEND){
                if (((IEnergySource) mainTile).canProvidePowerTo(direction))
                    return canConnectFromSide(direction.getOpposite(), powerTile2);
            }
            return false;
        }
    }

    private boolean canConnectFromSide(ForgeDirection direction, PowerTile powerTile2){
        TileEntity secondTile = powerTile2.getTile();
        boolean flag1 = false;
        boolean flag2 = false;
        if (secondTile instanceof IPowerTransmitter)
            return true;
        if (secondTile instanceof IEnergyReceiver)
            flag1 = ((IEnergyReceiver) secondTile).canAcceptEnergyFrom(direction);
        if (secondTile instanceof IEnergySource)
            flag2 = ((IEnergySource)secondTile).canProvidePowerTo(direction);
        return flag1 || flag2;
    }

    public void removeTile(IEnergyTile tile){
        /*PowerTile powerTile = ;
        powerTile.toGo = 3;
        pendingRemovals.add(powerTile);*/
        removeTile(getPowerTile(genCoords((TileEntity) tile)));
    }

    public void removeTile(PowerTile powerTile){
        if (powerTile != null) {
            for (EFluxCableGrid grid : powerTile.getGrids()) {
                if (grid != null) {
                    EFlux.logger.info("Removing tile at " + powerTile.getLocation().toString());
                    List<BlockLoc> vec3List = new ArrayList<BlockLoc>();
                    vec3List.addAll(grid.getLocations());
                    vec3List.remove(powerTile.getLocation());
                    EFlux.logger.info(registeredTiles.keySet().size());
                    registeredTiles.remove(powerTile.getLocation());
                    EFlux.logger.info(registeredTiles.keySet().size());
                    this.grids.remove(grid);
                    List<BlockLoc> vec3List2 = new ArrayList<BlockLoc>();
                    for (BlockLoc vec : vec3List) {
                        if (!vec.equals(powerTile.getLocation())) {
                            PowerTile pt = getPowerTile(vec);
                            if (pt != null) {
                                pt.resetGrid(grid);
                                vec3List2.add(vec);
                            }
                        }
                    }
                    for (BlockLoc vec : vec3List2) {
                        EFlux.logger.info("Re-adding tile at " + vec.toString());
                        TileEntity tileEntity1 = getTile(vec);
                        if (tileEntity1 instanceof IEnergyTile)
                            if (getPowerTile(vec) != null)
                                addTile(getPowerTile(vec));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event){
        onServerTickInternal(event);
    }

    protected void onServerTickInternal(TickEvent event){
        if (event.phase == TickEvent.Phase.START) {
            EFlux.logger.info("Tick! "+world.provider.dimensionId);
            if (!pending.isEmpty()) {
                /*List<PowerTile> tr = new ArrayList<PowerTile>();
                for (PowerTile powerTile : pending)
                    tr.add(powerTile);
                for (PowerTile powerTile : tr)
                    addTile(powerTile);
                pending.removeAll(tr);
                EFlux.logger.info("TickStuffPendingDone");*/
                for (PowerTile powerTile : pending){
                    addTile(powerTile);
                }
                pending.clear();
            }
            /*if (!pendingRemovals.isEmpty()){
                List<PowerTile> tr = new ArrayList<PowerTile>();
                for (PowerTile powerTile : pendingRemovals){
                    powerTile.toGo--;
                    if (getTile(powerTile.getLocation()) == null){
                        powerTile.toGo = 0;
                        if (!tr.contains(powerTile))
                            removeTile(powerTile);
                    }
                    if (powerTile.toGo <= 0)
                        tr.add(powerTile);
                }
                pendingRemovals.removeAll(tr);
            }
            this.oldInt = pending.size();*/
            for (int i = 0; i < grids.size(); i++){
                try {
                    grids.get(i).onTick();
                } catch (Throwable t){
                    throw new RuntimeException(t);
                }
                EFlux.logger.info(i);
            }
        }
    }

    public PowerTile getPowerTile(BlockLoc loc){
        return registeredTiles.get(loc);
    }

    private BlockLoc genCoords(TileEntity tileEntity){
        return new BlockLoc(tileEntity);
    }

    private TileEntity getTile(BlockLoc vec){
        return world.getTileEntity(vec.xCoord, vec.yCoord, vec.zCoord);
    }
}