/*
 *
 *  Part of the No Tree Punching Mod by alcatrazEscapee
 *  Work under Copyright. Licensed under the GPL-3.0.
 *  See the project LICENSE.md for more information.
 *
 */

package notreepunching.event;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import notreepunching.NoTreePunching;
import notreepunching.config.ModConfig;
import notreepunching.item.ItemKnife;
import notreepunching.item.ItemMattock;
import notreepunching.item.ModItems;
import notreepunching.recipe.knife.KnifeRecipeHandler;

public class HarvestEventHandler {

    // Controls the slow mining speed of blocks that aren't the right tool
    @SubscribeEvent
    public void slowMining(PlayerEvent.BreakSpeed event){

        EntityPlayer player = event.getEntityPlayer();

        if (player == null || player instanceof FakePlayer) {
            return;
        }

        if (player.capabilities.isCreativeMode) {
            return;
        }
        // Get Variables for the block and item held
        Block block = event.getState().getBlock();
        ItemStack heldItemStack = player.getHeldItemMainhand();

        // Get variables for the required and current harvest levels + tools
        int neededHarvestLevel = block.getHarvestLevel(event.getState());
        String neededToolClass = block.getHarvestTool(event.getState());

        // Allows Mattocks to break at slightly higher than normal speeds
        if (heldItemStack.getItem() instanceof ItemMattock){
            ItemMattock mattock = (ItemMattock) heldItemStack.getItem();
            if(mattock.shouldBreakBlock(block)){
                event.setNewSpeed(event.getOriginalSpeed()*1.4F);
                return;
            }
        }

        // Allows knifes to break at normal speeds
        if (heldItemStack.getItem() instanceof ItemKnife) {
            ItemKnife knife = (ItemKnife) heldItemStack.getItem();
            if (knife.shouldBreakBlock(block)) {
                return;
            }
        }

        // heldItemStack != ItemStack.EMPTY  && neededHarvestLevel >= 0
        if (neededToolClass != null) {
            for (String toolClass : heldItemStack.getItem().getToolClasses(heldItemStack)) {
                if (neededToolClass.equals(toolClass)) {
                    if (heldItemStack.getItem().getHarvestLevel(heldItemStack, toolClass, null, null) >= neededHarvestLevel) {
                        return;
                    }
                }else if(neededToolClass.equals("shovel") && toolClass.equals("pickaxe") && heldItemStack.getItem().getHarvestLevel(heldItemStack, toolClass, null, null) >= 1) {
                    return;
                }
                // Tinkers Construct Mattock
                else if ((neededToolClass.equals("shovel") && toolClass.equals("mattock")) || (neededToolClass.equals("axe") && toolClass.equals("mattock"))) {
                    return;
                }
            }
            // Always allow certian blocks to break at normal speed
            //Iterator itr = Arrays.asList(Config.VanillaTweaks.BREAKABLE).iterator();
            for(String name : ModConfig.VanillaTweaks.BREAKABLE){
                if(block.getRegistryName() == null) continue;
                if (block.getRegistryName().toString().equals(name)) {
                    return;
                }
            }

            switch (neededToolClass) {
                case "axe":
                    event.setNewSpeed(event.getOriginalSpeed() / 5);
                    break;
                case "shovel":
                    event.setNewSpeed(event.getOriginalSpeed() / 3);
                    break;
                case "pickaxe":
                    event.setNewSpeed(event.getOriginalSpeed() / 8);
                default:
                    event.setNewSpeed(event.getOriginalSpeed() / 3);
            }
        }

    }

