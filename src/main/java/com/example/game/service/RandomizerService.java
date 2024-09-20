package com.example.game.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@Slf4j
public class RandomizerService {

	public int generateRandomNumber() {
		return new Random().nextInt(10) + 1;
	}
}
