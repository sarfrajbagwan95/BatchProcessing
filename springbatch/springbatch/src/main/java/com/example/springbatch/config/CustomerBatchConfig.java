package com.example.springbatch.config;

import com.example.springbatch.mapper.CustomerFieldSetMapper;
import com.example.springbatch.model.Customer;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.logging.Logger;
@Configuration
@EnableBatchProcessing
public class CustomerBatchConfig {
    private static final Logger logger = Logger.getLogger(CustomerBatchConfig.class.getName());

    private static final String sql = "INSERT INTO customers " +
            "(`index`, customer_id, first_name, last_name, company, city, country, phone1, phone2, email, subscription_date, website) " +
            "VALUES (:index, :customerId, :firstName, :lastName, :company, :city, :country, :phone1, :phone2, :email, :subscriptionDate, :website)";

    @Bean
    @StepScope
    public FlatFileItemReader<Customer> reader(@Value("#{jobParameters[filePath]}") String filePath) {
        return new FlatFileItemReaderBuilder<Customer>()
                .name("customerItemReader")
                .resource(new FileSystemResource(filePath))
                .delimited()
                .names("index", "customerId", "firstName", "lastName", "company", "city", "country", "phone1", "phone2", "email", "subscriptionDate", "website")
                .fieldSetMapper(new CustomerFieldSetMapper())  // Using the extracted mapper
                .build();
    }


    @Bean
    public JdbcBatchItemWriter<Customer> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Customer>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql(sql)
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository, ItemReader<Customer> reader, JdbcBatchItemWriter<Customer> writer, PlatformTransactionManager transactionManager) {
        return new StepBuilder("csv-step", jobRepository)
                .<Customer, Customer>chunk(10, transactionManager)
                .reader(reader)
                .writer(writer)
                .listener(new StepExecutionListener() {
                    @Override
                    public void beforeStep(StepExecution stepExecution) {
                        logger.info("Batch processing start : " + LocalDateTime.now());
                    }

                    @Override
                    public ExitStatus afterStep(StepExecution stepExecution) {
                        logger.info("Batch processing end : " + LocalDateTime.now());
                        return ExitStatus.COMPLETED;
                    }
                })
                .build();
    }


    @Bean
    public Job importUserJob(JobRepository jobRepository, Step step1) {
        return new JobBuilder("importCustomerJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step1)
                .build();
    }
}
