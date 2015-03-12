package org.bukkit.craftbukkit.inventory;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.NBTBase;
import net.minecraft.server.NBTTagByte;
import net.minecraft.server.NBTTagByteArray;
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagDouble;
import net.minecraft.server.NBTTagFloat;
import net.minecraft.server.NBTTagInt;
import net.minecraft.server.NBTTagIntArray;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.NBTTagLong;
import net.minecraft.server.NBTTagShort;
import net.minecraft.server.NBTTagString;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates any custom data that may be attached to CraftMetaItem.
 */
public class CraftMetaItemData extends MemoryConfiguration {

    /**
     * Create a deep copy of another item's data
     *
     * @param other The data to copy
     */
    protected CraftMetaItemData(CraftMetaItemData other) {
        apply(this, other.map);
    }

    /**
     * Create an empty item data object.
     */
    protected CraftMetaItemData() {
    }

    /**
     * Retrieve a CraftMetaItemData object for a given NBTTagCompound.
     * <p>
     * This will scan the tag for any custom tags, those which are
     * not registered in ItemMetaKey, and copy them if present.
     * <p>
     * If there are no custom tags, this will return null and
     * allocate no memory.
     *
     * @param tag The NBTTagCompound to search for custom data
     * @return A new CraftMetaItemData object, or null if no custom data was found.
     */
    protected static CraftMetaItemData getCustomData(NBTTagCompound tag) {
        return getData(tag, getCustomKeys(tag));
    }

    protected static CraftMetaItemData getData(NBTTagCompound tag, Collection<String> keys) {
        if (keys == null) return null;
        CraftMetaItemData itemData = new CraftMetaItemData();
        for (String key : keys) {
            itemData.set(key, convert(tag.get(key), key, itemData));
        }
        return itemData;
    }

    protected static CraftMetaItemData getData(NBTTagCompound tag) {
        return getData(tag, getAllKeys(tag));
    }

    /**
     * Retrieve a CraftMetaItemData object for a given Map of data.
     * <p>
     * This will scan the Map for any custom tags, those which are
     * not registered in ItemMetaKey, and copy them if present.
     * <p>
     * If there are no custom tags, this will return null and
     * allocate no memory.
     *
     * @param map The Map to search for custom data
     * @return A new CraftMetaItemData object, or null if no custom data was found.
     */
    protected static CraftMetaItemData getCustomData(Map<String, Object> map) {
        return getData(map, getCustomKeys(map));
    }

    protected static CraftMetaItemData getData(Map<String, Object> map, Collection<String> keys) {
        if (keys == null) return null;

        CraftMetaItemData itemData = new CraftMetaItemData();
        apply(itemData, map, keys);

        return itemData;
    }

    protected static CraftMetaItemData getData(Map<String, Object> map) {
        return getData(map, map.keySet());
    }

    /**
     * This will serialize all data into an ImmutableMap.Builder.
     *
     * @param builder The Builder to put this ItemData into.
     * @return The same builder
     */
    ImmutableMap.Builder<String, Object> serialize(ImmutableMap.Builder<String, Object> builder) {
        for (Map.Entry<String, Object> extra : map.entrySet()) {
            // Does this need to recurse through the map and make an
            // ImmutableMap.Builder for each Map in the data, and so on?
            builder.put(extra.getKey(), extra.getValue());
        }
        return builder;
    }

    /**
     * Apply this data to an Item's NBTTag data.
     *
     * @param itemTag The item data to apply our map to.
     */
    protected void applyToItem(NBTTagCompound itemTag) {
        applyToItem(itemTag, map, true);
    }

    /**
     * Apply this data to an NBTTag, without filtering custom data.
     *
     * @param tag The data to apply our map to.
     */
    protected void applyToTag(NBTTagCompound tag) {
        applyToItem(tag, map, false);
    }

