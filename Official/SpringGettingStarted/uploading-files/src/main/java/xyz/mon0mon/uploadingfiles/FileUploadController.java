package xyz.mon0mon.uploadingfiles;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import xyz.mon0mon.uploadingfiles.storage.StorageFileNotFoundException;
import xyz.mon0mon.uploadingfiles.storage.StorageService;

import java.io.IOException;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class FileUploadController {
    private final StorageService storageService;

    @GetMapping
    public String listUploadedFiles(Model model) throws IOException {

        model.addAttribute("files", storageService.loadAll().map(path ->
                        MvcUriComponentsBuilder.fromMethodName(
                                        FileUploadController.class,
                                        "serveFile",
                                        path.getFileName().toString())
                                .build().toUri().toString())
                .collect(Collectors.toList()));

        return "uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        Resource file = storageService.loadAsResource(filename);

        if (file == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ("attachment; filename=\"" + file.getFilename() + "\""))
                .build();
    }

    @PostMapping
    public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        storageService.store(file);

        redirectAttributes.addFlashAttribute("message", "You successfully uploaded "
                + file.getOriginalFilename() + "!");

        return "redirect:/";
    }

    @ExceptionHandler
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }
}
