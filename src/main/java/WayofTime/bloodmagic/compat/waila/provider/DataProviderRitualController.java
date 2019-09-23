package WayofTime.bloodmagic.compat.waila.provider;

import WayofTime.bloodmagic.BloodMagic;
import WayofTime.bloodmagic.util.Constants;
import WayofTime.bloodmagic.ritual.imperfect.ImperfectRitual;
import WayofTime.bloodmagic.util.helper.PlayerHelper;
import WayofTime.bloodmagic.tile.TileMasterRitualStone;
import WayofTime.bloodmagic.util.helper.TextHelper;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;

public class DataProviderRitualController implements IWailaDataProvider {

    public static final IWailaDataProvider INSTANCE = new DataProviderRitualController();

    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (!config.getConfig(Constants.Compat.WAILA_CONFIG_RITUAL))
            return currenttip;

        CompoundNBT tag = accessor.getNBTData();
        if (tag.getBoolean("master")) {
            if (tag.hasKey("ritual")) {
                currenttip.add(TextHelper.localizeEffect(tag.getString("ritual")));
                if (tag.hasKey("owner"))
                    currenttip.add(TextHelper.localizeEffect("tooltip.bloodmagic.currentOwner", tag.getString("owner")));
                if (!tag.getBoolean("active"))
                    currenttip.add(TextHelper.localizeEffect("tooltip.bloodmagic.deactivated"));
                if (!tag.getBoolean("enabled"))
                    currenttip.add(TextHelper.localizeEffect("tooltip.bloodmagic.config.disabled"));
            }
        } else {
            if (tag.hasKey("ritual")) {
                currenttip.add(TextHelper.localizeEffect(tag.getString("ritual")));
                if (!tag.getBoolean("enabled"))
                    currenttip.add(TextHelper.localizeEffect("tooltip.bloodmagic.config.disabled"));
            }
        }

        return currenttip;
    }

    @Nonnull
    @Override
    public CompoundNBT getNBTData(ServerPlayerEntity player, TileEntity te, CompoundNBT tag, World world, BlockPos pos) {

        if (te instanceof TileMasterRitualStone) {
            TileMasterRitualStone mrs = (TileMasterRitualStone) te;
            tag.putBoolean("master", true);
            if (mrs.getCurrentRitual() != null) {
                tag.putString("ritual", mrs.getCurrentRitual().getTranslationKey());
                tag.putBoolean("active", mrs.isActive());
                if (mrs.getOwner() != null)
                    tag.putString("owner", PlayerHelper.getUsernameFromUUID(mrs.getOwner()));
                tag.putBoolean("enabled", BloodMagic.RITUAL_MANAGER.enabled(BloodMagic.RITUAL_MANAGER.getId(mrs.getCurrentRitual()), false));
            }
        } else {
            tag.putBoolean("master", false);

            ImperfectRitual ritual = BloodMagic.RITUAL_MANAGER.getImperfectRitual(world.getBlockState(pos.up()));
            if (ritual != null) {
                tag.putString("ritual", ritual.getTranslationKey());
                tag.putBoolean("enabled", BloodMagic.RITUAL_MANAGER.enabled(BloodMagic.RITUAL_MANAGER.getId(ritual), false));
            }
        }

        return tag;
    }
}
