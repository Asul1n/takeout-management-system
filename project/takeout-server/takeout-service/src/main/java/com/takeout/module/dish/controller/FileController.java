package com.takeout.module.dish.controller;

import com.takeout.common.exception.BusinessException;
import com.takeout.common.result.Result;
import com.takeout.framework.security.UserDetailsImpl;
import com.takeout.module.dish.service.DishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import cn.hutool.core.io.FileUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Tag(name = "文件上传接口")
@RestController
@RequestMapping("/api/v1/merchant/dishes")
@RequiredArgsConstructor
public class FileController {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private final DishService dishService;

    @Operation(summary = "商家上传菜品图片")
    @PostMapping("/{id}/image")
    @PreAuthorize("hasRole('MERCHANT')")
    public Result<String> uploadImage(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                       @PathVariable Long id,
                                       @RequestParam("file") MultipartFile file) throws IOException {
        // 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException("仅支持 JPG/PNG/WebP 格式");
        }

        // 校验文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小不能超过 5MB");
        }

        // 生成文件名和路径
        Long merchantId = userDetails.getUserId();
        String ext = FileUtil.extName(file.getOriginalFilename());
        String filename = LocalDateTime.now().format(FORMATTER) + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + ext;
        Path dir = Path.of("uploads", "dishes", String.valueOf(merchantId));
        Files.createDirectories(dir);

        // 保存文件
        file.transferTo(dir.resolve(filename));

        // 更新菜品 image_url
        String imageUrl = "/static/dishes/" + merchantId + "/" + filename;
        dishService.updateImageUrl(id, imageUrl);

        return Result.ok(imageUrl);
    }
}
