package com.moh.yehia.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ETLService implements CommandLineRunner {
    private final VectorStore vectorStore;

    private static final Logger log = LoggerFactory.getLogger(ETLService.class);

    @Value("classpath:spring-boot-reference.pdf")
    private Resource resource;

    public ETLService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(String... args) {
//        loadUsingDocumentReader();
    }

    private void loadUsingDocumentReader() {
        /*
         * this is a basic ETL pipeline using the below classes:
         * DocumentReader => PagePdfDocumentReader
         * DocumentTransformer => TextSplitter
         * DocumentWriter => VectorStore
         * */
        log.info("Start Embedding the document");
        PagePdfDocumentReader pagePdfDocumentReader = new PagePdfDocumentReader(resource, PdfDocumentReaderConfig.builder()
                .withPageTopMargin(0)
                .withPageExtractedTextFormatter(ExtractedTextFormatter.defaults())
//                .withPagesPerDocument(1)
                .build());
        // split the PDF file contents into chunks
        TextSplitter textSplitter = TokenTextSplitter.builder().build();
        List<Document> documents = pagePdfDocumentReader.read();
        log.info("documents before splitting: {}", documents.size());
        List<Document> documentsAfterSplitting = textSplitter.split(documents);
        log.info("documents after splitting: {}", documentsAfterSplitting.size());
        vectorStore.accept(documentsAfterSplitting);
        log.info("Reference loaded successfully");
    }
}
