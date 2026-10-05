package com.rental.request;


import java.util.List;
import lombok.Data;


@Data
public class BrevoEmailRequest {

        private BrevoSender sender;

        private List<BrevoRecipient> to;

        private String subject;

        private String htmlContent;
    }

