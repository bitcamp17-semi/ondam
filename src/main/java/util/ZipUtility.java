package util;
import data.dto.FilesDto;
import data.service.ObjectStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;
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
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            for (FilesDto file : files) {
                String objectKey = "dataroom/" + file.getPath(); // 수정된 부분
                InputStream fileInputStream = objectStorageService.downloadFile(objectKey);
                if (fileInputStream == null) {
                    System.err.println("⚠️ 다운로드 실패: " + objectKey);
                    continue;
                }

                ZipEntry zipEntry = new ZipEntry(file.getName());
                zos.putNextEntry(zipEntry);

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
