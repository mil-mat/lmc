package me.mil.lmc.frontend.gui;

import me.mil.lmc.backend.LMCProcessor;
import me.mil.lmc.backend.exceptions.LMCCompilationException;
import me.mil.lmc.backend.exceptions.LMCRuntimeException;

import java.util.*;
import java.util.function.Consumer;

public enum ControlFunction {
	COMPILE(3, (lmcInterface) -> {
		try {
			lmcInterface.setProcessor(LMCProcessor.compileInstructions(lmcInterface.getInputPanel().getText(),
					lmcInterface.getControlPanel().getRequestedMemorySize(), lmcInterface.getControlPanel().getRequestedClockSpeed(),
					lmcInterface.getReader(), lmcInterface.getWriter()));

			lmcInterface.getMemoryViewPanel().resetMemoryUnits();

		} catch (LMCCompilationException e) {
			lmcInterface.showErrorDialog(e);
		}
	}),
	LOAD_INTO_RAM(1, lmcInterface -> {
		try {
			lmcInterface.getProcessor().loadInstructionsIntoMemory();
		} catch (LMCRuntimeException e) {
			lmcInterface.showErrorDialog(e);
		}
	}, COMPILE),
	CLEAR_RAM(2, lmcInterface -> lmcInterface.getProcessor().clearMemory(), COMPILE),
	RUN(0, lmcInterface -> {
		try {
			lmcInterface.getProcessor().run();
		} catch (Exception e) {
			lmcInterface.showErrorDialog(new LMCRuntimeException("Unknown Runtime Exception."));
			throw new RuntimeException(e);
		}
	}, CLEAR_RAM, LOAD_INTO_RAM),
	STOP(-1, (lmc) -> lmc.getProcessor().forceHalt());

	private final Consumer<LMCInterface> action;
	private final ControlFunction[] inheritedFunctions;
	private final int priority;

	ControlFunction(int priority, Consumer<LMCInterface> action, ControlFunction... inheritedFunctions) {
		this.action = action;
		this.inheritedFunctions = inheritedFunctions;
		this.priority = priority;
	}


	public void executeAction(LMCInterface lmcInterface) {
		getAllFunctions().stream().sorted(Comparator.comparingInt(ControlFunction::getPriority).reversed()).forEach(a -> a.action.accept(lmcInterface));
	}

	private Set<ControlFunction> getAllFunctions() {
		Set<ControlFunction> set = new HashSet<>();
		set.add(this);

		for (ControlFunction function : inheritedFunctions) {
			set.addAll(function.getAllFunctions());
		}
		return set;
	}

	private int getPriority() {
		return priority;
	}
}