package kaktusz.kaktuszlogistics.items.events;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.items.properties.ItemProperty;
import kaktusz.kaktuszlogistics.modules.weaponry.input.ITriggerHeldListener;
import kaktusz.kaktuszlogistics.modules.weaponry.input.PlayerTriggerHeldEvent;
import kaktusz.kaktuszlogistics.recipe.CraftingRecipe;
import kaktusz.kaktuszlogistics.recipe.RecipeManager;
import kaktusz.kaktuszlogistics.recipe.SmeltingRecipe;
import kaktusz.kaktuszlogistics.recipe.inputs.ItemInput;
import kaktusz.kaktuszlogistics.util.SetUtils;
import kaktusz.kaktuszlogistics.util.minecraft.VanillaUtils;
import org.bukkit.Material;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.*;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Forwards events from vanilla & modules to CustomItems
 */
@SuppressWarnings("ConstantConditions")
public class ItemEventsListener implements Listener {

    /**
     * Insertions into this inventory with target slot index within the specified range will pass the check() function
     */
    private static class InventorySlotRange {
        public final InventoryType inventoryType;
        public final int inventoryMinSlot;
        public final int inventoryMaxSlot;

        public InventorySlotRange(InventoryType inventoryType, int inventoryMaxSlot) {
            this.inventoryType = inventoryType;
            this.inventoryMinSlot = 0;
            this.inventoryMaxSlot = inventoryMaxSlot;
        }
        public InventorySlotRange(InventoryType inventoryType, int inventoryMinSlot, int inventoryMaxSlot) {
            this.inventoryType = inventoryType;
            this.inventoryMinSlot = inventoryMinSlot;
            this.inventoryMaxSlot = inventoryMaxSlot;
        }

        /**
         * @return True if the inventory's class matches and the target slot is less than or equal the tuple's max slot
         */
        public boolean check(Inventory targetInventory, int targetSlot) {
            return (targetSlot == -1 || targetSlot >= inventoryMinSlot && targetSlot <= inventoryMaxSlot) && targetInventory.getType() == inventoryType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InventorySlotRange that = (InventorySlotRange) o;
            return inventoryMaxSlot == that.inventoryMaxSlot && inventoryType.equals(that.inventoryType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(inventoryType);
        }
    }

    /**
     * Any insertions that match one or more of these checks will be cancelled
     */
    private static final Set<InventorySlotRange> bannedInventories = SetUtils.setFromElements(
            new InventorySlotRange(InventoryType.ANVIL, 2),
            new InventorySlotRange(InventoryType.BREWING, 4),
            new InventorySlotRange(InventoryType.CARTOGRAPHY, 2),
            new InventorySlotRange(InventoryType.ENCHANTING, 1, 1),
            new InventorySlotRange(InventoryType.GRINDSTONE, 2),
            new InventorySlotRange(InventoryType.LOOM, 3),
            new InventorySlotRange(InventoryType.SMITHING, 2),
            new InventorySlotRange(InventoryType.STONECUTTER, 1)
    );