    /**
     * Convert an NBTBase object to an object of the appropriate type for
     * inclusion in our data map.
     * <p>
     * This will convert a compound tag into either a Map (used for object
     * deserialization) or a ConfigurationSection.
     * <p>
     * It is not possible to store a Map directly.
     *
     * @param tag The tag to convert and store
     * @param key The key of this tag, needed if creating as a ConfigurationSection.
     * @param baseSection If non-null, compound tags will be made into ConfigurationSections instead of Maps
     * @return The converted object, or null if nothing was stored
     */
    private static Object convert(NBTBase tag, String key, ConfigurationSection baseSection) {
        if (tag == null) return null;

        Object value = null;
        // This adds some extra reaching into NBT internals.
        if (tag instanceof NBTTagCompound) {
            NBTTagCompound compound = (NBTTagCompound)tag;
            Collection<String> keys = getAllKeys(compound);

            // Check for Map, ConfigurationSection or SerliazebleObject creation
            boolean isSerializedObject = compound.hasKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY);
            if (baseSection == null || isSerializedObject) {
                Map<String, Object> dataMap = new HashMap<String, Object>();
                for (String tagKey : keys) {
                    dataMap.put(tagKey, convert(compound.get(tagKey), tagKey, null));
                }
                if (isSerializedObject) {
                    try {
                        value = ConfigurationSerialization.deserializeObject(dataMap);
                        if (value == null) {
                            throw new IllegalArgumentException("Failed to deserialize object of class " + compound.get(ConfigurationSerialization.SERIALIZED_TYPE_KEY));
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        throw new IllegalArgumentException("Failed to deserialize object of class " + compound.get(ConfigurationSerialization.SERIALIZED_TYPE_KEY) + ", " + ex.getMessage());
                    }
                } else {
                    value = dataMap;
                }
            } else {
                ConfigurationSection newSection = baseSection.createSection(key);
                for (String tagKey : keys) {
                    newSection.set(tagKey, convert(compound.get(tagKey), tagKey, newSection));
                }
                value = newSection;
            }
        } else if (tag instanceof NBTTagString) {
            value = ((NBTTagString) tag).a_();
        } else if (tag instanceof NBTTagList) {
            NBTTagList list = (NBTTagList)tag;
            int tagSize = list.size();
            List<Object> convertedList = new ArrayList<Object>(tagSize);
            int listType = list.f();
            for (int i = 0; i < tagSize; i++) {
                // Convert to appropriate NBT object type
                Object listValue = null;
                switch (listType) {
                    case 10: // TagCompound
                        listValue = convert(list.get(i), null, null);
                        break;
                    case 1: // Byte
                    case 2: // Short
                    case 3: // Int
                    case 4: // Long
                        // I don't think this is going to work.
                        listValue = list.e(i);
                        break;
                    case 6: // Double
                    case 5: // Float
                        listValue = list.e(i);
                        break;
                    case 7: // Byte array
                        int[] intArray = list.c(i);
                        byte[] byteArray = new byte[intArray.length];
                        for (int arrayIndex = 0; arrayIndex < intArray.length; arrayIndex++) {
                            byteArray[arrayIndex] = (byte)intArray[arrayIndex];
                        }
                        listValue = byteArray;
                        break;
                    case 8: // String;
                        listValue = list.getString(i);
                        break;
                    case 9: // List;
                        // We don't support nested lists.
                        listValue = null;
                        break;
                    case 11: // Int array
                        listValue = list.c(i);
                        break;
                }
                if (listValue != null) {
                    convertedList.add(listValue);
                }
            }
            value = convertedList;
        } else if (tag instanceof NBTTagDouble) {
            value = ((NBTTagDouble)tag).g();
        } else if (tag instanceof NBTTagInt) {
            value = ((NBTTagInt)tag).d();
        } else if (tag instanceof NBTTagLong) {
            value = ((NBTTagLong)tag).c();
        } else if (tag instanceof NBTTagFloat) {
            value = ((NBTTagFloat)tag).h();
        } else if (tag instanceof NBTTagByte) {
            value = ((NBTTagByte)tag).f();
        } else if (tag instanceof NBTTagShort) {
            return ((NBTTagShort)tag).e();
        } else if (tag instanceof NBTTagByteArray) {
            value = ((NBTTagByteArray)tag).c();
        } else if (tag instanceof NBTTagIntArray) {
            value = ((NBTTagIntArray)tag).c();
        }

        return value;
    }

