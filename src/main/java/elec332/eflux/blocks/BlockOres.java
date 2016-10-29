package elec332.eflux.blocks;

import com.google.common.base.Strings;
import elec332.core.api.client.IIconRegistrar;
import elec332.core.api.client.model.IElecModelBakery;
import elec332.core.api.client.model.IElecQuadBakery;
import elec332.core.api.client.model.IElecTemplateBakery;
import elec332.core.client.model.loading.INoJsonBlock;
import elec332.core.client.model.map.BakedModelMetaMap;
import elec332.eflux.EFlux;
import elec332.eflux.client.EFluxResourceLocation;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by Elec332 on 21-7-2015.
 */
public class BlockOres extends BlockWithMeta implements INoJsonBlock {

    public BlockOres() {
        super(Material.ROCK, "ore", EFlux.ModID.toLowerCase());
        setResistance(5.0f);
        setHardness(2.5f);
    }

    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite[] textures;
    @SideOnly(Side.CLIENT)
    private BakedModelMetaMap<IBakedModel> models;

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        String s = nameForType(stack.getItemDamage());
        if (Strings.isNullOrEmpty(s)) {
            return "ERROR_BLOCK_EFLUX";
        }
        return getUnlocalizedName()+"."+s;
    }

    public String nameForType(int meta){
        switch (meta){
            case 0:
                return "copper";
            case 1:
                return "tin";
            case 2:
                return "zinc";
            case 3:
                return "silver";
            default:
                return null;
        }
    }

    @Override
    public int getTypes() {
        return 4;
    }

    /**
     * This method is used when a model is requested to render the block in a world.
     *
     * @param state The current BlockState.
     * @return The model to render for this block for the given arguments.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public IBakedModel getBlockModel(IBlockState state) {
        return models.forMeta(state.getValue(getProperty()));
    }

    /**
     * This method is used when a model is requested when its not placed, so for an item.
     *
     * @return The model to render when the block is not placed.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public IBakedModel getItemModel(ItemStack stack, World world, EntityLivingBase entity) {
        return models.forMeta(stack.getItemDamage());
    }

    /**
     * A helper method to prevent you from having to hook into the event,
     * use this to make your quads. (This always comes AFTER the textures are loaded)
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels(IElecQuadBakery quadBakery, IElecModelBakery modelBakery, IElecTemplateBakery templateBakery) {
        models = new BakedModelMetaMap<IBakedModel>();
        for (int i = 0; i < getTypes(); i++) {
            models.setModelForMeta(i, modelBakery.forTemplate(templateBakery.newDefaultBlockTemplate(textures[i]).setTexture(textures[i])));
        }
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
        for (int i = 0; i < getTypes(); i++) {
            textures[i] = iconRegistrar.registerSprite(new EFluxResourceLocation("blocks/ore/"+nameForType(i)+"_ore"));
        }
    }

}