    @SuppressWarnings("DefaultAnnotationParam") //clarity
    @EventHandler(ignoreCancelled = false) //air clicks are always cancelled for whatever reason, and we dont want to miss them
    public void onItemUsed(PlayerInteractEvent e) {
        if(e.useItemInHand() == Event.Result.DENY)
            return;

        ItemStack item = e.getItem();
        CustomItem customItem = CustomItem.getFromStack(item);
        if(customItem == null)
            return;
        //item
        if(customItem instanceof IUseListener)
            ((IUseListener)customItem).onTryUse(e, item);
        //properties
        for(ItemProperty p : customItem.getAllProperties()) {
            if(p instanceof IUseListener) {
                ((IUseListener)p).onTryUse(e, item);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemUsedOnEntity(PlayerInteractEntityEvent e) {
        if(e.getRightClicked() instanceof ItemFrame) //allow placing into itemframes
            return;

        ItemStack item = e.getPlayer().getInventory().getItem(e.getHand());
        CustomItem customItem = CustomItem.getFromStack(item);
        if(customItem == null)
            return;
        //item
        if(customItem instanceof IUseListener)
            ((IUseListener)customItem).onTryUseEntity(e, item);
        //properties
        for(ItemProperty p : customItem.getAllProperties()) {
            if(p instanceof IUseListener) {
                ((IUseListener)p).onTryUseEntity(e, item);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlaceBlock(BlockPlaceEvent e) {
        ItemStack item = e.getItemInHand();
        CustomItem customItem = CustomItem.getFromStack(item);
        if(customItem == null)
            return;
        //item
        if(customItem instanceof IPlacedListener)
            ((IPlacedListener)customItem).onTryPlace(e, item);
        //properties
        for(ItemProperty p : customItem.getAllProperties()) {
            if(p instanceof IPlacedListener) {
                ((IPlacedListener)p).onTryPlace(e, item);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemHeld(PlayerItemHeldEvent e) {
        ItemStack item = e.getPlayer().getInventory().getItem(e.getNewSlot());
        CustomItem customItem = CustomItem.getFromStack(item);
        if(customItem == null)
            return;
        //item
        if(customItem instanceof IHeldListener)
            ((IHeldListener)customItem).onHeld(e, item);
        //properties
        for(ItemProperty p : customItem.getAllProperties()) {
            if(p instanceof IHeldListener) {
                ((IHeldListener)p).onHeld(e, item);
            }
        }
    }

    //MODULE EVENTS
    @EventHandler(ignoreCancelled = true)
    public void onTriggerHeld(PlayerTriggerHeldEvent e) {
        ItemStack main = e.getPlayer().getInventory().getItemInMainHand();
        ItemStack secondary = e.getPlayer().getInventory().getItemInOffHand();
        onTriggerHeldForItem(e, main);
        onTriggerHeldForItem(e, secondary);
    }
    private void onTriggerHeldForItem(PlayerTriggerHeldEvent e, ItemStack stack) {
        CustomItem customItem = CustomItem.getFromStack(stack);
        if(customItem == null)
            return;
        //item
        if(customItem instanceof ITriggerHeldListener)
            ((ITriggerHeldListener)customItem).onTriggerHeld(e, stack);
        //properties
        for(ItemProperty p : customItem.getAllProperties()) {
            if(p instanceof ITriggerHeldListener) {
                ((ITriggerHeldListener)p).onTriggerHeld(e, stack);
            }
        }
    }

    //SPECIAL INVENTORIES
    /**
     * Handles an item being put into a certain slot of an inventory
     * @param stack The item stack being put into the inventory
     * @param slot The slot of the inventory it is being put in. -1 if unknown.
     * @param inv The target inventory
     * @param e The event of this happening
     */
    private void onItemPutInInventory(ItemStack stack, int slot, Inventory inv, Cancellable e) {
        if(slot == -999 || inv == null) //-999 = clicked outside of inventory
            return;
        //special behaviour: furnace
        if(inv.getType() == InventoryType.FURNACE) {
            if(slot <= 0)
                onTryInsertFurnace(stack, ((FurnaceInventory) inv).getResult(), e);
            return;
        }
        //banned inventories:
        if(CustomItem.getFromStack(stack) == null) //don't block vanilla items
            return;
        for (InventorySlotRange bannedInv : bannedInventories) {
            if(bannedInv.check(inv, slot)) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if(e.isShiftClick() || e.getClick().isKeyboardClick()) { //transfer between inventories sorta deal
            onItemPutInInventory(e.getCurrentItem(), -1, e.getInventory(), e);
            return;
        }
        onItemPutInInventory(e.getCursor(), e.getSlot(), e.getClickedInventory(), e);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent e) {
        Map<Integer, ItemStack> newItems = e.getNewItems();
        Inventory inv = e.getInventory();
        for(int s : e.getInventorySlots()) {
            onItemPutInInventory(newItems.get(s), s, inv, e);
            if(e.isCancelled()) //if it cancelled the event, dont bother with the rest of the items
                break;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryMove(InventoryMoveItemEvent e) {
        onItemPutInInventory(e.getItem(), -1, e.getDestination(), e);
    }

    //CRAFTING
    @EventHandler(ignoreCancelled = true)
    public void onTryCraft(PrepareItemCraftEvent e) {
        ItemInput[] inputs = ItemInput.fromStackArray(e.getInventory().getMatrix());
        CraftingRecipe matching = RecipeManager.matchCraftingRecipe(inputs);
        if(matching != null) {
            e.getInventory().setResult(matching.getCachedOutputs().get(0).getStack()); //craft special item
            return;
        }

        for (ItemStack itemStack : e.getInventory()) {
            if(CustomItem.getFromStack(itemStack) != null) {
                e.getInventory().setResult(new ItemStack(Material.AIR)); //make uncraftable
            }
        }
    }

    //SMELTING
    public void onTryInsertFurnace(ItemStack stack, ItemStack outputSlotStack, Cancellable e) {
        if(CustomItem.getFromStack(stack) != null) {
            ItemInput input = new ItemInput(stack);
            SmeltingRecipe smeltingRecipe = RecipeManager.matchSmeltingRecipe(input);
            if(smeltingRecipe == null) {
                e.setCancelled(true);
            }
            else if(!VanillaUtils.canCombineStacks(smeltingRecipe.getOutputs(input).get(0).getStack(), outputSlotStack)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBurn(FurnaceBurnEvent e) {
        if(CustomItem.getFromStack(e.getFuel()) != null)
            e.setCancelled(true); //make custom items unburnable
    }

    @EventHandler(ignoreCancelled = true)
    public void onSmelt(FurnaceSmeltEvent e) {
        ItemInput input = new ItemInput(e.getSource());
        SmeltingRecipe matching = RecipeManager.matchSmeltingRecipe(input);
        if (matching != null) {
            e.setResult(matching.getCachedOutputs().get(0).getStack()); //intercept recipe and apply correct output
            return;
        }

        if(CustomItem.getFromStack(e.getSource()) != null) {
            e.setCancelled(true);
        }
    }
}

