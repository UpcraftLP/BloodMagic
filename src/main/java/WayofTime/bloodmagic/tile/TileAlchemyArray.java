package WayofTime.bloodmagic.tile;

import WayofTime.bloodmagic.alchemyArray.AlchemyArrayEffect;
import WayofTime.bloodmagic.alchemyArray.AlchemyArrayEffectCraftingNew;
import WayofTime.bloodmagic.api.impl.BloodMagicAPI;
import WayofTime.bloodmagic.api.impl.recipe.RecipeAlchemyArray;
import WayofTime.bloodmagic.core.registry.AlchemyArrayRecipeRegistry;
import WayofTime.bloodmagic.iface.IAlchemyArray;
import WayofTime.bloodmagic.util.Constants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileAlchemyArray extends TileInventory implements ITickable, IAlchemyArray {
    public boolean isActive = false;
    public int activeCounter = 0;
    public Direction rotation = Direction.HORIZONTALS[0];
    public int rotateCooldown = 0;

    private String key = "empty";
    public AlchemyArrayEffect arrayEffect;
    private boolean doDropIngredients = true;

    public TileAlchemyArray() {
        super(2, "alchemyArray");
    }

    public void onEntityCollidedWithBlock(BlockState state, Entity entity) {
        if (arrayEffect != null) {
            arrayEffect.onEntityCollidedWithBlock(this, getWorld(), pos, state, entity);
        }
    }

    @Override
    public void deserialize(CompoundNBT tagCompound) {
        super.deserialize(tagCompound);
        this.isActive = tagCompound.getBoolean("isActive");
        this.activeCounter = tagCompound.getInt("activeCounter");
        this.key = tagCompound.getString("key");
        if (!tagCompound.hasKey("doDropIngredients")) //Check if the array is old
        {
            this.doDropIngredients = true;
        } else {
            this.doDropIngredients = tagCompound.getBoolean("doDropIngredients");
        }
        this.rotation = Direction.HORIZONTALS[tagCompound.getInt(Constants.NBT.DIRECTION)];

        CompoundNBT arrayTag = tagCompound.getCompoundTag("arrayTag");
        arrayEffect = AlchemyArrayRecipeRegistry.getAlchemyArrayEffect(key);
        if (arrayEffect != null) {
            arrayEffect.readFromNBT(arrayTag);
        }
    }

    @Override
    public CompoundNBT serialize(CompoundNBT tagCompound) {
        super.serialize(tagCompound);
        tagCompound.putBoolean("isActive", isActive);
        tagCompound.putInt("activeCounter", activeCounter);
        tagCompound.putString("key", "".equals(key) ? "empty" : key);
        tagCompound.putBoolean("doDropIngredients", doDropIngredients);
        tagCompound.putInt(Constants.NBT.DIRECTION, rotation.getHorizontalIndex());

        CompoundNBT arrayTag = new CompoundNBT();
        if (arrayEffect != null) {
            arrayEffect.writeToNBT(arrayTag);
        }
        tagCompound.putTag("arrayTag", arrayTag);

        return tagCompound;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    //Use this to prevent the Array from dropping items - useful for arrays that need to "consume" ingredients well before the effect.
    public void setItemDrop(boolean dropItems) {
        this.doDropIngredients = dropItems;
    }

    @Override
    public void update() {
        if (isActive && attemptCraft()) {
            activeCounter++;
        } else {
            isActive = false;
            doDropIngredients = true;
            activeCounter = 0;
            arrayEffect = null;
            key = "empty";
        }
        if (rotateCooldown > 0)
            rotateCooldown--;
    }

    /**
     * This occurs when the block is destroyed.
     */
    @Override
    public void dropItems() {
        if (arrayEffect == null || doDropIngredients) {
            super.dropItems();
        }
    }

    public boolean attemptCraft() {
        AlchemyArrayEffect effect = AlchemyArrayRecipeRegistry.getAlchemyArrayEffect(this.getStackInSlot(0), this.getStackInSlot(1));
        if (effect != null) {
            if (arrayEffect == null) {
                arrayEffect = effect;
                key = effect.getKey();
            } else {
                String effectKey = effect.getKey();
                if (effectKey.equals(key)) {
                    //Good! Moving on.
                } else {
                    //Something has changed, therefore we have to move our stuffs.
                    //TODO: Add an AlchemyArrayEffect.onBreak(); ?
                    arrayEffect = effect;
                    key = effect.getKey();
                }
            }
        } else {
            RecipeAlchemyArray recipe = BloodMagicAPI.INSTANCE.getRecipeRegistrar().getAlchemyArray(getStackInSlot(0), getStackInSlot(1));
            if (recipe == null)
                return false;

            AlchemyArrayEffect newEffect = new AlchemyArrayEffectCraftingNew(recipe);
            if (arrayEffect == null) {
                arrayEffect = newEffect;
                key = newEffect.key;
            } else if (!newEffect.key.equals(key)) {
                arrayEffect = newEffect;
                key = newEffect.key;
            }
        }

        if (arrayEffect != null) {
            isActive = true;

            if (arrayEffect.update(this, this.activeCounter)) {
                this.decrStackSize(0, 1);
                this.decrStackSize(1, 1);
                this.getWorld().setBlockToAir(getPos());
            }

            return true;
        }

        return false;
    }

    @Override
    public Direction getRotation() {
        return rotation;
    }

    public void setRotation(Direction rotation) {
        this.rotation = rotation;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return Block.FULL_BLOCK_AABB.offset(getPos());
    }
}
