package kaktusz.kaktuszlogistics.gui;

import kaktusz.kaktuszlogistics.recipe.machine.MachineRecipe;
import kaktusz.kaktuszlogistics.world.multiblock.MultiblockMachine;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MachineGUI extends InteractableGUI {
	private static final DecimalFormat progressFormatting = new DecimalFormat("00.0");

	private final MultiblockMachine machine;

	public MachineGUI(MultiblockMachine machine) {
		super(27, machine.getProperty().getName());
		this.machine = machine;
	}

	@Override
	protected void clearInventory() {
		super.clearInventory();

		//fill borders
		ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
		setName(border,"");
		for(int c = 0; c < INVENTORY_WIDTH; c++) {
			setSlot(0, c, border);
			if(c == 0 || c == INVENTORY_WIDTH-1) //vertical borders along side
				setSlot(1, c, border);
			setSlot(2, c, border);
		}

		//top middle
		ItemStack header = new ItemStack(machine.getGUIHeader(), 1);
		setName(header, machine.getName());
		setLore(header, machine.getLore());
		setSlot(0, 4, header);

		//progress bar and recipe
		GUIButton progressButton = new GUIButton()
				.setLeftClickAction(machine::toggleProcessing);
		for(int c = 1; c < INVENTORY_WIDTH-2; c++) {
			addButton(1, c, progressButton, new ItemStack(Material.BARRIER));
		}
		GUIButton recipeButton = new GUIButton()
				.setLeftClickAction(machine::tryStartProcessing);
		//TODO: right click = choose recipe
		addButton(1, INVENTORY_WIDTH-2, recipeButton, new ItemStack(Material.BARRIER));

		//update visuals for progress bar and recipe
		update();
	}

	public void update() {
		MachineRecipe<?> recipe = machine.getRecipe();

		ItemStack recipeIcon = getItemInSlot(1,INVENTORY_WIDTH-2);
		if(recipe == null) {
			recipeIcon.setType(Material.PAINTING);
			setName(recipeIcon, "Click to choose a recipe");
		}
		else {
			if(machine.isProcessingRecipe()) {
				recipeIcon = recipe.getDisplayIcon();
			}
			else {
				String startString;
				if(machine.isHalted()) {
					recipeIcon = recipe.getDisplayIcon();
					//noinspection ConstantConditions
					setName(recipeIcon, ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH
							+ ChatColor.stripColor(recipeIcon.getItemMeta().getDisplayName()));
					startString = ChatColor.DARK_GRAY + "[Cannot start when machine is halted]";
				} else {
					recipeIcon = recipe.getDisplayIcon(machine.getCachedSupplies());
					startString = ChatColor.DARK_GRAY + "[Left-Click to start]";
				}

				insertLore(recipeIcon, -1,
						startString,
						ChatColor.DARK_GRAY + "[Right-Click to change recipe]");
			}
			setSlot(1, INVENTORY_WIDTH-2, recipeIcon);
		}

		final int progressBarLength = INVENTORY_WIDTH-3; //from column # 1 to INVENTORY_WIDTH-3 (9 - 3 = 6: 1,2,3,4,5,6)
		double progressPercent = 0;
		int filledProgressSlots = 0;
		if(machine.isProcessingRecipe() && recipe != null) {
			progressPercent = 1 - (machine.getTimeLeft() / (double) recipe.time);
			filledProgressSlots = (int) Math.ceil(progressPercent * progressBarLength);
		}
		String progressTitle;
		List<String> progressLore = new ArrayList<>();
		if(recipe == null)
			progressTitle = ChatColor.DARK_GRAY + "No Recipe Chosen";
		else {
			if(!machine.isProcessingRecipe()) {
				if(machine.isHalted()) {
					progressTitle = ChatColor.BLUE + "Machine Halted";
					progressLore.add(ChatColor.DARK_GRAY + "[Click to resume]");
				}
				else {
					progressTitle = "Machine on Standby";
					progressLore.add(ChatColor.DARK_GRAY + "[Click to pause]");
				}
			}
			else {
				ChatColor colour = machine.isHalted() ? ChatColor.BLUE : ChatColor.WHITE;
				progressTitle = colour + "Progress: " + progressFormatting.format(progressPercent*100f) + "%";
				progressLore.add(ChatColor.GRAY + "Time remaining: " + MachineRecipe.getTimeString(machine.getTimeLeft()));
				progressLore.add(ChatColor.DARK_GRAY + (!machine.isHalted() ? "[Click to pause]" : "[Click to resume]"));
			}
		}

		for(int c = 1; c <= progressBarLength; c++) {
			ItemStack progressBar = getItemInSlot(1,c);
			if(c <= filledProgressSlots) {
				progressBar.setType(machine.isHalted() ? Material.BLUE_STAINED_GLASS_PANE : Material.LIME_STAINED_GLASS_PANE);
			} else {
				progressBar.setType(Material.WHITE_STAINED_GLASS_PANE);
			}
			setName(progressBar, progressTitle);
			setLore(progressBar, progressLore);
		}
	}

	@SuppressWarnings("ConstantConditions")
	private static void setName(ItemStack stack, String name) {
		name = ChatColor.WHITE + name;
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(name);
		stack.setItemMeta(meta);
	}

	private static void setLore(ItemStack stack, String... lore) {
		setLore(stack, Arrays.asList(lore));
	}
	@SuppressWarnings("ConstantConditions")
	private static void setLore(ItemStack stack, List<String> lore) {
		ItemMeta meta = stack.getItemMeta();
		meta.setLore(lore);
		stack.setItemMeta(meta);
	}

	@SuppressWarnings("ConstantConditions")
	private static void insertLore(ItemStack stack, int index, String... lore) {
		ItemMeta meta = stack.getItemMeta();
		List<String> currLore = meta.getLore();
		if(currLore == null)
			currLore = new ArrayList<>();
		if(index != -1)
			currLore.addAll(index, Arrays.asList(lore));
		else
			currLore.addAll(Arrays.asList(lore));
		meta.setLore(currLore);
		stack.setItemMeta(meta);
	}

}
