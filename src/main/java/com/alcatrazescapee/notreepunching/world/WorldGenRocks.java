/*
 * Part of the No Tree Punching mod by AlcatrazEscapee.
 * Copyright (c) 2019. See the project LICENSE.md for details.
 */

package com.alcatrazescapee.notreepunching.world;


// todo: convert to feature
public class WorldGenRocks
{
    /*
    private static final Set<Material> MATERIALS = Sets.newHashSet(Material.GROUND, Material.SAND, Material.GRASS, Material.CLAY, Material.GOURD, Material.SNOW);

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider)
    {
        if (world.provider != null && world.provider.getDimension() == 0 && ModConfig.GENERAL.looseRocksGeneration)
        {
            for (int i = 0; i < ModConfig.GENERAL.looseRocksFrequency; i++)
            {
                int xCoord = chunkX * 16 + random.nextInt(16) + 8;
                int zCoord = chunkZ * 16 + random.nextInt(16) + 8;
                if (random.nextFloat() < 0.5)
                {
                    placeRock(world, CoreHelpers.getTopSolidBlock(world, new BlockPos(xCoord, 0, zCoord)), random);
                }
            }
        }
    }

    private void placeRock(World world, BlockPos pos, Random random)
    {
        IBlockState state = world.getBlockState(pos.up());
        if (!state.getMaterial().isLiquid() && !state.isOpaqueCube() && state.getBlock().isReplaceable(world, pos.up()))
        {
            IBlockState stateDown = world.getBlockState(pos);
            if (stateDown.isOpaqueCube() && MATERIALS.contains(stateDown.getMaterial()))
            {
                Stone stone = Stone.getFromBlock(world.getBlockState(pos.down(6)), random);
                Block stoneBlock = stone.isEnabled() ? LooseRockBlock.get(stone) : LooseRockBlock.get(Stone.STONE);
                world.setBlockState(pos.up(), stoneBlock.getDefaultState());
            }
        }
    }
    */
}
