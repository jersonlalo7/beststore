package com.boostmytool.beststore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import com.boostmytool.beststore.repository.ProductsRepository;

@SpringBootApplication
public class BeststoreApplication {
	//sample comment on main class
	@Autowired
	private ProductsRepository repo;

	public static void main(String[] args) {
		SpringApplication.run(BeststoreApplication.class, args);
	}
	
	@EventListener(ApplicationReadyEvent.class)
    public void testRepo() {
        System.out.println(repo.findAll());
    }

}
