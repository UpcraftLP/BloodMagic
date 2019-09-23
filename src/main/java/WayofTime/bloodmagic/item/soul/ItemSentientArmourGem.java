package WayofTime.bloodmagic.item.soul;

import WayofTime.bloodmagic.BloodMagic;
import WayofTime.bloodmagic.client.IMeshProvider;
import WayofTime.bloodmagic.item.armour.ItemSentientArmour;
import WayofTime.bloodmagic.soul.EnumDemonWillType;
import WayofTime.bloodmagic.soul.PlayerDemonWillHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Consumer;

public class ItemSentientArmourGem extends Item implements IMeshProvider {
    public ItemSentientArmourGem() {
        super();

        setCreativeTab(BloodMagic.TAB_BM);
        setTranslationKey(BloodMagic.MODID + ".sentientArmourGem");
        setMaxStackSize(1);
    }

    public EnumDemonWillType getCurrentType(ItemStack stack) {
        return EnumDemonWillType.DEFAULT;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        boolean hasSentientArmour = false;
        NonNullList<ItemStack> armourInventory = player.inventory.armorInventory;
        for (ItemStack armourStack : armourInventory) {
            if (armourStack != null && armourStack.getItem() instanceof ItemSentientArmour) {
                hasSentientArmour = true;
            }
        }

        if (hasSentientArmour) {
            ItemSentientArmour.revertAllArmour(player);
        } else {
            EnumDemonWillType type = PlayerDemonWillHandler.getLargestWillType(player);
            double will = PlayerDemonWillHandler.getTotalDemonWill(type, player);

//                PlayerDemonWillHandler.consumeDemonWill(player, willBracket[bracket]);
            ItemSentientArmour.convertPlayerArmour(type, will, player);
        }

        return new ActionResult<>(ActionResultType.PASS, player.getHeldItem(hand));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ItemMeshDefinition getMeshDefinition() {
        return stack -> {
            boolean flag = false;
            NonNullList<ItemStack> armourInventory = Minecraft.getInstance().player.inventory.armorInventory;
            for (ItemStack armourStack : armourInventory) {
                if (armourStack != null && armourStack.getItem() instanceof ItemSentientArmour) {
                    flag = true;
                }
            }

            return new ModelResourceLocation(stack.getItem().getRegistryName(), "type=" + (flag ? "" : "de") + "activated");
        };
    }

    @Override
    public void gatherVariants(Consumer<String> variants) {
        variants.accept("type=activated");
        variants.accept("type=deactivated");
    }
}