    // Controls the drops of any block that is broken to require specific tools.
    @SubscribeEvent
    public void harvestBlock(BlockEvent.HarvestDropsEvent event) {

        EntityPlayer player = event.getHarvester();

        if (!event.getWorld().isRemote) {
            // Explosions, Quarries, etc. all are ok to break blocks
            if (player == null || player instanceof FakePlayer) {
                return;
            }
            // Always allow creative mode
            if (player.capabilities.isCreativeMode) {
                return;
            }

            // Get Variables for the block and item held
            Block block = event.getState().getBlock();
            ItemStack heldItemStack = player.getHeldItemMainhand();
            if(block.getRegistryName() == null) return;


            // Leaves now drop sticks 20% without a knife. 50% with a knife
            if (block instanceof BlockLeaves) {
                float stickDropChance = 0.2F;
                if(heldItemStack.getItem() instanceof ItemKnife){
                    stickDropChance += 0.3F;
                }
                if (event.getWorld().rand.nextFloat() <= stickDropChance) {
                    event.getDrops().add(new ItemStack(Items.STICK));
                }
                return;
            }

            // Stone and its Variants drop the respective rock
            if(ModConfig.MODULE_STONEWORKS) {
                int meta;
                if (block == Blocks.STONE) {
                    meta = block.getMetaFromState(event.getState());
                    if (meta == 0) { // Stone
                        event.getDrops().clear();
                        event.getDrops().add(new ItemStack(ModItems.rockStone, 3, 0));
                    } else if (meta == 1) { // Granite
                        event.getDrops().clear();
                        event.getDrops().add(new ItemStack(ModItems.rockStone, 3, 3));
                    } else if (meta == 3) { // Diorite
                        event.getDrops().clear();
                        event.getDrops().add(new ItemStack(ModItems.rockStone, 3, 2));
                    } else if (meta == 5) { // Andesite
                        event.getDrops().clear();
                        event.getDrops().add(new ItemStack(ModItems.rockStone, 3, 1));
                    }
                }
                if (NoTreePunching.hasQuark) {
                    meta = block.getMetaFromState(event.getState());
                    if (block.getRegistryName().toString().equals("quark:marble") && meta == 0) {
                        event.getDrops().clear();
                        event.getDrops().add(new ItemStack(ModItems.rockStone, 3, 4));
                    } else if (block.getRegistryName().toString().equals("quark:limestone") && meta == 0) {
                        event.getDrops().clear();
                        event.getDrops().add(new ItemStack(ModItems.rockStone, 3, 5));
                    }
                }
                if (NoTreePunching.hasRustic) {
                    if (block.getRegistryName().toString().equals("rustic:slate")) {
                        event.getDrops().clear();
                        event.getDrops().add(new ItemStack(ModItems.rockStone, 3, 6));
                    }
                }
            }

            //Allows Knifes to have special drops when breaking blocks
            if (heldItemStack.getItem() instanceof ItemKnife) {
                ItemKnife knife = (ItemKnife) heldItemStack.getItem();
                if (knife.shouldBreakBlock(block)) {
                    if(block instanceof BlockDoublePlant || block instanceof BlockTallGrass){
                        if(Math.random()< ModConfig.Balance.GRASS_FIBER_CHANCE){
                            event.getDrops().add(KnifeRecipeHandler.getRandomGrassDrop());
                        }

                    }
                }
                if (knife.shouldDamageItem(block)) {
                    player.getHeldItemMainhand().damageItem(1, player);
                }
            }

            //Allows mattock to drop the normal block drop
            if(heldItemStack.getItem() instanceof ItemMattock){
                ItemMattock mattock = (ItemMattock) heldItemStack.getItem();
                if(mattock.shouldBreakBlock(block)){
                    return;
                }
            }

            // Final case: Get variables for the required and current harvest levels + tools
            int neededHarvestLevel = block.getHarvestLevel(event.getState());
            String neededToolClass = block.getHarvestTool(event.getState());

            //heldItemStack != ItemStack.EMPTY && neededHarvestLevel >= 0
            if (neededToolClass != null) {
                for (String toolClass : heldItemStack.getItem().getToolClasses(heldItemStack)) {
                    if (neededToolClass.equals(toolClass)) {
                        if (heldItemStack.getItem().getHarvestLevel(heldItemStack, toolClass, null, null) >= neededHarvestLevel) {
                            return;
                        }
                    }
                    // Metal Pickaxes and above are allowed to function as shovels with no mining speed penalty + block drops.
                    else if(neededToolClass.equals("shovel") && toolClass.equals("pickaxe") && heldItemStack.getItem().getHarvestLevel(heldItemStack, toolClass, null, null) >= 1) {
                        return;
                    }
                    // Tinkers Construct Mattock
                    else if ((neededToolClass.equals("shovel") && toolClass.equals("mattock")) || (neededToolClass.equals("axe") && toolClass.equals("mattock"))) {
                        return;
                    }
                }

                //Iterator itr = Arrays.asList(Config.VanillaTweaks.BREAKABLE).iterator();
                String blockName=block.getRegistryName().getResourceDomain()+":"+block.getRegistryName().getResourcePath();
                for(String name : ModConfig.VanillaTweaks.BREAKABLE) {
                    if(name.equals(blockName)) {
                        return;
                    }
                }
                /*while (itr.hasNext()) {
                    if (blockName.equals(itr.next())) {
                        return;
                    }
                }*/

                event.getDrops().clear();
            }
        }
    }

    @SubscribeEvent
    public void harvestBlockInitialCheck(PlayerEvent.HarvestCheck event){
        Block block = event.getTargetBlock().getBlock();
        if(block.getRegistryName() == null) return;
        String blockName=block.getRegistryName().getResourceDomain()+":"+block.getRegistryName().getResourcePath();
        for(String name : ModConfig.VanillaTweaks.BREAKABLE){
            if(blockName.equals(name)){ event.setCanHarvest(true); }
        }
    }

    @SubscribeEvent
    public void blockEventCheck(BlockEvent.BreakEvent event){
        // Additional check for IC2 and the Damn Rubberwood
        Block block = event.getState().getBlock();
        if(block.getRegistryName() == null) return;
        if(block.getUnlocalizedName().equals("ic2.rubber_wood")){
            EntityPlayer player = event.getPlayer();
            if (player == null || player instanceof FakePlayer) {
                return;
            }
            // Always allow creative mode
            if (player.isCreative()) {
                return;
            }
            ItemStack heldItemStack = player.getHeldItemMainhand();
            for (String toolClass : heldItemStack.getItem().getToolClasses(heldItemStack)) {
                if(toolClass.equals("axe")){ return; }
            }
            //Iterator itr = Arrays.asList(Config.VanillaTweaks.BREAKABLE).iterator();
            String blockName=block.getRegistryName().getResourceDomain()+":"+block.getRegistryName().getResourcePath();
            for(String name : ModConfig.VanillaTweaks.BREAKABLE){
                if(blockName.equals(name)){ return; }
            }
            // Else, cancel the event and do a manual break, not triggering the IC2 breaking
            event.setCanceled(true);
            event.getWorld().setBlockState(event.getPos(),Blocks.AIR.getDefaultState());
        }
    }
}
