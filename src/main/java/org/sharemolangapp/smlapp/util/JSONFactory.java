package org.sharemolangapp.smlapp.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;



public abstract class JSONFactory {
	
	
	
	private static final ObjectMapper objectMapper = new ObjectMapper();

	private JSONFactory() {
		throw new UnsupportedOperationException(getClass().getName() + " is unavailable. Use static methods only");
	}
	
	
	private static JsonNode getJsonNodeFile(String jsonFile) throws JsonProcessingException, IOException {
		JsonNode jsonNode = objectMapper.readTree(Paths.get(jsonFile).toFile());
		return jsonNode;
	}
	
	private static boolean setJsonNodeFile(JsonNode newJsonNodeContent, String localPathFile) {
		Path modifiedSettingsPath = Paths.get(localPathFile);
		try {
			Files.write(modifiedSettingsPath, newJsonNodeContent.toPrettyString().getBytes(StandardCharsets.UTF_8));
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	
	
	public static <K, V>LinkedHashMap<K, V> extractJsonStr(String jsonStr){
		return jsonMapper(jsonStr);
	}
	
	
	
	private static <T,V>LinkedHashMap<T,V> jsonMapper(Path json) {
		LinkedHashMap<T,V> genMap = new LinkedHashMap<T,V>();
		try {
			TypeReference<LinkedHashMap<T, V>> typeRef = new TypeReference<LinkedHashMap<T, V>>() {};
			genMap = objectMapper.readValue(json.toFile(), typeRef);
		} catch(Exception e){ 
			// TEMPORARY
			e.printStackTrace();
		}
		return genMap;
	}
	
	
	
	private static <T,V>LinkedHashMap<T,V> jsonMapper(String json) {
		LinkedHashMap<T,V> genMap = new LinkedHashMap<T,V>();
		try {
			TypeReference<LinkedHashMap<T, V>> typeRef = new TypeReference<LinkedHashMap<T, V>>() {};
			genMap = objectMapper.readValue(json, typeRef);
		} catch(Exception e){ 
			// TEMPORARY
			e.printStackTrace();
		}
		return genMap;
	}
	
	
	
	// settings class
	public abstract static class Settings{
		
		public static final String YOUR_NAME_PNODE = "yourName";
		public static final String OUTPUT_FOLDER_PNODE = "outputFolder";
		
		
		private Settings() {
			throw new UnsupportedOperationException(getClass().getName() + " is unavailable. Use static methods only");
		}
		
		
		private static JsonNode getSettings(String jsonPathFile) {
			JsonNode jsonNode = null;
			try {
				jsonNode = getJsonNodeFile(jsonPathFile);
			} catch (IOException e) {
				// TEMPORARY
				e.printStackTrace();
			}
			return jsonNode;
		}
		
		private static boolean setSettings(String jsonPathFile, JsonNode newSettingsJsonNode) {
			return setJsonNodeFile(newSettingsJsonNode, jsonPathFile);
		}
		
		
		
		public static ArrayList<String> getArrayOnObject(String jsonPathFile, String[] paths, String path) {
			JsonNode dosagesNode = getNodePath(paths, path, getSettings(jsonPathFile));
			List<String> dosagesList = 
					StreamSupport.stream(dosagesNode.spliterator(), false)
						.map( e -> e.asText() )
						.collect(Collectors.toList());
			return new ArrayList<>(dosagesList);
		}
		
		public static void setArrayOnObject(String jsonPathFile, String[]paths, String path, ArrayList<String> newList) {
			JsonNode locatedNode = getSettings(jsonPathFile);
			ArrayNode dosageArr = objectMapper.valueToTree(newList);
			getObjectNode(paths, (ObjectNode)locatedNode).set(path, dosageArr);;
			setSettings(jsonPathFile, locatedNode);
		}
		
		
		private static JsonNode getNodePath(String[] paths, String path, JsonNode locatedNode) {
			JsonNode lastPath = locatedNode;
			for(String p : paths) {
				lastPath = lastPath.path(p);
			}
			return lastPath.path(path);
		}
		
		private static ObjectNode getObjectNode(String[] paths, ObjectNode locatedNode) {
			ObjectNode lastPath = locatedNode;
			for(String path : paths) {
				lastPath = (ObjectNode)lastPath.get(path);
			}
			return lastPath;
		}
		
		
		
		
		/**
		 *  top/parent node
		 * @return parent json node
		 */
		public static String getJsonParentValue(String jsonPathFile, String parentNode) {
			return getSettings(jsonPathFile).get(parentNode).asText();
		}
		
		public static void setJsonParentValue(String jsonPathFile, String parentNode, String value) {
			JsonNode locatedNode = getSettings(jsonPathFile);
			((ObjectNode)locatedNode).put(parentNode, value);
			setSettings(jsonPathFile, locatedNode);
		}
		
		public static void removeJsonParentValue(String jsonPathFile, String parentNode) {
			JsonNode locatedNode = getSettings(jsonPathFile);
			((ObjectNode)locatedNode).remove(parentNode);
			setSettings(jsonPathFile, locatedNode);
		}
	}
}
