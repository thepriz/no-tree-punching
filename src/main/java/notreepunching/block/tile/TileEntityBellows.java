package notreepunching.block.tile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import notreepunching.block.IHasBellowsInput;
import notreepunching.block.ModBlocks;
import notreepunching.client.ModSounds;
import notreepunching.network.ModNetwork;
import notreepunching.network.PacketRequestBellows;
import notreepunching.network.PacketUpdateBellows;

import javax.annotation.Nonnull;

public class TileEntityBellows extends TileEntity implements ITickable{

    private boolean power;
    private double height; // Min 0.125 Max 0.875
    private final double stepSize = 0.028;
    private double step = stepSize;
    private int facing;

    // This needs to be public
    public TileEntityBellows(){
        super();
        power = false;
        height = 0.2;
        step = 0;
    }
    public TileEntityBellows(EnumFacing facing){
        this();
        this.facing = facing.getHorizontalIndex();
        this.markDirty();
    }

    public void update(){
        // This needs to run on both client and server (for graphical purposes)
        // the powered status is updated on the client through the network
        height += step;
        if(height <= 0.2 && step < 0){
            step = 0;
            height = 0.2;
            setPower(power);
        }else if(height >= 0.875 && step > 0){
            step = 0;
            height = 0.875;
            setPower(power);
        }
    }

    @Override
    public void onLoad() {
        if (world.isRemote) {
            ModNetwork.network.sendToServer(new PacketRequestBellows(this));
        }
    }

    public void setPower(boolean power){
        if(world.isRemote) return;

        this.power = power;
        this.markDirty();
        if(step != 0) return;

        if(height == 0.2){
            step = power ? stepSize : 0;
        }else if(height == 0.875){
            step = power ? 0 : -stepSize;
        }
        // If step was changed, then update client + forges + play sound
        if(step != 0){
            updateForges();
            world.playSound(null,pos, power ? ModSounds.bellowsOut : ModSounds.bellowsIn, SoundCategory.BLOCKS,1.0F,1.0F);
            ModNetwork.network.sendToAllAround(new PacketUpdateBellows(this),
                    new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64));
        }
    }
    public void setStep(double step){ this.step = step; } // Called from PacketUpdateBellows on CLIENT
    public void setFacing(int facing){ this.facing = facing; } // Called from PacketUpdateBellows on CLIENT
    public void setHeight(double height){ this.height = height; } // Called from PacketUpdateBellows on CLIENT
    public double getHeight(){ return height; } // Called from TESRBellows
    public int getFacing(){ return facing; } // Called from TESRBellows
    public double getStep(){ return step; } // Called from PacketUpdateBellows on SERVER

    private void updateForges(){
        if(!world.isRemote) {
            BlockPos forgePos = getPos().offset(EnumFacing.getHorizontal(facing));

            IBlockState state = world.getBlockState(forgePos);
            if(state.getBlock() == ModBlocks.blockTuyere ||
                    state.getBlock() == ModBlocks.blastFurnace) {

                IHasBellowsInput te = ((IHasBellowsInput) world.getTileEntity(forgePos));
                if (te != null) {
                    te.setAirTimer();
                }
            }
        }
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setBoolean("power", power);
        compound.setDouble("height", height);
        compound.setDouble("step", step);
        compound.setInteger("facing", facing);
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        power = compound.getBoolean("power");
        height = compound.getDouble("height");
        step = compound.getDouble("step");
        facing = compound.getInteger("facing");
        super.readFromNBT(compound);
    }

}