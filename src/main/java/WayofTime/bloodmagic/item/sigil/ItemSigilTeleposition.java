package WayofTime.bloodmagic.item.sigil;

import WayofTime.bloodmagic.core.data.Binding;
import WayofTime.bloodmagic.iface.ISigil;
import WayofTime.bloodmagic.teleport.TeleportQueue;
import WayofTime.bloodmagic.teleport.Teleports;
import WayofTime.bloodmagic.tile.TileTeleposer;
import WayofTime.bloodmagic.util.helper.NBTHelper;
import WayofTime.bloodmagic.util.helper.PlayerHelper;
import WayofTime.bloodmagic.util.helper.TextHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ItemSigilTeleposition extends ItemSigilBase {

    public ItemSigilTeleposition() {
        super("teleposition");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);

        TeleportLocation location = getTeleportLocation(stack);
        if (location != null) {
            tooltip.add("");
            tooltip.add(TextHelper.localizeEffect("tooltip.bloodmagic.telepositionFocus.coords", location.pos.getX(), location.pos.getY(), location.pos.getZ()));
            tooltip.add(TextHelper.localizeEffect("tooltip.bloodmagic.telepositionFocus.dimension", location.dim));
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.getItem() instanceof ISigil.Holding)
            stack = ((Holding) stack.getItem()).getHeldItem(stack, player);
        if (PlayerHelper.isFakePlayer(player))
            return ActionResult.newResult(ActionResultType.FAIL, stack);

        TeleportLocation location = getTeleportLocation(stack);
        Binding binding = getBinding(stack);
        if (!world.isRemote && location != null && binding != null) {
            World teleportTo = DimensionManager.getWorld(location.dim);
            if (teleportTo != null) {
                TileEntity tile = teleportTo.getTileEntity(location.pos);
                if (tile instanceof TileTeleposer) {
                    BlockPos blockPos = location.pos.up();
                    UUID bindingOwnerID = binding.getOwnerId();
                    if (world.provider.getDimension() == location.dim) {
                        TeleportQueue.getInstance().addITeleport(new Teleports.TeleportSameDim(blockPos, player, bindingOwnerID, true));

                    } else {
                        TeleportQueue.getInstance().addITeleport(new Teleports.TeleportToDim(blockPos, player, bindingOwnerID, world, tile.getWorld().provider.getDimension(), true));
                    }
                }
            }
        }

        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public ActionResultType onItemUse(PlayerEntity player, World world, BlockPos pos, Hand hand, Direction side, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.getItem() instanceof ISigil.Holding)
            stack = ((Holding) stack.getItem()).getHeldItem(stack, player);
        if (PlayerHelper.isFakePlayer(player))
            return ActionResultType.FAIL;

        if (!world.isRemote && player.isSneaking() && NBTHelper.checkNBT(stack) != null) {
            if (world.getTileEntity(pos) != null && world.getTileEntity(pos) instanceof TileTeleposer) {
                TeleportLocation teleportLocation = new TeleportLocation(world.provider.getDimension(), pos);
                updateLocation(stack, teleportLocation);
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.FAIL;
    }

    @Nullable
    public TeleportLocation getTeleportLocation(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemSigilTeleposition))
            return null;

        if (!stack.hasTagCompound())
            return null;

        CompoundNBT locationTag = stack.getSubCompound("tplocation");
        if (locationTag == null)
            return null;

        return TeleportLocation.fromTag(locationTag);
    }

    public void updateLocation(ItemStack stack, TeleportLocation location) {
        CompoundNBT tagCompound;
        if (!stack.hasTagCompound())
            stack.setTagCompound(tagCompound = new CompoundNBT());
        else
            tagCompound = stack.getTagCompound();

        tagCompound.putTag("tplocation", location.serializeNBT());
    }

    public static class TeleportLocation implements INBTSerializable<CompoundNBT> {

        private int dim;
        private BlockPos pos;

        private TeleportLocation() {
        }

        public TeleportLocation(int dim, BlockPos pos) {
            this.dim = dim;
            this.pos = pos;
        }

        public TeleportLocation(int dim, int x, int y, int z) {
            this(dim, new BlockPos(x, y, z));
        }

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT tag = new CompoundNBT();
            tag.putInt("dim", dim);
            tag.putLong("pos", pos.toLong());
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            this.dim = nbt.getInt("dim");
            this.pos = BlockPos.fromLong(nbt.getLong("pos"));
        }

        public static TeleportLocation fromTag(CompoundNBT tpTag) {
            TeleportLocation location = new TeleportLocation();
            location.deserializeNBT(tpTag);
            return location;
        }
    }
}