    /**
     * Convert an object to an NBTBase object. This creates a copy of the
     * input and wraps it in the appropriate NBT class.
     *
     * @param value The value to copy and wrap
     * @return An NBTBase representation of the input
     */
    @SuppressWarnings("unchecked")
    private static NBTBase convert(Object value) {
        if (value == null) return null;

        NBTBase copiedValue = null;
        if (value instanceof ConfigurationSection) {
            NBTTagCompound subtag = new NBTTagCompound();
            Map<String, Object> sectionMap = copyRoot((ConfigurationSection) value);
            applyToItem(subtag, sectionMap, false);
            copiedValue = subtag;
        } else if (value instanceof Map) {
            NBTTagCompound subtag = new NBTTagCompound();
            applyToItem(subtag, (Map<String, Object>)value, false);
            copiedValue = subtag;
        } else if (value instanceof String) {
            copiedValue = new NBTTagString((String)value);
        } else if (value instanceof Integer) {
            copiedValue = new NBTTagInt((Integer)value);
        } else if (value instanceof Float) {
            copiedValue = new NBTTagFloat((Float)value);
        } else if (value instanceof Double) {
            copiedValue = new NBTTagDouble((Double)value);
        } else if (value instanceof Byte) {
            copiedValue = new NBTTagByte((Byte)value);
        } else if (value instanceof Short) {
            copiedValue = new NBTTagShort((Short)value);
        } else if (value instanceof List) {
            NBTTagList tagList = new NBTTagList();
            List<Object> list = (List<Object>)value;
            for (Object listValue : list) {
                tagList.add(convert(listValue));
            }
            copiedValue = tagList;
        } else if (value.getClass().isArray()) {
            Class<?> arrayType = value.getClass().getComponentType();
            // I suppose you could convert Byte[], Integer[] here ... Long, Float, etc for that matter.
            if (arrayType == Byte.TYPE) {
                copiedValue = new NBTTagByteArray((byte[]) value);
            } else if (arrayType == Integer.TYPE) {
                copiedValue = new NBTTagIntArray((int[]) value);
            }
        } else if (value instanceof ConfigurationSerializable) {
            ConfigurationSerializable serializable = (ConfigurationSerializable)value;
            Map<String, Object> serializedMap = new HashMap<String, Object>();
            serializedMap.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(serializable.getClass()));
            serializedMap.putAll(serializable.serialize());
            NBTTagCompound subtag = new NBTTagCompound();
            applyToItem(subtag, serializedMap, false);
            copiedValue = subtag;
        } else {
            throw new IllegalArgumentException("Can't store objects of type " + value.getClass().getName());
        }

