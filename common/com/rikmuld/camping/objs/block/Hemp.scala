package com.rikmuld.camping.objs.block

import java.util.ArrayList
import java.util.Random
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.AxisAlignedBB
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.common.EnumPlantType
import net.minecraftforge.common.IPlantable
import com.rikmuld.camping.objs.Objs
import net.minecraftforge.fml.relauncher.SideOnly
import com.rikmuld.corerm.objs.RMBlock
import java.io.ObjectInput
import com.rikmuld.corerm.objs.ObjInfo
import com.rikmuld.camping.objs.Objs
import com.rikmuld.corerm.objs.ObjInfo
import com.rikmuld.corerm.objs.RMBlock
import net.minecraftforge.fml.relauncher.Side
import com.rikmuld.corerm.objs.WithModel
import com.rikmuld.camping.CampingMod._
import net.minecraft.block.BlockReed
import net.minecraft.util.BlockPos
import com.rikmuld.corerm.misc.WorldBlock._
import net.minecraft.util.EnumFacing
import com.rikmuld.corerm.objs.WithInstable
import net.minecraft.block.state.BlockState
import net.minecraft.block.state.IBlockState
import scala.collection.JavaConversions._
import net.minecraft.util.EnumWorldBlockLayer
import net.minecraft.block.properties.PropertyInteger
import com.rikmuld.camping.objs.BlockDefinitions
import com.rikmuld.corerm.objs.RMItemBlock
import net.minecraft.entity.player.EntityPlayer
import com.sun.org.apache.bcel.internal.generic.GETSTATIC
import com.rikmuld.corerm.objs.RMIntProp
import com.rikmuld.camping.objs.block.Hemp._
import com.rikmuld.corerm.objs.WithProperties

object Hemp {
  val AGE = PropertyInteger.create("age", 0, 5);
}

class Hemp(modId:String, info:ObjInfo) extends RMBlock(modId, info) with IPlantable with WithModel with WithInstable with WithProperties {
  setTickRandomly(true)
  setDefaultState(getStateFromMeta(0))

