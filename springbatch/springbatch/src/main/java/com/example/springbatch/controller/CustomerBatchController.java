package com.example.springbatch.controller;
import com.example.springbatch.exception.FileUploadException;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/batch")
public class CustomerBatchController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job job;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {

        String uploadDir = System.getProperty("user.dir") + "/uploads";
        File dir = new File(uploadDir);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filePath = uploadDir + "/" + UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        File destFile = new File(filePath);

        try {
            file.transferTo(destFile);

            JobParameters parameters = new JobParametersBuilder()
                    .addString("filePath", destFile.getAbsolutePath())
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(job, parameters);

            String startTime = jobExecution.getStartTime().toString();
            String endTime = jobExecution.getEndTime() != null ? jobExecution.getEndTime().toString() : "Not completed";

            return ResponseEntity.ok("Batch processing started for : " + file.getOriginalFilename() +
                    "\n Start Time: " + startTime + "\n End Time: " + endTime);

        } catch (IOException | JobExecutionException e) {
            throw new FileUploadException("Error uploading file: " + e.getMessage());
        }

    }


    @DeleteMapping("/remove")
    public ResponseEntity<String> removeAllCustomers() {
        jdbcTemplate.execute("delete from customers");
        return ResponseEntity.ok("All customer records removed successfully!");
    }
}


