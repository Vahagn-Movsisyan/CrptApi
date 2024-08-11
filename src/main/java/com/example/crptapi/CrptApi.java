package com.example.crptapi;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CrptApi {

	private static final String API_URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";
	private final HttpClient httpClient;
	private final ObjectMapper objectMapper;
	private final Semaphore semaphore;
	private final ScheduledExecutorService scheduler;

	public CrptApi(TimeUnit timeUnit, int requestLimit) {
		this.httpClient = HttpClient.newHttpClient();
		this.objectMapper = new ObjectMapper();
		this.semaphore = new Semaphore(requestLimit);
		this.scheduler = Executors.newScheduledThreadPool(1);

		scheduler.scheduleAtFixedRate(() -> semaphore.release(requestLimit - semaphore.availablePermits()),
				0, 1, timeUnit);
	}

	public void createDocument(Document document, String signature) throws InterruptedException, IOException {
		semaphore.acquire();

		String jsonDocument = objectMapper.writeValueAsString(document);

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(API_URL))
				.header("Content-Type", "application/json")
				.header("Signature", signature)
				.POST(HttpRequest.BodyPublishers.ofString(jsonDocument))
				.build();

		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

		System.out.println("Response status code: " + response.statusCode());
		System.out.println("Response body: " + response.body());
	}

	public static class Document {
		public Description description;
		public String doc_id;
		public String doc_status;
		public String doc_type;
		public boolean importRequest;
		public String owner_inn;
		public String participant_inn;
		public String producer_inn;
		public String production_date;
		public String production_type;
		public Product[] products;
		public String reg_date;
		public String reg_number;

		public static class Description {
			public String participantInn;
		}

		public static class Product {
			public String certificate_document;
			public String certificate_document_date;
			public String certificate_document_number;
			public String owner_inn;
			public String producer_inn;
			public String production_date;
			public String tnved_code;
			public String uit_code;
			public String uitu_code;
		}
	}
}
