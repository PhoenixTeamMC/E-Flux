package elec332.eflux.blocks;

import com.google.common.collect.Lists;
import elec332.core.client.IIconRegistrar;
import elec332.core.client.model.ElecModelBakery;
import elec332.core.client.model.ElecQuadBakery;
import elec332.core.client.model.INoJsonBlock;
import elec332.core.client.model.RenderingRegistry;
import elec332.core.client.model.map.BakedModelMetaMap;
import elec332.core.client.model.map.IBakedModelMetaMap;
import elec332.core.client.model.model.IBlockModel;
import elec332.core.client.model.template.ElecTemplateBakery;
import elec332.core.world.WorldHelper;
import elec332.eflux.api.energy.IEnergyReceiver;
import elec332.eflux.api.energy.IEnergySource;
import elec332.eflux.client.EFluxResourceLocation;
import elec332.eflux.client.render.CableRenderer;
import elec332.eflux.tileentity.energy.cable.AbstractCable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import elec332.core.client.RenderHelper;
import elec332.eflux.EFlux;
import elec332.eflux.tileentity.energy.cable.AdvancedCable;
import elec332.eflux.tileentity.energy.cable.BasicCable;
import elec332.eflux.tileentity.energy.cable.NormalCable;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * Created by Elec332 on 20-9-2015.
 */
public class BlockCable extends BlockWithMeta implements ITileEntityProvider, INoJsonBlock {

    public BlockCable(String blockName) {
        super(Material.cloth, blockName, EFlux.ModID);
        setCreativeTab(EFlux.creativeTab);
    }

    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite[] textures;
    @SideOnly(Side.CLIENT)
    private IBakedModelMetaMap<IBlockModel> models;

    @Override
    public BlockWithMeta register() {
        super.register();
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()){
            registerISBHR();
        }
        return this;
    }

    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getTextureFor(IBlockState state){
        return textures[getMetaFromState(state)];
    }

    @Override
    public int getRenderType() {
        return RenderingRegistry.SPECIAL_BLOCK_RENDERER_ID;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        switch (metadata){
            case 0:
                return new BasicCable();
            case 1:
                return new NormalCable();
            case 2:
                return new AdvancedCable();
            default:
                return null;
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
        setBlockBoundsBasedOnState(worldIn, pos);
        return super.getCollisionBoundingBox(worldIn, pos, state);
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox(World worldIn, BlockPos pos) {
        return super.getSelectedBoundingBox(worldIn, pos);
    }

    @Override
    @SuppressWarnings("all")
    public void setBlockBoundsBasedOnState(IBlockAccess iba, BlockPos pos) {
        List<EnumFacing> connections = Lists.newArrayList();
        TileEntity tile = WorldHelper.getTileAt(iba, pos);
        if (tile != null) {
            for (EnumFacing direction : EnumFacing.VALUES) {
                TileEntity tile2 = WorldHelper.getTileAt(iba, pos.offset(direction));
                if ((tile2 instanceof AbstractCable && ((AbstractCable) tile2).getUniqueIdentifier().equals(((AbstractCable) tile).getUniqueIdentifier())) || tile2 instanceof IEnergySource && ((IEnergySource) tile2).canProvidePowerTo(direction.getOpposite()) || tile2 instanceof IEnergyReceiver && ((IEnergyReceiver) tile2).canAcceptEnergyFrom(direction.getOpposite())) {
                    connections.add(direction);
                }
            }
        }
        float thickness = 6 * RenderHelper.renderUnit;
        float heightStuff = (1 - thickness)/2;
        float f1 = thickness + heightStuff;

        float yMin = connections.contains(EnumFacing.DOWN) ? 0 : heightStuff;
        float yMax = connections.contains(EnumFacing.UP) ? 1 : f1;
        float xMin = connections.contains(EnumFacing.WEST) ? 0 : heightStuff;
        float xMax = connections.contains(EnumFacing.EAST) ? 1 : f1;
        float zMin = connections.contains(EnumFacing.NORTH) ? 0 : heightStuff;
        float zMax = connections.contains(EnumFacing.SOUTH) ? 1 : f1;

        setBlockBounds(xMin, yMin, zMin, xMax, yMax, zMax);
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return false;
    }

    @Override
    public boolean isNormalCube() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean isFullBlock() {
        return false;
    }

    @Override
    public boolean isFullCube() {
        return false;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return getUnlocalizedName()+"."+stack.getItemDamage();
    }

    @Override
    public int getTypes() {
        return 3;
    }

    /**
     * Use this to register your textures.
     *
     * @param iconRegistrar The IIconRegistrar.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void registerTextures(IIconRegistrar iconRegistrar) {
        textures = new TextureAtlasSprite[getTypes()];
        textures[0] = iconRegistrar.registerSprite(new EFluxResourceLocation("blocks/basicCable"));
        textures[1] = iconRegistrar.registerSprite(new EFluxResourceLocation("blocks/normalCable"));
        textures[2] = iconRegistrar.registerSprite(new EFluxResourceLocation("blocks/advancedCable"));
    }

    /**
     * This method is used when a model is requested to render the block in a world.
     *
     * @param state The current BlockState.
     * @param iba   The IBlockAccess the block is in.
     * @param pos   The position of the block.
     * @return The model to render for this block for the given arguments.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public IBlockModel getBlockModel(IBlockState state, IBlockAccess iba, BlockPos pos) {
        return models.forMeta(getMetaFromState(state));
    }

    /**
     * This method is used when a model is requested when its not placed, so for an item.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public IBakedModel getBlockModel(Item item, int meta) {
        return models.forMeta(meta);
    }

    /**
     * A helper method to prevent you from having to hook into the event,
     * use this to make your quads. (This always comes AFTER the textures are loaded)
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels(ElecQuadBakery quadBakery, ElecModelBakery modelBakery, ElecTemplateBakery templateBakery) {
        models = new BakedModelMetaMap<IBlockModel>();
        for (int i = 0; i < textures.length; i++) {
            models.setModelForMeta(i, modelBakery.forTemplate(templateBakery.newDefaultBlockTemplate(textures[i]).setTexture(textures[i])));
        }
    }

    @SideOnly(Side.CLIENT)
    private void registerISBHR(){
        RenderingRegistry.instance().registerRenderer(this, new CableRenderer());
    }

}
