package org.apollo.fs.decoder;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apollo.fs.IndexedFileSystem;
import org.apollo.fs.archive.Archive;
import org.apollo.fs.def.ObjectDefinition;
import org.apollo.util.BufferUtil;

/**
 * Decodes object data from the {@code loc.dat} file into {@link ObjectDefinition}s.
 *
 * @author Major
 */
public final class ObjectDefinitionDecoder {

	/**
	 * The {@link IndexedFileSystem}.
	 */
	private final IndexedFileSystem fs;

	/**
	 * Creates the decoder.
	 *
	 * @param fs The {@link IndexedFileSystem}.
	 */
	public ObjectDefinitionDecoder(IndexedFileSystem fs) {
		this.fs = fs;
	}

	/**
	 * Decodes all of the data into {@link ObjectDefinition}s.
	 *
	 * @return The definitions.
	 * @throws IOException If an error occurs when decoding the archive or finding an entry.
	 */
	public ObjectDefinition[] decode() throws IOException {
		Archive config = Archive.decode(fs.getFile(0, 2));
		ByteBuffer data = config.getEntry("loc.dat").getBuffer();
		ByteBuffer idx = config.getEntry("loc.idx").getBuffer();

		int count = idx.getShort(), index = 2;
		int[] indices = new int[count];
		for (int i = 0; i < count; i++) {
			indices[i] = index;
			index += idx.getShort();
		}

		ObjectDefinition[] defs = new ObjectDefinition[count];
		for (int i = 0; i < count; i++) {
			data.position(indices[i]);
			defs[i] = decode(i, data);
		}
		return defs;
	}

	/**
	 * Decodes data from the cache into an {@link ObjectDefinition}.
	 *
	 * @param id The id of the object.
	 * @param data The {@link ByteBuffer} containing the data.
	 * @return The object definition.
	 */
	public static ObjectDefinition decode(int id, ByteBuffer data) {
		ObjectDefinition definition = new ObjectDefinition(id);
		while (true) {
			int opcode = data.get() & 0xFF;

			if (opcode == 0) {
				return definition;
			} else if (opcode == 1) {
				int amount = data.get() & 0xFF;
				for (int i = 0; i < amount; i++) {
					data.getShort();
					data.get();
				}
			} else if (opcode == 2) {
				definition.setName(BufferUtil.readString(data));
			} else if (opcode == 3) {
				definition.setDescription(BufferUtil.readString(data));
			} else if (opcode == 5) {
				int amount = data.get() & 0xFF;
				for (int i = 0; i < amount; i++) {
					data.getShort();
				}
			} else if (opcode == 14) {
				definition.setWidth(data.get() & 0xFF);
			} else if (opcode == 15) {
				definition.setLength(data.get() & 0xFF);
			} else if (opcode == 17) {
				definition.setSolid(false);
			} else if (opcode == 18) {
				definition.setImpenetrable(false);
			} else if (opcode == 19) {
				definition.setInteractive((data.get() & 0xFF) == 1);
			} else if (opcode == 24) {
				data.getShort();
			} else if (opcode == 28 || opcode == 29) {
				data.get();
			} else if (opcode >= 30 && opcode < 39) {
				String[] actions = definition.getMenuActions();
				if (actions == null) {
					actions = new String[10];
				}
				String action = BufferUtil.readString(data);
				actions[opcode - 30] = action;
				definition.setMenuActions(actions);
			} else if (opcode == 39) {
				data.get();
			} else if (opcode == 40) {
				int amount = data.get() & 0xFF;
				for (int i = 0; i < amount; i++) {
					data.getShort();
					data.getShort();
				}
			} else if (opcode == 60 || opcode >= 65 && opcode <= 68) {
				data.getShort();
			} else if (opcode == 69) {
				data.get();
			} else if (opcode >= 70 && opcode <= 72) {
				data.getShort();
			} else if (opcode == 73) {
				definition.setObstructive(true);
			} else if (opcode == 75) {
				data.get();
			} else if (opcode == 77) {
				data.getShort();
				data.getShort();
				int count = data.get();
				for (int i = 0; i <= count; i++) {
					data.getShort();
				}
			} else {
				continue;
			}
		}
	}

}