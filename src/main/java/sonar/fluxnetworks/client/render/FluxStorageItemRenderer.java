package sonar.fluxnetworks.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import sonar.fluxnetworks.api.FluxConstants;
import sonar.fluxnetworks.client.ClientRepository;
import sonar.fluxnetworks.common.block.FluxStorageBlock;
import sonar.fluxnetworks.common.util.FluxUtils;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class FluxStorageItemRenderer extends BlockEntityWithoutLevelRenderer {

    public FluxStorageItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(@Nonnull ItemStack stack, @Nonnull ItemTransforms.TransformType transformType,
                             @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                             int packedLight, int packedOverlay) {
        int color; // 0xRRGGBB
        long energy;
        CompoundTag tag = stack.getTagElement(FluxConstants.TAG_FLUX_DATA);
        if (tag != null) {
            if (tag.getBoolean(FluxConstants.FLUX_COLOR)) {
                // GUI display
                Screen screen = Minecraft.getInstance().screen;
                /*if (screen instanceof GuiFluxCore) {
                    GuiFluxCore gui = (GuiFluxCore) screen;
                    color = gui.network.getNetworkColor();
                } else {*/
                color = FluxConstants.INVALID_NETWORK_COLOR;
                //}
            } else if (tag.contains(FluxConstants.CLIENT_COLOR)) {
                // TheOneProbe
                color = tag.getInt(FluxConstants.CLIENT_COLOR);
            } else {
                // ItemStack inventory
                color = ClientRepository.getNetwork(tag.getInt(FluxConstants.NETWORK_ID)).getNetworkColor();
            }
            energy = tag.getLong(FluxConstants.ENERGY);
        } else {
            color = FluxConstants.INVALID_NETWORK_COLOR;
            energy = 0;
        }

        FluxStorageBlock block = (FluxStorageBlock) Block.byItem(stack.getItem());
        BlockState renderState = block.defaultBlockState();

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel model = dispatcher.getBlockModel(renderState);

        float r = FluxUtils.getRed(color), g = FluxUtils.getGreen(color), b = FluxUtils.getBlue(color);
        dispatcher.getModelRenderer()
                .renderModel(poseStack.last(), bufferSource.getBuffer(Sheets.cutoutBlockSheet()),
                        renderState, model, r, g, b, packedLight, packedOverlay, EmptyModelData.INSTANCE);
        FluxStorageEntityRenderer.render(poseStack, bufferSource.getBuffer(FluxStorageRenderType.getType()),
                color, packedOverlay, energy, block.getEnergyCapacity());
    }
}
