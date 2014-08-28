/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.inventory;

import net.minecraft.item.ItemStack;

import net.minecraftforge.oredict.OreDictionary;

import buildcraft.core.ItemList;

public class StackHelper {

	protected StackHelper() {
	}

	/* STACK MERGING */
	/**
	 * Checks if two ItemStacks are identical enough to be merged
	 *
	 * @param stack1 - The first stack
	 * @param stack2 - The second stack
	 * @return true if stacks can be merged, false otherwise
	 */
	public static boolean canStacksMerge(ItemStack stack1, ItemStack stack2) {
		if (stack1 == null || stack2 == null) {
			return false;
		}
		if (!stack1.isItemEqual(stack2)) {
			return false;
		}
		if (!ItemStack.areItemStackTagsEqual(stack1, stack2)) {
			return false;
		}
		return true;

	}

	public static boolean canStacksOrListsMerge(ItemStack stack1, ItemStack stack2) {
		if (stack1 == null || stack2 == null) {
			return false;
		}

		if (stack1.getItem() instanceof ItemList) {
			return ItemList.matches(stack1, stack2);
		} else if (stack2.getItem() instanceof ItemList) {
			return ItemList.matches(stack2, stack1);
		}

		if (!stack1.isItemEqual(stack2)) {
			return false;
		}
		if (!ItemStack.areItemStackTagsEqual(stack1, stack2)) {
			return false;
		}
		return true;

	}

	/**
	 * Merges mergeSource into mergeTarget
	 *
	 * @param mergeSource - The stack to merge into mergeTarget, this stack is
	 * not modified
	 * @param mergeTarget - The target merge, this stack is modified if doMerge
	 * is set
	 * @param doMerge - To actually do the merge
	 * @return The number of items that was successfully merged.
	 */
	public static int mergeStacks(ItemStack mergeSource, ItemStack mergeTarget, boolean doMerge) {
		if (!canStacksMerge(mergeSource, mergeTarget)) {
			return 0;
		}
		int mergeCount = Math.min(mergeTarget.getMaxStackSize() - mergeTarget.stackSize, mergeSource.stackSize);
		if (mergeCount < 1) {
			return 0;
		}
		if (doMerge) {
			mergeTarget.stackSize += mergeCount;
		}
		return mergeCount;
	}

	/* ITEM COMPARISONS */
	/**
	 * Determines whether the given ItemStack should be considered equivalent
	 * for crafting purposes.
	 *
	 * @param base The stack to compare to.
	 * @param comparison The stack to compare.
	 * @param oreDictionary true to take the Forge OreDictionary into account.
	 * @return true if comparison should be considered a crafting equivalent for
	 * base.
	 */
	public static boolean isCraftingEquivalent(ItemStack base, ItemStack comparison, boolean oreDictionary) {
		if (isMatchingItem(base, comparison, true, false)) {
			return true;
		}

		if (oreDictionary) {
			int[] idBase = OreDictionary.getOreIDs(base);
			if (idBase.length > 0) {
				for (int id : idBase) {
					for (ItemStack itemstack : OreDictionary.getOres(OreDictionary.getOreName(id))) {
						if (comparison.getItem() == itemstack.getItem() && (itemstack.getItemDamage() == OreDictionary.WILDCARD_VALUE || comparison.getItemDamage() == itemstack.getItemDamage())) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	public static boolean isCraftingEquivalent(int[] oreIDs, ItemStack comparison) {
		if (oreIDs.length > 0) {
			for (int id : oreIDs) {
				for (ItemStack itemstack : OreDictionary.getOres(OreDictionary.getOreName(id))) {
					if (comparison.getItem() == itemstack.getItem() && (itemstack.getItemDamage() == OreDictionary.WILDCARD_VALUE || comparison.getItemDamage() == itemstack.getItemDamage())) {
						return true;
					}
				}
			}
		}

		return false;
	}

	public static boolean isMatchingItemOrList(ItemStack a, ItemStack b) {
		if (a == null || b == null) {
			return false;
		}

		if (a.getItem() instanceof ItemList) {
			return ItemList.matches(a, b);
		} else if (b.getItem() instanceof ItemList) {
			return ItemList.matches(b, a);
		}

		return isMatchingItem(a, b, true, false);
	}

	/**
	 * Compares item id, damage and NBT. Accepts wildcard damage. Ignores damage
	 * entirely if the item doesn't have subtypes.
	 *
	 * @param base The stack to compare to.
	 * @param comparison The stack to compare.
	 * @return true if id, damage and NBT match.
	 */
	public static boolean isMatchingItem(ItemStack base, ItemStack comparison) {
		return isMatchingItem(base, comparison, true, true);
	}

	/**
	 * Compares item id, and optionally damage and NBT. Accepts wildcard damage.
	 * Ignores damage entirely if the item doesn't have subtypes.
	 *
	 * @param a ItemStack
	 * @param b ItemStack
	 * @param matchDamage
	 * @param matchNBT
	 * @return true if matches
	 */
	public static boolean isMatchingItem(final ItemStack a, final ItemStack b, final boolean matchDamage,
			final boolean matchNBT) {
		if (a == null || b == null) {
			return false;
		}

		if (a.getItem() != b.getItem()) {
			return false;
		}
		if (matchDamage && a.getHasSubtypes()) {
			if (!isWildcard(a) && !isWildcard(b)) {
				if (a.getItemDamage() != b.getItemDamage()) {
					return false;
				}
			}
		}
		if (matchNBT) {
			if (a.stackTagCompound != null && !a.stackTagCompound.equals(b.stackTagCompound)) {
				return false;
			}
		}
		return true;
	}

	public static boolean isWildcard(ItemStack stack) {
		return isWildcard(stack.getItemDamage());
	}

	public static boolean isWildcard(int damage) {
		return damage == -1 || damage == OreDictionary.WILDCARD_VALUE;
	}
}