        return copiedValue;
    }

    /**
     * Retrieve all keys for a tag.
     * <p>
     * This is a simple wrapper for the obfuscated c() method
     *
     * @param tag The NBTTagCompound to list keys
     * @return A Set of keys from the tag, or null on null input.
     */
    protected static Set<String> getAllKeys(NBTTagCompound tag) {
        if (tag == null) return null;
        // TODO: Deobfuscate c() and remove the wrapper?
        return tag.c();
    }

    /**
     * Return a list of custom tags found on the specified NBTTag.
     * <p>
     * If there are no tags or no custom tags, this will return null.
     *
     * @param tag The NBTTagCompound to search for custom keys
     * @return A Collection of custom keys, or null if none were found
     */
    protected static Collection<String> getCustomKeys(NBTTagCompound tag) {
        Set<String> keys = getAllKeys(tag);
        if (keys == null) return null;

        Collection<String> customKeys = null;
        for (String key : keys) {
            // Skip over auto-registered NBT tags
            if (CraftMetaItem.ItemMetaKey.NBT_TAGS.contains(key) || CraftMetaItem.SerializableMeta.TYPE_FIELD.equals(key)) {
                continue;
            }
            if (customKeys == null) {
                customKeys = new ArrayList<String>();
            }
            customKeys.add(key);
        }

        return customKeys;
    }

    /**
     * Return a list of custom tags found in the specified Map.
     * <p>
     * This filters out Bukkit tags (not NBT tags), they differ
     * in some cases- see ItemMetaKey for details.
     * <p>
     * If there are no tags or no custom tags, this will return null.
     *
     * @param from The Map to search for custom keys
     * @return A Collection of custom keys, or null if none were found
     */
    @SuppressWarnings("unchecked")
    private static Collection<String> getCustomKeys(Map<String, Object> from) {
        if (from == null) return null;

        Collection<String> keys = null;
        for (Map.Entry<String, Object> entry : from.entrySet()) {
            String key = entry.getKey();
            // Skip over well-known tags, but only at the root level.
            // Skip Bukkit and NBT names, to avoid forward-compatibility issues
            if (CraftMetaItem.ItemMetaKey.BUKKIT_TAGS.contains(key) || CraftMetaItem.ItemMetaKey.NBT_TAGS.contains(key) || CraftMetaItem.SerializableMeta.TYPE_FIELD.equals(key)) {
                continue;
            }

            // Skip over this as it will be passed in when deserializing
            if (key.equals(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
               continue;
            }
            if (keys == null) {
                keys = new ArrayList<String>();
            }
            keys.add(key);
        }

        return keys;
    }

    /**
     * Apply a Map of data to an item's NBTTag
     * <p>
     * Will throw an IllegalArgumentException if providing
     * a non-ConfigurationSerializable object, or if
     * trying to override a well-known root key when
     * filterRegistered is true.
     *
     * @param itemTag The tag for which to apply data.
     * @param data The data to apply
     * @param filterRegistered if true, an IllegalArgumentException
     *    when trying to override a well-known tag name
     */
    private static void applyToItem(NBTTagCompound itemTag, Map<String, Object> data, boolean filterRegistered) {
       if (itemTag == null || data == null) return;

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            if (filterRegistered && (CraftMetaItem.ItemMetaKey.NBT_TAGS.contains(key) || CraftMetaItem.SerializableMeta.TYPE_FIELD.equals(key))) {
                throw new IllegalArgumentException("Can not customize key: " + key);
            }
            NBTBase copiedValue = convert(entry.getValue());
            if (copiedValue != null) {
                itemTag.set(key, copiedValue);
            } else {
                itemTag.remove(key);
            }
        }
    }

    /**
     * Does a deep copy from a Map to a ConfigurationSeciton.
     *
     * @param to The ConfigurationSection to copy data to
     * @param from The Map to copy data from
     */
    private static void apply(ConfigurationSection to, Map<String, Object> from) {
        if (from == null) return;

        apply(to, from, from.keySet());
    }

    /**
     * Does a deep copy from a Map to this object.
     * <p>
     * Can be used to filter out unwanted keys.
     *
     * @param from The Map to copy from
     * @param keys The specific keys to copy, must be provided
     */
    private static void apply(ConfigurationSection to, Map<String, Object> from, Collection<String> keys) {
        if (to == null || from == null ||keys == null) return;

        for (String key : keys) {
            Object value = from.get(key);
            if (value != null) {
                if (value instanceof ConfigurationSection) {
                    ConfigurationSection originalSection = (ConfigurationSection)value;
                    ConfigurationSection newSection = to.createSection(key);
                    apply(newSection, copyRoot(originalSection));
                    value = newSection;
                } else if (value instanceof List) {
                    value = new ArrayList<Object>((List<Object>) value);
                } else if (value.getClass().isArray()) {
                    Object[] originalArray = (Object[])value;
                    Class arrayType = value.getClass().getComponentType();
                    value = (Object[])java.lang.reflect.Array.newInstance(arrayType, originalArray.length);
                    System.arraycopy(originalArray, 0, value, 0, originalArray.length);
                } else if (value instanceof Map) {
                    Map<String, Object> originalMap = (Map<String, Object>)value;
                    // Note that we don't do a deep-copy of Map contents
                    value = new HashMap<String, Object>(originalMap);
                }
            }
            to.set(key, value);
        }
    }

    /**
     * Converts the root of a ConfigurationSection to a Map.
     *
     * @param section The ConfigurationSection to convert.
     * @return A copy of this configuration section as a Map.
     */
    private static Map<String, Object> copyRoot(ConfigurationSection section) {
        Collection<String> keys = section.getKeys(false);
        Map<String, Object> sectionMap = new HashMap<String, Object>(keys.size());
        for (String key : keys) {
            Object value = section.get(key);
            if (value instanceof ConfigurationSection) {
                value = copyRoot((ConfigurationSection)value);
            }

            sectionMap.put(key, value);
        }
        return sectionMap;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof CraftMetaItemData ? ((CraftMetaItemData)other).map.equals(this.map) : false;
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }
}
