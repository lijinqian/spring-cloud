/*
 * Copyright (c) 2017-2019, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.controller;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import net.coobird.thumbnailator.Thumbnails;
import tcsyn.basic.controller.AbstractController;
import tcsyn.basic.util.TextUtil;

/**
 * 
 * @author Ares S
 * @date 2020年6月12日
 */
public abstract class AbstractAppController extends AbstractController {
	
	Logger logger = Logger.getLogger(getClass());
	
	/**
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	protected String uploadFile(MultipartFile file, String parentPath) {
		File fileDir = new File(super.getRequest().getServletContext().getRealPath("/"));
		String rootPath = fileDir.getParentFile().getParentFile().getAbsolutePath();
		String parentDir = "files/" + parentPath;
		File rootFile = new File(rootPath, parentDir);
		if (!rootFile.exists()) {
			rootFile.mkdirs();
		}
		
		String fileName = file.getOriginalFilename();
		fileName = TextUtil.generateId();
		File imgFile = new File(rootFile.getAbsolutePath() + File.separator + fileName);
		try {
			Thumbnails.of(file.getInputStream()).scale(1f).outputFormat("jpg").outputQuality(0.75f).toFile(imgFile);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		
		String path = super.getRequest().getContextPath();
		int port = super.getRequest().getServerPort();
		String portStr = null;
		if (port != 80 && port != 443) {
			portStr = ":" + port;
		}
		String basePath = super.getRequest().getScheme() + "://" + super.getRequest().getServerName() + portStr + path + "/";
		String fileUrl = basePath + parentDir + "/" + fileName + ".jpg";
		return fileUrl;
	} 
}
