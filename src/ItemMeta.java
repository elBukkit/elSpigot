package org.bukkit.inventory.meta;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

/**
 * This type represents the storage mechanism for auxiliary item data.
 * <p>
 * An implementation will handle the creation and application for ItemMeta.
 * This class should not be implemented by a plugin in a live environment.
 */
public interface ItemMeta extends Cloneable, ConfigurationSerializable {

    /**
     * Checks for existence of a display name.
     *
     * @return true if this has a display name
     */
    boolean hasDisplayName();

    /**
     * Gets the display name that is set.
     * <p>
     * Plugins should check that hasDisplayName() returns <code>true</code>
     * before calling this method.
     *
     * @return the display name that is set
     */
    String getDisplayName();

    /**
     * Sets the display name.
     *
     * @param name the name to set
     */
    void setDisplayName(String name);

    /**
     * Checks for existence of lore.
     *
     * @return true if this has lore
     */
    boolean hasLore();

    /**
     * Gets the lore that is set.
     * <p>
     * Plugins should check if hasLore() returns <code>true</code> before
     * calling this method.
     * 
     * @return a list of lore that is set
     */
    List<String> getLore();

    /**
     * Sets the lore for this item. 
     * Removes lore when given null.
     *
     * @param lore the lore that will be set
     */
    void setLore(List<String> lore);

    /**
     * Checks for the existence of any enchantments.
     *
     * @return true if an enchantment exists on this meta
     */
    boolean hasEnchants();

    /**
     * Checks for existence of the specified enchantment.
     *
     * @param ench enchantment to check
     * @return true if this enchantment exists for this meta
     */
    boolean hasEnchant(Enchantment ench);

    /**
     * Checks for the level of the specified enchantment.
     *
     * @param ench enchantment to check
     * @return The level that the specified enchantment has, or 0 if none
     */
    int getEnchantLevel(Enchantment ench);

    /**
     * Returns a copy the enchantments in this ItemMeta. <br> 
     * Returns an empty map if none.
     *
     * @return An immutable copy of the enchantments
     */
    Map<Enchantment, Integer> getEnchants();

    /**
     * Adds the specified enchantment to this item meta.
     *
     * @param ench Enchantment to add
     * @param level Level for the enchantment
     * @param ignoreLevelRestriction this indicates the enchantment should be
     *     applied, ignoring the level limit
     * @return true if the item meta changed as a result of this call, false
     *     otherwise
     */
    boolean addEnchant(Enchantment ench, int level, boolean ignoreLevelRestriction);

    /**
     * Removes the specified enchantment from this item meta.
     *
     * @param ench Enchantment to remove
     * @return true if the item meta changed as a result of this call, false
     *     otherwise
     */
    boolean removeEnchant(Enchantment ench);

   /**
    * Checks if the specified enchantment conflicts with any enchantments in
    * this ItemMeta.
    *
    * @param ench enchantment to test
    * @return true if the enchantment conflicts, false otherwise
    */
    boolean hasConflictingEnchant(Enchantment ench);

    /**
     * Set itemflags which should be ignored when rendering a ItemStack in the Client. This Method does silently ignore double set itemFlags.
     *
     * @param itemFlags The hideflags which shouldn't be rendered
     */
    void addItemFlags(ItemFlag... itemFlags);

    /**
     * Remove specific set of itemFlags. This tells the Client it should render it again. This Method does silently ignore double removed itemFlags.
     *
     * @param itemFlags Hideflags which should be removed
     */
    void removeItemFlags(ItemFlag... itemFlags);

    /**
     * Get current set itemFlags. The collection returned is unmodifiable.
     *
     * @return A set of all itemFlags set
     */
    Set<ItemFlag> getItemFlags();

    /**
     * Check if the specified flag is present on this item.
     *
     * @param flag the flag to check
     * @return if it is present
     */
    boolean hasItemFlag(ItemFlag flag);

    /**
     * Check to see if this Item has any custom or unknown data.
     * <p>
     * This is normally data that has been put here by a Plugin.
     *
     * @return true if this Item has any custom data
     */
    boolean hasCustomData();

    /**
     * Retrieve a read/write accessor fo this Item's custom data.
     * <p>
     * Changes made to this object will affect the Item's custom
     * data directly.
     * <p>
     * These objects should not be retained, it is best to get a
     * new reference each time you want to modify the item, since
     * items may get copied as they move around or spawn and get
     * picked up.
     * <p>
     * If this item has no custom data, calling this will initialize
     * custom data for the item.
     * <p>
     * Use hasData for an efficient pre-check if you
     * only getting data to look for a specific key or value.
     *
     * @return Access to the Item's custom data
     */
    ConfigurationSection getCustomData();

    /**
     * See if this item is glowing for any reason.
     * This could be due to enchantments, or to having its gow effect set with setGlowEffect(true)
     *
     * @return true if this item is glowing
     */
    boolean hasGlowEffect();

    /**
     * Force this item to glow, or remove an item's glow.
     *
     * Glow can only be removed this way if there are no enchantments on the item.
     *
     * @param glow if true, a visual glow effect will be added to the item, effect removed if false
     */
    void setGlowEffect(boolean glow);

    @SuppressWarnings("javadoc")
    ItemMeta clone();

    // Spigot start
    public class Spigot
    {

        /**
         * Sets the unbreakable tag
         *
         * @param unbreakable true if set unbreakable
         */
        public void setUnbreakable(boolean unbreakable)
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        /**
         * Return if the unbreakable tag is true
         *
         * @return true if the unbreakable tag is true
         */
        public boolean isUnbreakable()
        {
            throw new UnsupportedOperationException( "Not supported yet." );
        }
    }

    Spigot spigot();
    // Spigot end
}
