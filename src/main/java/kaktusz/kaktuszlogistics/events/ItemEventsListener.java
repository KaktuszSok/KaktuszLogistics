package kaktusz.kaktuszlogistics.events;

import kaktusz.kaktuszlogistics.items.CustomItem;
import kaktusz.kaktuszlogistics.events.input.PlayerTriggerHeldEvent;
import kaktusz.kaktuszlogistics.items.properties.ItemProperty;
import kaktusz.kaktuszlogistics.recipe.CraftingRecipe;
import kaktusz.kaktuszlogistics.recipe.RecipeManager;
import kaktusz.kaktuszlogistics.recipe.SmeltingRecipe;
import kaktusz.kaktuszlogistics.recipe.inputs.ItemInput;
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
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("ConstantConditions")
public class ItemEventsListener implements Listener {

    @EventHandler(ignoreCancelled = false)
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
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if(e.getClickedInventory() instanceof FurnaceInventory) {
            onTryInsertFurnace(e.getCursor(), e);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent e) {
        if(e.getInventory() instanceof FurnaceInventory) {
            for(int s : e.getInventorySlots()) {
                if(s == 0) {
                    onTryInsertFurnace(e.getOldCursor(), e);
                    return;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryMove(InventoryMoveItemEvent e) {
        if(e.getDestination() instanceof FurnaceInventory) {
            onTryInsertFurnace(e.getItem(), e);
        }
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
    public void onTryInsertFurnace(ItemStack stack, Cancellable e) { //TODO: disallow if output is not stackable
        if(CustomItem.getFromStack(stack) != null) {
            if(RecipeManager.matchSmeltingRecipe(new ItemInput(stack)) == null) {
                e.setCancelled(true);
            }
        }
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

