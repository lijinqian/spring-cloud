package com.provider;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class UploadController {
	
	@RequestMapping(value = "/upload", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseBody
	public String uploadFile(@RequestPart("file") MultipartFile file ) throws IOException {
		byte[] bytes = file.getBytes();
		System.out.println(bytes.length);
		return file.getOriginalFilename();
	}
}
