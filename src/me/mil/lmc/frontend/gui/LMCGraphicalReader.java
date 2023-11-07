package me.mil.lmc.frontend.gui;

import me.mil.lmc.LMCReader;

import java.util.concurrent.CompletableFuture;

public class LMCGraphicalReader implements LMCReader {

	private final LMCInterface lmcInterface;

	public LMCGraphicalReader(LMCInterface lmcInterface) {
		this.lmcInterface = lmcInterface;
	}

	@Override
	public int nextInt() {

		return promptForInt();
	}

	private int promptForInt() {
		return promptForInt("");
	}

	private int promptForInt(String msg) {
		try{
			return Integer.parseInt(getInterface().showInputDialog(msg));
		}catch (NumberFormatException e) {
			return promptForInt("Please provide an integer.");
		}
	}

	private LMCInterface getInterface() {
		return lmcInterface;
	}
}