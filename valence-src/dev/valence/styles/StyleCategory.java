package dev.valence.styles;

import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public record StyleCategory(String id, Item icon, String preview, List<String> textures) {
   public static final List<StyleCategory> ALL = List.of(
      new StyleCategory("crystal", Items.END_CRYSTAL, "crystal", List.of("textures/entity/end_crystal/end_crystal.png")),
      new StyleCategory("obsidian", Items.OBSIDIAN, "cube", List.of("textures/block/obsidian.png", "textures/block/crying_obsidian.png")),
      new StyleCategory(
         "anchor",
         Items.RESPAWN_ANCHOR,
         "anchor",
         List.of(
            "textures/block/respawn_anchor_side4.png",
            "textures/block/respawn_anchor_top.png",
            "textures/block/respawn_anchor_top_off.png",
            "textures/block/respawn_anchor_bottom.png",
            "textures/block/respawn_anchor_side0.png",
            "textures/block/respawn_anchor_side1.png",
            "textures/block/respawn_anchor_side2.png",
            "textures/block/respawn_anchor_side3.png"
         )
      ),
      new StyleCategory(
         "armor",
         Items.NETHERITE_CHESTPLATE,
         "armor",
         List.of(
            "textures/entity/equipment/humanoid/netherite.png",
            "textures/entity/equipment/humanoid_leggings/netherite.png",
            "textures/entity/equipment/humanoid/diamond.png",
            "textures/entity/equipment/humanoid_leggings/diamond.png",
            "textures/item/netherite_helmet.png",
            "textures/item/netherite_chestplate.png",
            "textures/item/netherite_leggings.png",
            "textures/item/netherite_boots.png",
            "textures/item/diamond_helmet.png",
            "textures/item/diamond_chestplate.png",
            "textures/item/diamond_leggings.png",
            "textures/item/diamond_boots.png"
         )
      ),
      new StyleCategory("swords", Items.NETHERITE_SWORD, "item", List.of("textures/item/netherite_sword.png", "textures/item/diamond_sword.png")),
      new StyleCategory(
         "tools",
         Items.NETHERITE_PICKAXE,
         "item",
         List.of(
            "textures/item/netherite_pickaxe.png",
            "textures/item/diamond_pickaxe.png",
            "textures/item/netherite_axe.png",
            "textures/item/diamond_axe.png",
            "textures/item/netherite_shovel.png",
            "textures/item/diamond_shovel.png"
         )
      ),
      new StyleCategory("mace", Items.MACE, "item", List.of("textures/item/mace.png")),
      new StyleCategory("gapples", Items.GOLDEN_APPLE, "item", List.of("textures/item/golden_apple.png")),
      new StyleCategory("pearls", Items.ENDER_PEARL, "item", List.of("textures/item/ender_pearl.png")),
      new StyleCategory("totem", Items.TOTEM_OF_UNDYING, "item", List.of("textures/item/totem_of_undying.png")),
      new StyleCategory("xp", Items.EXPERIENCE_BOTTLE, "item", List.of("textures/item/experience_bottle.png")),
      new StyleCategory("shield", Items.SHIELD, "shield", List.of("textures/entity/shield_base_nopattern.png", "textures/entity/shield_base.png")),
      new StyleCategory("cobweb", Items.COBWEB, "cross", List.of("textures/block/cobweb.png"))
   );

   public String nameKey() {
      return "valence.styles.category." + this.id;
   }

   public static StyleCategory byId(String id) {
      for (StyleCategory c : ALL) {
         if (c.id.equals(id)) {
            return c;
         }
      }

      return null;
   }
}
