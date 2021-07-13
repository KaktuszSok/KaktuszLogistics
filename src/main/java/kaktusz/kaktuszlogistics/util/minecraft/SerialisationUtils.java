package kaktusz.kaktuszlogistics.util.minecraft;

import kaktusz.kaktuszlogistics.util.CastingUtils;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SerialisationUtils {

	public static <T> byte[] serialiseToBytes(T serialisable) {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		try (BukkitObjectOutputStream bukkitStream = new BukkitObjectOutputStream(byteStream)) {
			bukkitStream.writeObject(serialisable); //write serialisable
		} catch (IOException e) {
			e.printStackTrace();
		}
		return byteStream.toByteArray();
	}

	public static <T> byte[] serialisablesToBytes(List<T> serialisables) {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		try (BukkitObjectOutputStream bukkitStream = new BukkitObjectOutputStream(byteStream)) {
			bukkitStream.writeInt(serialisables.size()); //write int
			for(T serialisable : serialisables) {
				bukkitStream.writeObject(serialisable); //write serialisable (size times)
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return byteStream.toByteArray();
	}

	public static <T> T deserialiseFromBytes(byte[] bytes) {
		ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
		try(BukkitObjectInputStream bukkitStream = new BukkitObjectInputStream(byteStream)) {
			return CastingUtils.confidentCast(bukkitStream.readObject());
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static <T> List<T> serialisablesFromBytes(byte[] bytes) {
		List<T> result = new ArrayList<>();

		ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
		try(BukkitObjectInputStream bukkitStream = new BukkitObjectInputStream(byteStream)) {
			int size = bukkitStream.readInt(); //read int
			for(int i = 0; i < size; i++) {
				result.add(CastingUtils.confidentCast(bukkitStream.readObject())); //read serialisable (size times)
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		return result;
	}
}