  override def getProps = Array(new RMIntProp(AGE, 3, 0))
  override def canPlaceBlockAt(world: World, pos:BlockPos): Boolean = canStay((world, pos))
  override def canStay(bd:BlockData): Boolean = ((bd.world, bd.pos.down).block==this && bd.down.state.getValue(AGE) == BlockDefinitions.Hemp.GROWN_BIG_BOTTOM)||bd.down.block.canSustainPlant(bd.world, bd.pos.down, EnumFacing.UP, this)
  override def getCollisionBoundingBox(world: World, pos:BlockPos, state:IBlockState): AxisAlignedBB = null
  override def getItemDropped(state: IBlockState, random: Random, pInt: Int): Item = Item.getItemFromBlock(this)
  @SideOnly(Side.CLIENT)
  override def getItem(world: World, pos:BlockPos): Item = Item.getItemFromBlock(this)
  override def getPlantType(world: IBlockAccess, pos:BlockPos): EnumPlantType = EnumPlantType.Beach
  override def getPlant(world: IBlockAccess, pos:BlockPos): IBlockState = getDefaultState
  override def setBlockBoundsBasedOnState(world: IBlockAccess, pos:BlockPos) {
    if (getMetaFromState(world.getBlockState(pos)) == 4) setBlockBounds(0.3F, 0.0F, 0.3F, 0.7F, 1.0F, 0.7F)
    else if (getMetaFromState(world.getBlockState(pos)) == 3) setBlockBounds(0.3F, 0.0F, 0.3F, 0.7F, 0.8F, 0.7F)
    else if ((getMetaFromState(world.getBlockState(pos)) == 2) || (getMetaFromState(world.getBlockState(pos)) == 5)) setBlockBounds(0.3F, 0.0F, 0.3F, 0.7F, 0.6F, 0.7F)
    else if (getMetaFromState(world.getBlockState(pos)) == 1) setBlockBounds(0.3F, 0.0F, 0.3F, 0.7F, 0.4F, 0.7F)
    else if (getMetaFromState(world.getBlockState(pos)) == 0) setBlockBounds(0.3F, 0.0F, 0.3F, 0.7F, 0.2F, 0.7F)
  }
  @SideOnly(Side.CLIENT)
  override def colorMultiplier(world:IBlockAccess, pos:BlockPos, renderPass:Int) = world.getBiomeGenForCoords(pos).getGrassColorAtPos(pos)
  @SideOnly(Side.CLIENT)
  override def getBlockLayer = EnumWorldBlockLayer.CUTOUT
  override def updateTick(world: World, pos:BlockPos, state:IBlockState, random: Random) {
    super.updateTick(world, pos, state, random)
    if ((world.getLightBrightness(pos.up) * 15) >= 9) {
      val bd = (world, pos)
      val speed = Math.min(25, Math.max(1, getGrowthRate(bd)))
      if (bd.meta < BlockDefinitions.Hemp.GROWN_SMALL) {
        if (random.nextInt((25.0F / speed).toInt + 1) == 0) bd.setState(getStateFromMeta(bd.meta+1), 2)
      } else if (bd.meta == BlockDefinitions.Hemp.GROWN_SMALL) {
        if (random.nextInt((25.0F / speed).toInt + 1) == 0) {
          if ((world, pos.up).block == Blocks.air) {
            (world, pos.up).setState(getStateFromMeta(5), 2)
            bd.setState(getStateFromMeta(4), 2)
          }
        }
      }
    }
  }
  override def breakBlock(world:World, pos:BlockPos, state:IBlockState) = {
    if((world, pos.down).meta==BlockDefinitions.Hemp.GROWN_BIG_BOTTOM)(world, pos.down).setState(getStateFromMeta(BlockDefinitions.Hemp.GROWN_SMALL))
    super.breakBlock(world, pos, state)
  }
  def getGrowthRate(bd:BlockData): Float = {
    var water = if (bd.world.getBlockState(bd.relPos(1, -1, 0)).getBlock.getMaterial == Material.water) 1 else 0
    water += (if (bd.world.getBlockState(bd.relPos(-1, -1, 0)).getBlock.getMaterial == Material.water) 1 else 0)
    water += (if (bd.world.getBlockState(bd.relPos(0, -1, 1)).getBlock.getMaterial == Material.water) 1 else 0)
    water += (if (bd.world.getBlockState(bd.relPos(0, -1, -1)).getBlock.getMaterial == Material.water) 1 else 0)
    var light = Math.max(1, ((bd.world.getLightBrightness(bd.pos.up) * 15) - 9) / 3f)    
    var ground = if (bd.world.getBlockState(bd.pos.down).getBlock == Blocks.grass || bd.world.getBlockState(bd.pos.down).getBlock == Blocks.dirt) 2 else 1
    ground * water * light * config.hempSpeed
  }
  override def getDrops(world: IBlockAccess, pos:BlockPos, state:IBlockState, fortune: Int): java.util.List[ItemStack] = if (getMetaFromState(state) >= BlockDefinitions.Hemp.GROWN_SMALL) super.getDrops(world, pos, state, fortune) else List()
  def grow(bd:BlockData): Boolean = {
    if (bd.meta < BlockDefinitions.Hemp.GROWN_SMALL) bd.setState(getStateFromMeta(Math.min(BlockDefinitions.Hemp.GROWN_SMALL, bd.meta + new Random().nextInt(3 - bd.meta) + 1)), 2)
    else if (bd.meta == BlockDefinitions.Hemp.GROWN_SMALL) {
      bd.setState(getStateFromMeta(BlockDefinitions.Hemp.GROWN_BIG_BOTTOM), 2)
      (bd.world, bd.pos.up).setState(getStateFromMeta(BlockDefinitions.Hemp.GROWN_BIG_TOP), 2)
      true
    } else false
  }
}