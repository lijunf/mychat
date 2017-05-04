package com.lucien.xtest;

public class Calculate {

	public static void main(String[] args) {
		int capital = 10000;
		for (int i = 0; i < 3; i++) {
			capital += capital * 0.0297;
		}
		System.out.println(capital);
	}
}
