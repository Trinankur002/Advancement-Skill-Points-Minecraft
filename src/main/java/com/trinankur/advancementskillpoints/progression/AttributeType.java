package com.trinankur.advancementskillpoints.progression;

import com.trinankur.advancementskillpoints.progression.attribute.ModAttributes;
import net.minecraft.resources.Identifier;

/**
 * Retained for backwards compatibility with Phase 1 references.
 */
public enum AttributeType {
	STRENGTH(ModAttributes.STRENGTH_ID),
	VITALITY(ModAttributes.VITALITY_ID),
	ENDURANCE(ModAttributes.ENDURANCE_ID),
	STAMINA(ModAttributes.STAMINA_ID);

	private final Identifier id;

	AttributeType(Identifier id) {
		this.id = id;
	}

	public Identifier getId() {
		return id;
	}

	public String getPath() {
		return id.getPath();
	}

	public static AttributeType fromId(String id) {
		if (id == null) return null;
		for (AttributeType type : values()) {
			if (type.name().equalsIgnoreCase(id) || type.id.getPath().equalsIgnoreCase(id) || type.id.toString().equalsIgnoreCase(id)) {
				return type;
			}
		}
		return null;
	}
}
