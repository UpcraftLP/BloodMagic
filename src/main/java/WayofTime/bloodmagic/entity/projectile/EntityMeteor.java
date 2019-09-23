package WayofTime.bloodmagic.entity.projectile;

import WayofTime.bloodmagic.util.Constants;
import WayofTime.bloodmagic.meteor.MeteorRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IThrowableEntity;

public class EntityMeteor extends ThrowableEntity implements IThrowableEntity {
    public ItemStack meteorStack = ItemStack.EMPTY;
    protected int ticksInAir = 0;
    protected int maxTicksInAir = 600;
    protected double radiusModifier = 1;
    protected double explosionModifier = 1;
    protected double fillerChance = 0;

    public EntityMeteor(World world) {
        super(world);
    }

    public EntityMeteor(World world, double x, double y, double z, double velX, double velY, double velZ, double radiusModifier, double explosionModifier, double fillerChance) {
        super(world);
        this.setSize(1F, 1F);
        this.setPosition(x, y, z);
        motionX = velX;
        motionY = velY;
        motionZ = velZ;
        this.radiusModifier = radiusModifier;
        this.explosionModifier = explosionModifier;
        this.fillerChance = fillerChance;
    }

    @Override
    protected float getGravityVelocity() {
        return 0.03F;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.ticksExisted > this.maxTicksInAir) {
            setDead();
        }
    }

    @Override
    protected void onImpact(RayTraceResult mop) {
        if (mop.typeOfHit == RayTraceResult.Type.ENTITY && mop.entityHit != null) {
            this.onImpact(mop.entityHit);
        } else if (mop.typeOfHit == RayTraceResult.Type.BLOCK) {
            generateMeteor(mop.getBlockPos());
        }

        this.setDead();
    }

    protected void onImpact(Entity mop) {
        if (mop instanceof LivingEntity) {
            doDamage(100, mop);
        }

        generateMeteor(mop.getPosition());

        // spawnHitParticles("magicCrit", 8);
        this.setDead();
    }

    protected void doDamage(int i, Entity mop) {
        mop.attackEntityFrom(this.getDamageSource(), i);
    }

    public void generateMeteor(BlockPos pos) {
        MeteorRegistry.generateMeteorForItem(meteorStack, getEntityWorld(), pos, Blocks.STONE.getDefaultState(), radiusModifier, explosionModifier, fillerChance);
    }

    public DamageSource getDamageSource() {
        return DamageSource.ANVIL;
    }

    @Override
    public void writeEntityToNBT(CompoundNBT nbt) {
        super.writeEntityToNBT(nbt);
        nbt.putInt(Constants.NBT.PROJECTILE_TICKS_IN_AIR, ticksInAir);
        nbt.putInt(Constants.NBT.PROJECTILE_MAX_TICKS_IN_AIR, maxTicksInAir);
        nbt.putDouble("radiusModifier", radiusModifier);
        nbt.putDouble("explosionModifier", explosionModifier);
        nbt.putDouble("fillerChance", fillerChance);
        if (!meteorStack.isEmpty())
            meteorStack.writeToNBT(nbt);
        else
            nbt.putBoolean("noItem", true);
    }

    @Override
    public void readEntityFromNBT(CompoundNBT nbt) {
        super.readEntityFromNBT(nbt);
        ticksInAir = nbt.getInt(Constants.NBT.PROJECTILE_TICKS_IN_AIR);
        maxTicksInAir = nbt.getInt(Constants.NBT.PROJECTILE_MAX_TICKS_IN_AIR);
        radiusModifier = nbt.getDouble("radiusModifier");
        explosionModifier = nbt.getDouble("explosionModifier");
        fillerChance = nbt.getDouble("fillerChance");
        if (!nbt.hasKey("noItem"))
            meteorStack = new ItemStack(nbt);
        else
            meteorStack = ItemStack.EMPTY;
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public void setThrower(Entity entity) {

    }

    public void setMeteorStack(ItemStack meteorStack) {
        this.meteorStack = meteorStack;
    }
}