package com.nebula.commerce.infrastructure.web;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/file")
public class FileController {

    // 修改点：使用 ${user.dir} 获取当前项目运行目录
    // 默认路径变为：项目根目录/uploads/
    @Value("${nebula.upload.path:${user.dir}/uploads/}")
    private String uploadPath;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        // 1. 生成新文件名
        String originalFilename = file.getOriginalFilename();
        String suffix = FileUtil.extName(originalFilename);
        String fileUUID = IdUtil.simpleUUID();
        String datePath = LocalDate.now().toString(); // e.g., 2026-01-01
        String newFileName = fileUUID + "." + suffix;

        // 2. 构建存储路径
        File saveFile = new File(uploadPath + File.separator + datePath + File.separator + newFileName);

        // 打印绝对路径，方便你在控制台查看文件到底存哪去了
        log.info("文件即将保存到项目目录: {}", saveFile.getAbsolutePath());

        if (!saveFile.getParentFile().exists()) {
            saveFile.getParentFile().mkdirs();
        }

        // 3. 保存文件
        try {
            file.transferTo(saveFile);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败");
        }

        // 4. 生成完整的访问 URL
        // 逻辑：当前协议 + IP + 端口 + /images/ + 日期目录 + 文件名
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        String fileUrl = String.format("%s://%s:%d/images/%s/%s",
                scheme, serverName, serverPort, datePath, newFileName);

        return Result.success(fileUrl);
    }
}