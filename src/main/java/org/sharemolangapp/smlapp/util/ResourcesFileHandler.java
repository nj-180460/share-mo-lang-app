package org.sharemolangapp.smlapp.util;


import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;


public final class ResourcesFileHandler {
	
	private final LinkedHashMap<String, Path> dirMap;
	
	
	public ResourcesFileHandler() {
		dirMap = new LinkedHashMap<>();
		dirMap.put(ConfigConstant.JSON_DIR, Paths.get(ConfigConstant.getJsonRelativePath(null)));
	}
	
	
	
	public boolean copyResourcesToLocal() throws IOException {
		try {
			
			createDirectories(Paths.get(ConfigConstant.getJsonRelativePath(null)));
			if(createDirectories(Paths.get(ConfigConstant.LOCAL_DEFAULT_OUTPUT_DIR.toString()))) {
				
				// create directories to local machine disk
				dirMap.entrySet().stream()
					.map(Map.Entry::getValue)
					.collect(Collectors.toList())
					.forEach(dirPath -> createDirectories(dirPath));
				
				// copy all resources with their respective folders in local machine disk
				for(Map.Entry<String, Path> entry: dirMap.entrySet()) {
					Path dirPath = entry.getValue();
					String resourcePath = entry.getKey();
					if(resourcePath.equals(ConfigConstant.JSON_DIR)) {
						copyJsonResourcesToLocal(resourcePath, dirPath);
					}
				}
				
				return true;
			}
			
		} catch(IOException | URISyntaxException ioException) {
			ioException.printStackTrace();
			throw new IOException(ioException);
		}
		return false;
	}
	
	
	public static boolean createDir(String dir) {
		boolean isCreated = false;
		try {
			Files.createDirectories(Paths.get(dir));
			isCreated = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return isCreated;
	} 
	
	
	public static Path getResourceFile(String resourceFilename) throws URISyntaxException {
		return Paths.get(ResourcesFileHandler.class.getClassLoader().getResource(resourceFilename).toURI());
	}
	
	
	public static InputStream getResourceFileStream(String resourceFilename) throws IOException {
		return ResourcesFileHandler.class.getClassLoader().getResourceAsStream(resourceFilename);
	}
	
	
	public Path getResourceFile0(String resourceFilename) throws URISyntaxException {
		return Paths.get(getClass().getResource(resourceFilename).toURI());
	}
	
	
	public InputStream getResourceFileStream0(String resourceFilename) throws IOException {
		return getClass().getResourceAsStream(resourceFilename);
	}
	
	
	private boolean createDirectories(Path localDIR) {
		boolean isCreated = false;
		try {
			Files.createDirectories(localDIR);
			isCreated = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return isCreated;
	}
	
	
	/**
	 * Do nothing pag yaon na json file na nag i-exist local.
	 * Your only option is to delete that file from local and run the program again
	 * @param orResourcePathFile - the resource path file
	 * @param localPath - local path (machine path/directory)
	 * @param file - the file you want to copy to
	 * @throws URISyntaxException 
	 * @throws NoSuchAlgorithmException 
	 * */
	private boolean copyResourceToLocal(InputStream sourceInputStream, Path orResourcePathFile, Path localPath, String file)
			throws IOException, URISyntaxException {
		
		Path localPathFile = localPath.resolve(file);
		
		// create file if it's not exist. copy resource content to newly created file
		if(!Files.exists(localPathFile)) {
			
			localPathFile = Files.createFile(localPathFile);
			
			if(sourceInputStream != null) {
        		Files.copy(sourceInputStream, localPathFile, StandardCopyOption.REPLACE_EXISTING);
        	} else if(orResourcePathFile != null) {
        		Files.copy(orResourcePathFile, localPathFile, StandardCopyOption.REPLACE_EXISTING);
        	}
			
			defaultValue(localPathFile);
			
		}
		
		return Files.exists(localPathFile);
	}
	
	
	
	private void defaultValue(Path localPathFile) {
		JSONFactory.Settings.setJsonParentValue(
				ConfigConstant.getJsonRelativePath(ConfigConstant.PREFERENCES_JSON_FILE),
				JSONFactory.Settings.OUTPUT_FOLDER_PNODE,
				ConfigConstant.LOCAL_DEFAULT_OUTPUT_DIR.toString());
		JSONFactory.Settings.setJsonParentValue(
				ConfigConstant.getJsonRelativePath(ConfigConstant.PREFERENCES_JSON_FILE),
				JSONFactory.Settings.YOUR_NAME_PNODE,
				NetworkUtility.getMachineIPv4Address().getHostName());
	}
	
	
	
	
	private InputStream getResourcePathFileStream(String resourcePath, String resourcePathFile)
			throws URISyntaxException{
		StringBuilder resourcePathSB = new StringBuilder();
		resourcePathSB.append(resourcePath);
		resourcePathSB.append(resourcePathFile);
		return ResourcesFileHandler.class.getClassLoader().getResourceAsStream(resourcePathSB.toString());
	}
	
	
	
	public void copyJsonResourcesToLocal(String resourcePath, Path localPath) 
			throws IOException, URISyntaxException{
		ArrayList<String> jsonFileList = new ArrayList<>();
		jsonFileList.add(ConfigConstant.PREFERENCES_JSON_FILE);
		
		for(String jsonFile : jsonFileList) {
			copyResourceToLocal(getResourcePathFileStream(resourcePath, jsonFile), null, localPath, jsonFile);
		}
	}

	
	
	
	
//	public void copyImageResourcesToLocal(String resourcePath, Path localPath) 
//			throws IOException, URISyntaxException, NoSuchAlgorithmException {
//		ArrayList<String> imageFileList = new ArrayList<>();
//		imageFileList.add(ConfigConstant.LAOANG_LOGO_FILE);
//		imageFileList.add(ConfigConstant.QRCODE_IMAGE_BACKGROUND_FILE);
//		imageFileList.add(ConfigConstant.VACCINATION_CARD_IMAGE_FILE);
//		imageFileList.add(ConfigConstant.IC_REPORT_FILE);
//		imageFileList.add(ConfigConstant.IC_ADD_VACCINEE_FILE);
//		imageFileList.add(ConfigConstant.IC_SEARCH_FILE);
//		imageFileList.add(ConfigConstant.IC_SIGNIN_FILE);
//		for(String imageFile : imageFileList) {
//			copyResourceToLocal(getResourcePathFileStream(resourcePath, imageFile), null, localPath, imageFile);
//		}
//	}
	
}
