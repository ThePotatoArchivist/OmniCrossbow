package archives.tater.omnicrossbow.util

import net.minecraft.core.HolderSet
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

operator fun HolderSet<Item>.contains(stack: ItemStack) = stack.`is`(this)