package util;
import data.dto.FilesDto;
import data.service.ObjectStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class ZipUtility {

    private final ObjectStorageService objectStorageService;

    @Autowired
    public ZipUtility(ObjectStorageService objectStorageService) {
        this.objectStorageService = objectStorageService;
    }

    public File createZip(List<FilesDto> files, String zipFileName) throws IOException {
        File zipFile = File.createTempFile(zipFileName, ".zip");
        Map<String, Integer> nameCountMap = new HashMap<>(); // 파일명 중복 확인용 맵

        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            for (FilesDto file : files) {
                String originalName = file.getName(); // 원본 파일명
                String uniqueName = originalName; // 중복 확인을 위한 고유 이름

                // 파일명이 중복되면 숫자 붙여서 이름 변경
                if (nameCountMap.containsKey(originalName)) {
                    int count = nameCountMap.get(originalName) + 1;
                    nameCountMap.put(originalName, count);

                    int extensionIndex = originalName.lastIndexOf(".");
                    if (extensionIndex > 0) {
                        String namePart = originalName.substring(0, extensionIndex); // 확장자 앞 부분
                        String extensionPart = originalName.substring(extensionIndex); // 확장자 부분
                        uniqueName = namePart + "(" + count + ")" + extensionPart; // 이름에 숫자 추가
                    } else {
                        uniqueName = originalName + "(" + count + ")"; // 확장자가 없는 경우
                    }
                } else {
                    nameCountMap.put(originalName, 0); // 처음 등장한 파일명은 카운트 0으로 시작
                }

                // 압축 파일 내에 이름 설정
                ZipEntry zipEntry = new ZipEntry(uniqueName);
                zos.putNextEntry(zipEntry);

                // 파일을 읽어서 zip에 쓴다.
                InputStream fileInputStream = objectStorageService.downloadFile("dataroom/" + file.getPath());
                if (fileInputStream == null) {
                    System.err.println("⚠️ 다운로드 실패: " + file.getPath());
                    continue;
                }

                byte[] buffer = new byte[1024];
                int length;
                while ((length = fileInputStream.read(buffer)) >= 0) {
                    zos.write(buffer, 0, length);
                }

                zos.closeEntry();
                fileInputStream.close();
            }

        }
        return zipFile;
    }


}
