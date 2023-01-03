package com.kantar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.kantar.service.KafkaSender;

@SpringBootApplication
public class KantarApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(KantarApplication.class, args);
	}

	@Autowired
    private KafkaSender KafkaSender;

    @Override
    public void run(String... strings) throws Exception {
        KafkaSender.send(123.123);
    }
}
