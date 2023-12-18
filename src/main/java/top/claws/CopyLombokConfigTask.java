/* (C)2023 */
package top.claws;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

/**
 * @author claws
 * @since 2023/12/18
 */
public class CopyLombokConfigTask extends DefaultTask {
    @TaskAction
    public void copyConfig() {
        try {
            File targetFile = new File(getProject().getRootDir(), "lombok.config");
            if (targetFile.exists()) {
                throw new RuntimeException(
                        "lombok.config has already existed. Please delete it first.");
            }

            URL resourceUrl = getClass().getClassLoader().getResource("lombok.config");
            if (resourceUrl == null) {
                throw new RuntimeException("lombok.config resource not found");
            }

            try (InputStream resourceStream = resourceUrl.openStream()) {
                try (FileOutputStream outStream = new FileOutputStream(targetFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = resourceStream.read(buffer)) > 0) {
                        outStream.write(buffer, 0, length);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error copying lombok.config", e);
        }
    }
}
