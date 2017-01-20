package thebetweenlands.common.network.serverbound;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thebetweenlands.common.capability.equipment.EnumEquipmentInventory;
import thebetweenlands.common.capability.equipment.EquipmentHelper;
import thebetweenlands.common.capability.equipment.IEquipmentCapability;
import thebetweenlands.common.item.equipment.IEquippable;
import thebetweenlands.common.network.MessageEntity;
import thebetweenlands.common.registries.CapabilityRegistry;

public class MessageEquipItem extends MessageEntity {
	private int sourceSlot, mode;
	private EnumEquipmentInventory inventory;

	public MessageEquipItem() { }

	/**
	 * Creates a message to equip an item
	 * @param sourceSlot
	 * @param target
	 * @param inventory
	 * @param destSlot
	 */
	public MessageEquipItem(int sourceSlot, Entity target) {
		this.addEntity(target);
		this.sourceSlot = sourceSlot;
		this.mode = 0;
	}

	/**
	 * Creates a message to unequip an item
	 * @param target
	 * @param destInv
	 * @param destSlot
	 */
	public MessageEquipItem(Entity target, EnumEquipmentInventory inventory, int slot) {
		this.addEntity(target);
		this.sourceSlot = slot;
		this.inventory = inventory;
		this.mode = 1;
	}

	@Override
	public void serialize(PacketBuffer buf) {
		super.serialize(buf);

		buf.writeInt(this.mode);

		switch(this.mode) {
		default:
		case 0:
			buf.writeInt(this.sourceSlot);
			break;

		case 1:
			buf.writeInt(this.sourceSlot);
			buf.writeInt(this.inventory.id);
			break;
		}
	}

	@Override
	public void deserialize(PacketBuffer buf) {
		super.deserialize(buf);

		this.mode = buf.readInt();

		switch(this.mode) {
		default:
		case 0:
			this.sourceSlot = buf.readInt();
			break;

		case 1:
			this.sourceSlot = buf.readInt();
			this.inventory = EnumEquipmentInventory.fromID(buf.readInt());
			break;
		}
	}

	@Override
	public IMessage process(MessageContext ctx) {
		super.process(ctx);

		if(ctx.getServerHandler() != null) {
			EntityPlayer sender = ctx.getServerHandler().playerEntity;
			Entity target = this.getEntity(0);

			if(target.hasCapability(CapabilityRegistry.CAPABILITY_EQUIPMENT, null)) {
				IEquipmentCapability cap = target.getCapability(CapabilityRegistry.CAPABILITY_EQUIPMENT, null);

				switch(this.mode) {
				default:
				case 0:
					//Equip
					if(this.sourceSlot < sender.inventory.getSizeInventory()) {
						ItemStack stack = sender.inventory.getStackInSlot(this.sourceSlot);

						if(stack != null && stack.getItem() instanceof IEquippable) {
							IEquippable equippable = (IEquippable) stack.getItem();

							if(equippable.canEquip(stack, sender, target, cap.getInventory(equippable.getEquipmentCategory(stack)))) {
								ItemStack result = EquipmentHelper.equipItem(sender, target, stack, false);

								if(result == null || result.stackSize != stack.stackSize) {
									if(!sender.capabilities.isCreativeMode) {
										sender.inventory.setInventorySlotContents(this.sourceSlot, result);
									}
								}
							}
						}
					}
					break;
				case 1:
					//Unequip
					IInventory inv = cap.getInventory(this.inventory);
					if(this.sourceSlot < inv.getSizeInventory()) {
						ItemStack stack = inv.getStackInSlot(this.sourceSlot);

						if(stack != null) {
							if(stack.getItem() instanceof IEquippable && 
									!((IEquippable) stack.getItem()).canUnequip(stack, sender, target, inv)) {
								break;
							}

							if(stack.getItem() instanceof IEquippable) {
								((IEquippable) stack.getItem()).onUnequip(stack, target, inv);
							}

							inv.setInventorySlotContents(this.sourceSlot, null);

							if(!sender.inventory.addItemStackToInventory(stack)) {
								target.entityDropItem(stack, target.getEyeHeight());
							}
						}
					}
					break;
				}
			}
		}

		return null;
	}
}