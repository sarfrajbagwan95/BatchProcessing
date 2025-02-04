package com.example.springbatch.mapper;

import com.example.springbatch.model.Customer;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CustomerFieldSetMapper implements FieldSetMapper<Customer> {

        @Override
        public Customer mapFieldSet(FieldSet fieldSet) {
            Customer customer = new Customer();
            customer.setIndex(fieldSet.readString("index"));
            customer.setCustomerId(fieldSet.readString("customerId"));
            customer.setFirstName(fieldSet.readString("firstName"));
            customer.setLastName(fieldSet.readString("lastName"));
            customer.setCompany(fieldSet.readString("company"));
            customer.setCity(fieldSet.readString("city"));
            customer.setCountry(fieldSet.readString("country"));
            customer.setPhone1(fieldSet.readString("phone1"));
            customer.setPhone2(fieldSet.readString("phone2"));
            customer.setEmail(fieldSet.readString("email"));
            customer.setSubscriptionDate(parseDate(fieldSet.readString("subscriptionDate")));
            customer.setWebsite(fieldSet.readString("website"));
            return customer;
        }

        private String parseDate(String subscriptionDate) {
            try {
                LocalDate date = LocalDate.parse(subscriptionDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                return date.toString();
            } catch (DateTimeParseException e) {
                return null;
            }
        }
    }

