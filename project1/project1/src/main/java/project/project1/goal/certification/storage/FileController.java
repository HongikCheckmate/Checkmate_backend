package project.project1.goal.certification.storage;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class FileController {
    @GetMapping("/certification/group_{groupId}/goal_{goalId}/{date}/user_{userId}/{fileName}")
    @ResponseBody
    public ResponseEntity<Resource> serveCertificationFile(
            @PathVariable("groupId") Long groupId,
            @PathVariable("goalId") Long goalId,
            @PathVariable("date") String date,
            @PathVariable("userId") Long userId,
            @PathVariable("fileName") String fileName) {

        String filePath = String.format(
                "C:/Users/Public/degree_project/certification/group_%d/goal_%d/%s/user_%d/%s",
                groupId, goalId, date, userId, fileName
        );

        try {
            Path path = Paths.get(filePath);
            UrlResource resource = new UrlResource(path.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body((Resource) resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.internalServerError().build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
